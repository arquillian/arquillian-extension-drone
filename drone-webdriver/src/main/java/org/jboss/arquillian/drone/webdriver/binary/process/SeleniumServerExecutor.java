package org.jboss.arquillian.drone.webdriver.binary.process;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.openqa.selenium.grid.Main;

/**
 * Is responsible for launching and stopping selenium server binary
 */
public class SeleniumServerExecutor {
    private Logger log = Logger.getLogger(SeleniumServerExecutor.class.toString());

    /**
     * Runs an instance of Selenium Server
     */
    public void startSeleniumServer(@Observes StartSeleniumServer startSeleniumServer) {
        int port = startSeleniumServer.getUrl().getPort();
        String[] seleniumServerArgs = startSeleniumServer.getSeleniumServerArgs() == null ?
            new String[] { "standalone" } : startSeleniumServer.getSeleniumServerArgs().split("[,\\s]");
        boolean isHub = "hub".equals(seleniumServerArgs[0]);

        Stream.Builder<String> builder = Stream.<String>builder();
        Arrays.stream(seleniumServerArgs).forEach(builder::add);
        builder.add("--port").add(Integer.toString(port));
        if (!isHub) {
            builder.add("--selenium-manager").add("true")
                .add("--enable-managed-downloads").add("true");
        }

        String[] allArgs = builder.build().toArray(String[]::new);
        log.info("Starting server with: " + Arrays.asList(allArgs));
        Main.main(allArgs);
    }
}
