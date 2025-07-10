package io.beanchain.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.iq80.leveldb.DB;
import java.util.Collections;
import java.util.Map;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class PongDBService {
    private static final DB pongDB = io.beanchain.managers.DBManager.getDB("pongDB");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void recordPongResponse(String pingNumber, String walletAddress, String ipAddress) {
        try {
            String key = "ping:" + pingNumber;
            byte[] raw = pongDB.get(bytes(key));
            ObjectNode record;

            if (raw != null) {
                record = (ObjectNode) mapper.readTree(new String(raw));
            } else {
                record = mapper.createObjectNode();
            }

            record.put(walletAddress, ipAddress); // or `.put(walletAddress, true)` if IP not needed
            pongDB.put(bytes(key), bytes(record.toString()));

        } catch (Exception e) {
            System.err.println("[PongDB] Error recording pong: " + e.getMessage());
        }
    }

    public static Map<String, String> getResponders(String pingNumber) {
        try {
            String key = "ping:" + pingNumber;
            byte[] raw = pongDB.get(bytes(key));
            if (raw == null) return Collections.emptyMap();

            return mapper.readValue(new String(raw), new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            System.err.println("[PongDB] Error retrieving responders: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public static void deletePingRecord(String pingNumber) {
        try {
            pongDB.delete(bytes("ping:" + pingNumber));
        } catch (Exception e) {
            System.err.println("[PongDB] Error deleting ping record: " + e.getMessage());
        }
    }
}
