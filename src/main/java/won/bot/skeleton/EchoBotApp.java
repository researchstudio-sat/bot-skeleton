package won.bot.skeleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import javax.crypto.Cipher;
import java.lang.invoke.MethodHandles;

public class EchoBotApp {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static void main(String[] args) throws Exception {
        if(Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE) {
            logger.error("JCE unlimited strength encryption policy is not enabled, WoN applications will not work. Please consult the setup guide.");
            System.exit(1);
        }
        if(System.getProperty("WON_NODE_URI") == null && System.getenv("WON_NODE_URI") == null) {
            logger.error("WON_NODE_URI needs to be set to the node you want to connect to. e.g. https://node.matchat.org/won");
            System.exit(1);
        }
        if(System.getProperty("WON_CONFIG_DIR") == null && System.getenv("WON_CONFIG_DIR") == null) {
            logger.error("WON_CONFIG_DIR needs to be set");
            System.exit(1);
        }
        SpringApplication app = new SpringApplication("classpath:/spring/app/echoBotApp.xml");
        app.setWebEnvironment(false);
        app.run(args);
        // ConfigurableApplicationContext applicationContext = app.run(args);
        // Thread.sleep(5*60*1000);
        // app.exit(applicationContext);
    }
}
