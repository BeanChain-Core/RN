package io.beanchain.services;

import java.io.IOException;
import java.security.PrivateKey;

import com.beanpack.TXs.*;
import com.beanpack.Utils.*;
import com.beanpack.crypto.WalletGenerator;

import io.beanchain.managers.RewardWalletManager;

public class InternalTxFactory {
    
    private static final String EARLY_WALLET = "BEANX:0xEARLYWALLET";
    private static final String GAS_WALLET = "BEANX:0xGASPOOL";
    private static final String FAUCET_WALLET = "BEANX:0xFAUCETWALLET";
    
    private static String rewardPrivateKeyHex;
    private static PrivateKey fullNodePrivateKey;
    private static String pubKey;
    private static String rewardAddress;
    
    


    public static AirdropTX createEarlyRewardTx(String toAddress) throws IOException, Exception {
        String RNWallet = rewardAddress;
        long beantoshi = beantoshinomics.toBeantoshi(100);

        if (!RewardWalletManager.hasEnough(EARLY_WALLET, beantoshi)) {
            System.err.println("Not enough balance in EARLY_WALLET to reward " + toAddress);
            return null;
        }

        int nonce = RewardDB.incrementSystemNonce();
        AirdropTX tx = new AirdropTX(RNWallet, toAddress, 100, nonce, EARLY_WALLET, 0);
        tx.sign(getKey());
        RewardWalletManager.deduct(EARLY_WALLET, beantoshi);
        return tx;     
    }

    public static double calculateDrip() {
        long totalFaucetBalance = RewardWalletManager.getBalance(FAUCET_WALLET); // in beantoshi

        if (totalFaucetBalance <= 0) return 0;

        double initialRatio = 100.0 / 5_000_000; // First drip was 100 out of 5mil
        long nextDripToshi = Math.round(totalFaucetBalance * initialRatio);
        double nextDrip = beantoshinomics.toBean(nextDripToshi);

        if (nextDrip < 1) return 1;
        return nextDrip; // convert back to whole BEAN
    }

    public static AirdropTX createFaucetDripTx(String toAddress) throws IOException, Exception {
        String RNWallet = rewardAddress;
        double dripThis = calculateDrip();
        long beantoshi = beantoshinomics.toBeantoshi(dripThis);

        if (!RewardWalletManager.hasEnough(FAUCET_WALLET, beantoshi)) {
            System.err.println("Not enough balance in FAUCET_WALLET to reward " + toAddress);
            return null;
        }

        int nonce = RewardDB.incrementSystemNonce();
        AirdropTX tx = new AirdropTX(RNWallet, toAddress, dripThis, nonce, FAUCET_WALLET, 0);
        tx.sign(getKey());
        RewardWalletManager.deduct(FAUCET_WALLET, beantoshi);
        return tx;     
    }

    public static AirdropTX createValidatorGasRewardTx(String validatorAddress, long gasReward) throws IOException, Exception {
        String RNWallet = rewardAddress;
        long beantoshi = gasReward;
        double beanReward = beantoshinomics.toBean(beantoshi);


        int nonce = RewardDB.incrementSystemNonce();
        AirdropTX tx = new AirdropTX(RNWallet, validatorAddress, beanReward, nonce, GAS_WALLET, 0);
        tx.sign(getKey());
        return tx;  
    }

    public static void initialize(String key) throws Exception {
        rewardPrivateKeyHex = key;
        fullNodePrivateKey = WalletGenerator.restorePrivateKey(rewardPrivateKeyHex);
        pubKey = WalletGenerator.generatePublicKey(fullNodePrivateKey);
        rewardAddress = WalletGenerator.generateAddress(pubKey);
        System.out.println("InternalTxFactory initialized RN address: " + rewardAddress);
    }

    private static PrivateKey getKey() throws IOException, Exception{
        PrivateKey key = fullNodePrivateKey;
        return key;
    }
  
}
