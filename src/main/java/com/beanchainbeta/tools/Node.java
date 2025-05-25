package com.beanchainbeta.tools;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


import com.bean_core.TXs.TX;
import com.beanchainbeta.config.ConfigLoader;
import com.beanchainbeta.controllers.MessageRouter;
import com.beanchainbeta.controllers.PeerConnector;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

public class Node {
    private final int port = ConfigLoader.networkPort; 
    private String ip;
    private final ServerSocket serverSocket;
    private final Set<Socket> connectedPeers = ConcurrentHashMap.newKeySet();
    private final List<String> knownAddresses = new CopyOnWriteArrayList<>();
    private static Node instance;

    public static void initialize(String ip) throws IOException {
        if (instance == null) {
            instance = new Node(ip);
        }
    }

    public static Node getInstance() {
        return instance;
    }

    public Node(String ip) throws IOException {
        this.ip = ip;
        InetAddress bindAddress = ip.equals("0.0.0.0") ? InetAddress.getByName("0.0.0.0") : InetAddress.getByName(ip);
        this.serverSocket = new ServerSocket(port, 100, bindAddress);
    }

    public void start() {
        System.out.println("NodeBeta listening on: " + ip + ":" + port);
        new Thread(this::listenForPeers).start();
    }

    public void listenForPeers() {
        while (true) {
            try {
                Socket peer = serverSocket.accept();
                connectedPeers.add(peer);
                System.out.println("New peer connected: " + peer.getInetAddress().getHostAddress());
                new Thread(() -> handleIncomingMessages(peer)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleIncomingMessages(Socket peer) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(peer.getInputStream()))) {
            ObjectMapper mapper = new ObjectMapper();
            MessageRouter router = new MessageRouter();
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    JsonNode message = mapper.readTree(line);
                    router.route(message, peer);
                } catch (Exception e) {
                    System.err.println("Failed to parse incoming JSON: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost with peer: " + peer.getInetAddress());
        } finally {
            try {
                peer.close();
            } catch (IOException ignored) {}
            connectedPeers.remove(peer);
        }
    }

    public void broadcast(String message) {
        for (Socket peer : new ArrayList<>(connectedPeers)) {
            try {
                if (!peer.isClosed() && peer.isConnected()) {
                    PrintWriter out = new PrintWriter(peer.getOutputStream(), true);
                    out.println(message);
                } else {
                    connectedPeers.remove(peer);
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to broadcast message to peer: " + peer.getInetAddress());
                connectedPeers.remove(peer);
            }
        }
    }

    public void broadcastTransaction(TX tx) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode message = mapper.createObjectNode();
            message.put("type", "transaction");
            message.set("payload", mapper.readTree(tx.createJSON()));
            String jsonMessage = mapper.writeValueAsString(message);
            broadcast(jsonMessage);
        } catch (Exception e) {
            System.err.println(" Failed to broadcast transaction:");
            e.printStackTrace();
        }
    }

    public static void broadcastTransactionStatic(TX tx) {
        if (instance != null) {
            instance.broadcastTransaction(tx);
        }
    }


    public List<String> getKnownPeers() {
        return knownAddresses;
    }

    public void connectToPeer(String host) {
        try {
            Socket socket = new Socket(host, port);
            connectedPeers.add(socket);
            knownAddresses.add(host + ":" + port);
            System.out.println("Connected to peer: " + host + ":" + port);
            sendRewardNodeHandshake(socket);
            new Thread(() -> handleIncomingMessages(socket)).start();
        } catch (IOException e) {
            System.err.println("Failed to connect to peer at " + host + ":" + port);
        }
    }

    public void connectToGPN() {
        String host = ConfigLoader.bootstrapIp;
        int port = 6442;
        try {
            Socket socket = new Socket(host, port);
            PeerConnector.init(socket);
            connectedPeers.add(socket);
            knownAddresses.add(host + ":" + port);
            System.out.println("Connected to peer: " + host + ":" + port);
            sendRewardNodeHandshake(socket);
            new Thread(() -> handleIncomingMessages(socket)).start();
        } catch (IOException e) {
            System.err.println("Failed to connect to peer at " + host + ":" + port);
        }
    }

    public static void sendRewardNodeHandshake(Socket socket) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode msg = mapper.createObjectNode();
            msg.put("type", "handshake");
            msg.put("address", "REWARD_NODE");
            msg.put("blockHeight", 0);
            msg.put("requestSync", true);
            msg.put("syncMode", "TX_ONLY");
            msg.put("isValidator", false);
            msg.put("isPublicNode", false);
    
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mapper.writeValueAsString(msg));
    
            System.out.println("🤝 Sent RN handshake requesting TX-only sync.");
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

