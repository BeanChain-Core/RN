package io.beanchain.controllers;

import java.net.Socket;
import com.beanpack.Block.Block;
import com.beanpack.Block.BlockHeader;
import com.beanpack.TXs.*;
import com.beanpack.Utils.hex;
import com.beanpack.crypto.TransactionVerifier;
import com.beanpack.crypto.WalletGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.beanchain.services.InternalTxFactory;
import io.beanchain.services.PongDBService;
import io.beanchain.services.RewardDB;
import io.beanchain.services.TrustDB;
import io.beanchain.tools.EarlyWalletRegistry;
import io.beanchain.tools.Hasher;
import io.beanchain.tools.Node;

public class MessageRouter {

    public MessageRouter() {}

    public void route(JsonNode message, Socket peer) {
        if (!message.has("type")) {
            System.out.println("Invalid message (missing 'type')");
            return;
        }

        String type = message.get("type").asText();

        switch (type) {
            case "sync_response":
                handleSyncResponse(message);
                break;
            case "transaction":
                handleIncomingTransaction(message);
                break;
            case "block":
                handleIncomingBlock(message);
                break;
            case "pong":
                handlePong(message, peer.getInetAddress().getHostAddress());
                break;
            default:
                System.out.println("[UNKNOWN]: " + type);
        }
    }

    public static void handlePong(JsonNode message, String sourceIp) {
        try {
            JsonNode payload = message.get("payload");
            if (payload == null) {
                System.err.println("[Pong] Missing payload.");
                return;
            }

            String pingNumber = payload.get("pingNumber").asText();
            String publicKeyHex = payload.get("publicKey").asText();
            String receivedHash = payload.get("hash").asText();
            String signature = payload.get("signature").asText();

            // Step 1: Reconstruct hash from pingNumber + publicKey
            String rawData = pingNumber + publicKeyHex;
            String computedHash = Hasher.generateHash(rawData); 

            if (!computedHash.equals(receivedHash)) {
                System.err.println("[Pong] Hash mismatch. Possible tampering.");
                return;
            }

            // Step 2: Verify signature against the hash
            boolean isValid = TransactionVerifier.verifySHA256Transaction(publicKeyHex, hex.hexToBytes(computedHash), signature); // assumes hex input
            if (!isValid) {
                System.err.println("[Pong] Invalid signature.");
                return;
            }

            // Step 3: Derive wallet address from public key
            String address = WalletGenerator.generateAddress(publicKeyHex);

            TX tx = InternalTxFactory.createNodeRewardTx(address);
            PeerConnector.sendTxToGPN(tx);

            // Step 4: Record to pongDB
            PongDBService.recordPongResponse(pingNumber, address, sourceIp);
            System.out.println("[Pong] ✅ Verified and recorded pong for: " + address);

        } catch (Exception e) {
            System.err.println("[Pong] Error handling pong: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSyncResponse(JsonNode msg) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode confirmedTxs = msg.get("confirmedTxs");
    
            int checked = 0;
            int rewarded = 0;
            
    
            for (JsonNode txNode : confirmedTxs) {
                TX tx = mapper.treeToValue(txNode, TX.class);
                String recipient = tx.getTo();
    
                if (recipient == null || recipient.startsWith("BEANX:0x") == false) continue;
    
                if (EarlyWalletRegistry.isExcludedFromRewards(recipient)) {
                    System.out.println("⛔ Skipping reward: " + recipient + " is genesis-funded.");
                    return;
                }

                checked++;
    
                // 🔍 Check if this wallet already got early reward
                if (!RewardDB.hasReceivedEarlyReward(recipient)) {
                    // 🪙 Mark as rewarded
                    RewardDB.markAsRewarded(recipient);
    
                    // 🧾 Create internal reward TX
                    TX rewardTx = InternalTxFactory.createEarlyRewardTx(recipient);
    
                    // 📡 Broadcast to GPN
                    PeerConnector.sendTxToGPN(rewardTx);
    
                    rewarded++;
                }
            }
    
            System.out.println("🎯 RN finished TX sync:");
            System.out.println("   ➤ Wallets checked: " + checked);
            System.out.println("   ➤ Rewards sent: " + rewarded);
    
        } catch (Exception e) {
            System.err.println("❌ RN failed to process sync_response:");
            e.printStackTrace();
        }
    }

    
    private void handleIncomingTransaction(JsonNode msg) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        TX tx = mapper.treeToValue(msg.get("payload"), TX.class);
        String toAddress = tx.getTo();

        if (toAddress == null || !toAddress.startsWith("BEANX:0x") || toAddress.startsWith("BBEANX:0x")) {
            System.out.println("Ignored non-user TX: " + tx.getTxHash());
            return;
        }

        if (EarlyWalletRegistry.isExcludedFromRewards(toAddress)) {
            System.out.println("⛔ Skipping reward: " + toAddress + " is genesis-funded.");
            return;
        }

        // Check if wallet already got early reward
        if (!RewardDB.hasReceivedEarlyReward(toAddress)) {
            System.out.println("🪙 New eligible wallet: " + toAddress);

            // Mark it as rewarded
            RewardDB.markAsRewarded(toAddress);

            // Build internal TX from EARLYWALLET to this address
            AirdropTX rewardTx = InternalTxFactory.createEarlyRewardTx(toAddress);

            // Send it to the GPN via P2P
            PeerConnector.sendTxToGPN(rewardTx);

            System.out.println("🎁 Early reward sent for: " + toAddress);
        } else {
            System.out.println("🔁 Wallet already rewarded: " + toAddress);
        }

    } catch (Exception e) {
        System.err.println("❌ Error handling incoming TX:");
        e.printStackTrace();
    }
}

private void handleIncomingBlock(JsonNode msg) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        Block block = mapper.treeToValue(msg.get("payload"), Block.class);

        BlockHeader header = block.getHeader();
        String validatorKey = header.getValidator();
        String validatorAdress = WalletGenerator.generateAddress(validatorKey);
        long gasFeeReward = (long) header.getGasFeeReward();

        int height = header.getHeight();
                if(height % 10 == 0){
                    PeerConnector.startPingGossipToGPN();
                }
                
        if (gasFeeReward <= 0) {
            System.out.println("No validator reward for block (gas fee = 0).");
            return;
        }
        
        TrustDB trusty = new TrustDB();
        trusty.updateBlock(height);

        AirdropTX validatorReward = InternalTxFactory.createValidatorGasRewardTx(validatorAdress, gasFeeReward);
        PeerConnector.sendTxToGPN(validatorReward);

    } catch (Exception e) {
        System.err.println("Error handling incoming Block:");
        e.printStackTrace();
    }
}

}
