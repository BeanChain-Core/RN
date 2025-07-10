package io.beanchain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.beanchain.managers.DBManager;
import io.beanchain.models.TrustEntry;
import io.beanchain.models.TrustListEntry;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrustDB {
    private final DB db;
    private static final double MAX_TRUST = 99.5;
    private static final double MIN_TRUST = 0.5;

    public TrustDB() {
        this.db = DBManager.getDB("trustDB");
    }

    public void saveTrustEntry(TrustEntry entry) throws IOException {
        String key = "trust:" + entry.getPubKey();
        String json = new ObjectMapper().writeValueAsString(entry);
        db.put(bytes(key), bytes(json));
    }

    public TrustEntry getTrustEntry(String pubKey) throws IOException {
        String key = "trust:" + pubKey;
        byte[] value = db.get(bytes(key));
        if (value == null) return null;
        return new ObjectMapper().readValue(value, TrustEntry.class);
    }

    public void increaseTrust(String pubKey) throws IOException {
        TrustEntry entry = getTrustEntry(pubKey);
        if (entry == null) return;
        double newScore = Math.min(entry.getTrustScore() + 1.0, MAX_TRUST);
        entry.setTrustScore(newScore);
        entry.setLastPinged(System.currentTimeMillis());
        entry.setSuccessfulPings(entry.getSuccessfulPings() + 1);
        saveTrustEntry(entry);
    }

    public void decreaseTrust(String pubKey) throws IOException {
        TrustEntry entry = getTrustEntry(pubKey);
        if (entry == null) return;
        double newScore = Math.max(entry.getTrustScore() - 1.0, MIN_TRUST);
        entry.setTrustScore(newScore);
        entry.setLastPinged(System.currentTimeMillis());
        saveTrustEntry(entry);
    }

    public void updateBlock(long height) throws IOException {
        db.put(bytes("block:height"), bytes(String.valueOf(height)));
    }

    public long getCurrentBlockHeight() throws IOException {
        byte[] value = db.get(bytes("block:height"));
        if (value == null) return 0;
        return Long.parseLong(new String(value, StandardCharsets.UTF_8));
    }

    public List<TrustEntry> getAllPublicNodes() throws IOException {
        List<TrustEntry> result = new ArrayList<>();
        DBIterator iterator = db.iterator();

        try {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = new String(iterator.peekNext().getKey(), StandardCharsets.UTF_8);
                if (key.startsWith("trust:") && !key.equals("trust:currentHeight") && !key.equals("block:height")) {
                    byte[] value = iterator.peekNext().getValue();
                    TrustEntry entry = new ObjectMapper().readValue(value, TrustEntry.class);
                    if (entry.isPublic()) {
                        result.add(entry);
                    }
                }
            }
        } finally {
            iterator.close();
        }

        return result;
    }

    public String buildTrustSnapshot() throws IOException {
        long currentHeight = getCurrentBlockHeight();
        List<TrustEntry> publicNodes = getAllPublicNodes();

        List<TrustListEntry> trustList = new ArrayList<>();
        for (TrustEntry entry : publicNodes) {
            trustList.add(new TrustListEntry(entry));
        }

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("type", "trust_snapshot");
        snapshot.put("heartbeatHeight", currentHeight);
        snapshot.put("trustList", trustList);

        return new ObjectMapper().writeValueAsString(snapshot); // Ready to send via P2P
    }

    private byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
