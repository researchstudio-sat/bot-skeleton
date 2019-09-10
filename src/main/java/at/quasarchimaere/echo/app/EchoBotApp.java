package at.quasarchimaere.echo.app;

import org.springframework.boot.SpringApplication;

public class EchoBotApp {
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(new Object[] { "classpath:/spring/app/echoBotApp.xml" });
        app.setWebEnvironment(false);
        app.run(args);
        // ConfigurableApplicationContext applicationContext = app.run(args);
        // Thread.sleep(5*60*1000);
        // app.exit(applicationContext);
    }
}
