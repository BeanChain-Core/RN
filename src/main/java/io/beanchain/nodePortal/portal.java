package io.beanchain.nodePortal;

import org.springframework.boot.SpringApplication;
import com.beanpack.beanify.Branding;
import io.beanchain.config.ConfigLoader;
import io.beanchain.managers.RewardWalletManager;
import io.beanchain.tools.Node;
import io.beanchain.tools.RNApplication;

public class portal {
    public static volatile boolean isSyncing = true;
    public static final long BOOT_TIME = System.currentTimeMillis();

    public static void setIsSyncing(boolean bool) {isSyncing = bool;}


    public static void main(String[] args) throws Exception {
        System.out.println(Branding.logo);
        ConfigLoader.loadConfig();
        RewardWalletManager.initializeBalances();

        try {
            Thread springThread = new Thread(() -> {
                    SpringApplication.run(RNApplication.class);
                }, "SpringThread");

            springThread.setDaemon(false);
            springThread.start();
        } catch (Exception e){
            System.out.println("ERROR LAUNCHING SPRING");
        }
        Node node = new Node("0.0.0.0");
        node.start();
        node.connectToParentNode();
    }
    
}
