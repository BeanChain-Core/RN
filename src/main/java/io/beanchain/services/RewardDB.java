package io.beanchain.services;

import org.iq80.leveldb.DB;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.beanchain.managers.DBManager;

import java.nio.charset.StandardCharsets;

public class RewardDB {
    private static final String DB_NAME = "rewardDB";
    private static final DB db = DBManager.getDB(DB_NAME);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String INIT_KEY_PREFIX = "init-";
    private static final String NONCE_KEY = "RN-THIS";
    private static final int timeout = 1; // adjust this for drip timeout in hours 

    // Fetch or create reward profile
    public static ObjectNode getProfile(String address) {
        try {
            byte[] raw = db.get(address.getBytes(StandardCharsets.UTF_8));
            if (raw != null) {
                return (ObjectNode) mapper.readTree(new String(raw, StandardCharsets.UTF_8));
            } else {
                // Create new blank profile
                ObjectNode profile = mapper.createObjectNode();
                profile.put("earlyFlag", false);
                profile.put("lastDrip", 0L);
                profile.put("nodeScore", 0);
                return profile;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("⚠️ Failed to parse reward profile for: " + address);
            return mapper.createObjectNode(); // fail-safe
        }
    }

    // Save a profile back to DB
    public static void saveProfile(String address, ObjectNode profile) {
        try {
            String json = mapper.writeValueAsString(profile);
            db.put(address.getBytes(StandardCharsets.UTF_8), json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("⚠️ Failed to save reward profile for: " + address);
        }
    }

    // High-level helper for early reward status
    public static boolean hasReceivedEarlyReward(String address) {
        return getProfile(address).get("earlyFlag").asBoolean(false);
    }

    public static void markAsRewarded(String address) {
        ObjectNode profile = getProfile(address);
        profile.put("earlyFlag", true);
        saveProfile(address, profile);
    }

    // High-level helper for faucet drip time
    public static long getLastDrip(String address) {
        return getProfile(address).get("lastDrip").asLong(0);
    }

    public static void updateLastDrip(String address) {
        ObjectNode profile = getProfile(address);
        profile.put("lastDrip", System.currentTimeMillis());
        saveProfile(address, profile);
    }

    public static boolean canDrip(String address) {
        long lastDrip = getLastDrip(address);
        long now = System.currentTimeMillis();
        long cooldownPeriod = timeout * 60 * 60 * 1000; // timeout = number of hours (default set to 1)

        return (now - lastDrip) >= cooldownPeriod;  // returns true if enough time passed
    }

    public static long getRemainingDripTime(String address) {
        long lastDrip = getLastDrip(address);
        long now = System.currentTimeMillis();
        long cooldownPeriod = timeout * 60 * 60 * 1000; // timeout in hours → ms

        long timeLeft = (lastDrip + cooldownPeriod) - now;
        return Math.max(timeLeft, 0); // If cooldown passed, return 0
    }

    // High-level helper for node trust score
    public static int getNodeScore(String address) {
        return getProfile(address).get("nodeScore").asInt(0);
    }

    public static void updateNodeScore(String address, int score) {
        ObjectNode profile = getProfile(address);
        profile.put("nodeScore", score);
        saveProfile(address, profile);
    }

    public static int getSystemNonce() {
        try {
            byte[] data = db.get(NONCE_KEY.getBytes(StandardCharsets.UTF_8));
            if (data != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(new String(data, StandardCharsets.UTF_8));
                return node.has("nonce") ? node.get("nonce").asInt() : 0;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error reading system nonce.");
        }
        return 0;
    }

    public static int incrementSystemNonce() {
        int next = getSystemNonce() + 1;

        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("nonce", next);
            db.put(NONCE_KEY.getBytes(StandardCharsets.UTF_8), node.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("❌ Failed to increment system nonce.");
        }

        return next;
    }


    public static long getSystemBalance(String wallet) {
        return getProfile(wallet).get("balance").asLong(0);
    }
    
    public static void setSystemBalance(String wallet, long beantoshi) {
        ObjectNode profile = getProfile(wallet);
        profile.put("balance", beantoshi);
        saveProfile(wallet, profile);
    }
    
    public static boolean deductSystemBalance(String wallet, long beantoshi) {
        long current = getSystemBalance(wallet);
        if (current < beantoshi) return false;
        setSystemBalance(wallet, current - beantoshi);
        return true;
    }

    public static boolean isInitialized(String wallet) {
        return db.get((INIT_KEY_PREFIX + wallet).getBytes()) != null;
    }

    public static void markInitialized(String wallet) {
        db.put((INIT_KEY_PREFIX + wallet).getBytes(), "true".getBytes());
    }

    
}
