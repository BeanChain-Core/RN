package io.beanchain.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    public static String bindAddress;
    public static int networkPort;
    public static boolean isBootstrapNode;
    public static String parentNodeIP;


    public static void loadConfig() throws Exception {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.docs/beanchain.config.properties")) { //LITE RN CONFIG FORK OF MAIN BEANNODE CONFIG
            props.load(fis);

            bindAddress = props.getProperty("bindAddress", "0.0.0.0");
            networkPort = Integer.parseInt(props.getProperty("networkPort", "6443")); //RN DEFAULT TO 6443 to CONNECT WITH NETORK NODE AT 6442
            isBootstrapNode = Boolean.parseBoolean(props.getProperty("isBootstrapNode", "false")); // WILL ALWAYS BE FALSE BUT LEFT AS OPTIONAL FOR WEIRD TEST CASES
            parentNodeIP = props.getProperty("parentNodeIP", "localhost"); //SET DEFAULT TO LOCALHOST BECAUSE THIS RN IS A PERIPHERAL NODE THAT NEEDS A PARENT

        } catch (IOException e) {
            System.err.println("⚠️ Failed to load BeanChain config: " + e.getMessage());
            System.exit(1);
        }
    }

}


