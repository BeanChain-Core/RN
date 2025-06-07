package com.beanchainbeta.managers;

import com.beanchainbeta.services.RewardDB;
import java.util.Map;

public class RewardWalletManager {

    private static final Map<String, Long> GENESIS_BALANCES = Map.of(
        "BEANX:0xEARLYWALLET", 5_000_000_000_000L,   // 5,000,000 BEAN
        "BEANX:0xFAUCETWALLET", 5_000_000_000_000L,  // 5,000,000 BEAN
        "BEANX:0xNODEREWARD", 30_000_000_000_000L    // 30,000,000 BEAN
    );

    public static void initializeBalances() {
        System.out.println("Checking system wallet balances...");
    
        for (Map.Entry<String, Long> entry : GENESIS_BALANCES.entrySet()) {
            String wallet = entry.getKey();
            long expectedBalance = entry.getValue();
    
            if (!RewardDB.isInitialized(wallet)) {
                RewardDB.setSystemBalance(wallet, expectedBalance);
                RewardDB.markInitialized(wallet);
                System.out.println("Initialized " + wallet + " to " + expectedBalance + " beantoshi");
            } else {
                long current = getBalance(wallet);
                System.out.println("🔁 " + wallet + " already initialized with " + current + " beantoshi");
            }
        }
    }

    public static boolean hasEnough(String wallet, long beantoshi) {
        return RewardDB.getSystemBalance(wallet) >= beantoshi;
    }

    public static void deduct(String wallet, long beantoshi) {
        RewardDB.deductSystemBalance(wallet, beantoshi);
    }

    public static long getBalance(String wallet) {
        return RewardDB.getSystemBalance(wallet);
    }
}

