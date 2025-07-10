package io.beanchain.nodePortal;

import java.io.File;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;

import com.beanpack.Wizard.WizCryptHandler;
import com.beanpack.beanify.Branding;
import io.beanchain.config.ConfigLoader;
import io.beanchain.managers.RewardWalletManager;
import io.beanchain.services.InternalTxFactory;
import io.beanchain.tools.Node;
import io.beanchain.RNApplication;

public class portal {
    public static volatile boolean isSyncing = true;
    public static final long BOOT_TIME = System.currentTimeMillis();

    public static void setIsSyncing(boolean bool) {isSyncing = bool;}

    private static final String CONFIG_FOLDER = "config.docs/";
    private static final String KEY_PATH = CONFIG_FOLDER + "wiz.txt";


    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[WIZCrypt] Shutting down — decrypting config...");
            WizCryptHandler.decryptConfig();
            scanner.close();
        }));
        System.out.println(Branding.logo);
        ConfigLoader.loadConfig();

        
        System.out.println("ENTER TEAM PASSWORD");
        String pass = scanner.nextLine().trim();
        WizCryptHandler.setPassword(pass);
        WizCryptHandler.setWizFileEnc("wiz.txt.enc");
        WizCryptHandler.setConfigFolder(new File("config.docs/"));
        WizCryptHandler.setKeyPath(KEY_PATH);
        WizCryptHandler.bootWizCrypt(pass);

        String key = WizCryptHandler.readL2EncWizKey();
        InternalTxFactory.initialize(key);

        WizCryptHandler.encryptConfig();
       
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
