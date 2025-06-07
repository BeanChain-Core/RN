package com.beanchainbeta.nodePortal;

//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Comparator;

import com.bean_core.beanify.Branding;
import com.beanchainbeta.config.ConfigLoader;
import com.beanchainbeta.managers.RewardWalletManager;
import com.beanchainbeta.services.InternalTxFactory;
import com.beanchainbeta.tools.Node;


//@SpringBootApplication
public class portal {
    public static final String currentVersion = "(BETA)";
    public static volatile boolean isSyncing = true;
    public static final long BOOT_TIME = System.currentTimeMillis();

    public static void setIsSyncing(boolean bool) {isSyncing = bool;}


    public static void main(String[] args) throws Exception {
        System.out.println(Branding.logo);
        ConfigLoader.loadConfig();
        RewardWalletManager.initializeBalances();
        Node node = new Node("0.0.0.0");
        node.start();
        node.connectToGPN();
    }
    
}
