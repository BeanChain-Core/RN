package io.beanchain.controllers;

import com.beanpack.TXs.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class PeerConnector {

    private static Socket gpnSocket;
    private static PrintWriter out;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static int currentPing = 0;

    // Call this once after GPN socket is established
    public static void init(Socket gpn) {
        try {
            gpnSocket = gpn;
            out = new PrintWriter(gpnSocket.getOutputStream(), true);
            System.out.println("✅ PeerConnector initialized for GPN.");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize PeerConnector:");
            e.printStackTrace();
        }
    }

    public static void sendTxToGPN(TX tx) {
        if (tx == null) {
            System.err.println("⚠️ Tried to send null TX to GPN.");
            return;
        }

        if (out == null) {
            System.err.println("❌ GPN socket not initialized. Cannot send TX.");
            return;
        }

        try {
            ObjectNode wrapper = mapper.createObjectNode();
            wrapper.put("type", "transaction");
            wrapper.set("payload", mapper.readTree(tx.createJSON()));

            String message = mapper.writeValueAsString(wrapper);
            out.println(message);

            System.out.println("📤 Sent TX to GPN: " + tx.getTxHash());
        } catch (Exception e) {
            System.err.println("❌ Failed to send TX to GPN:");
            e.printStackTrace();
        }
    }

    public static void startPingGossipToGPN() {
        if (out == null) {
            System.err.println("GPN socket not initialized. Cannot send Ping Gossip.");
            return;
        }

        try {
            currentPing = PingUtils.generateRandomPingNumber();
            ObjectNode wrapper = mapper.createObjectNode();
            wrapper.put("type", "ping");
            wrapper.put("payload", String.valueOf(currentPing)); // FIXED

            String message = mapper.writeValueAsString(wrapper);
            out.println(message);

            System.out.println("[BeanLog] [PING] gossip sent: " + currentPing);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        return gpnSocket != null && gpnSocket.isConnected() && !gpnSocket.isClosed();
    }

    public static void close() {
        try {
            if (gpnSocket != null) gpnSocket.close();
        } catch (Exception e) {
            System.err.println("Error closing GPN socket:");
            e.printStackTrace();
        }
    }
}

class PingUtils {
    private static final Random random = new Random();

    public static int generateRandomPingNumber() {
        return 10000000 + random.nextInt(90000000);
    }
}

