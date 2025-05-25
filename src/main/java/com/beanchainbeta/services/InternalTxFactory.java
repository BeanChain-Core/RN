package com.beanchainbeta.services;

import java.io.IOException;
import java.security.PrivateKey;

import com.bean_core.NodeConfig.ConfigLoader;
import com.bean_core.TXs.*;
import com.beanchainbeta.controllers.RewardWalletManager;
import com.bean_core.Utils.*;
import com.bean_core.Wizard.wizard;
import com.bean_core.crypto.WalletGenerator;

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
