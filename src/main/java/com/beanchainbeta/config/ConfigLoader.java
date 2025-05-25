package com.beanchainbeta.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.bean_core.Wizard.wizard;
import com.bean_core.crypto.WalletGenerator;
import com.beanchainbeta.services.InternalTxFactory;

public class ConfigLoader {
    public static String privateKeyPath;
    public static String bindAddress;
    public static int networkPort;
    public static boolean isBootstrapNode;
    public static String bootstrapIp;

    public static void loadConfig() throws Exception {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.docs/rn.config.properties")) { //RN SPECIFIC HARDCODED CONFIG DIFFERENT THAN NORMAL NODE
            props.load(fis);

            privateKeyPath = props.getProperty("privateKeyPath", "config.docs/RN-WIZ.txt"); //RN SPECIFIC DEFAULTS FOR KEY STORAGE
            bindAddress = props.getProperty("bindAddress", "0.0.0.0");
            networkPort = Integer.parseInt(props.getProperty("networkPort", "6443")); //RN DEFAULT TO 6443 to CONNECT WITH NETORK NODE AT 6442
            isBootstrapNode = Boolean.parseBoolean(props.getProperty("isBootstrapNode", "false"));
            bootstrapIp = props.getProperty("bootstrapIp", "66.179.82.188");

            

            String key = wizard.wizardRead(privateKeyPath);
            InternalTxFactory.initialize(key);

        } catch (IOException e) {
            System.err.println("⚠️ Failed to load BeanChain config: " + e.getMessage());
            System.exit(1);
        }
    }

    // generate and save encrypted key using wiz
    public static void main(String[] args) throws Exception {
        String privateHash = WalletGenerator.generatePrivateKey();
        String pubKey = WalletGenerator.generatePublicKey(WalletGenerator.restorePrivateKey(privateHash));
        String addy = WalletGenerator.generateAddress(pubKey);
        System.out.println(addy + " " + privateHash);
        //wizard.saveKeyToWizard(privateHash, "config.docs/RN-WIZ.txt");
    }
}


