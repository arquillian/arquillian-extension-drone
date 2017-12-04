package org.jboss.arquillian.drone.webdriver.binary.process;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.execution.Execution;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessResult;
import org.arquillian.spacelift.task.os.CommandTool;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.webdriver.binary.handler.BinaryHandler;
import org.jboss.arquillian.drone.webdriver.binary.handler.ChromeDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.binary.handler.EdgeDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.binary.handler.FirefoxDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.binary.handler.InternetExplorerBinaryHandler;
import org.jboss.arquillian.drone.webdriver.binary.handler.PhantomJSDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Is responsible for launching and stopping selenium server binary
 */
public class SeleniumServerExecutor {

    private Logger log = Logger.getLogger(SeleniumServerExecutor.class.toString());

    @Inject
    @ApplicationScoped
    private InstanceProducer<SeleniumServerExecution> seleniumServerExecutionInstanceProducer;

    /**
     * Runs an instance of Selenium Server
     */
    public void startSeleniumServer(@Observes StartSeleniumServer startSeleniumServer) {
        String browser = startSeleniumServer.getBrowser();
        String seleniumServerArgs = startSeleniumServer.getSeleniumServerArgs();
        String seleniumServer = startSeleniumServer.getPathToSeleniumServerBinary();
        int port = startSeleniumServer.getUrl().getPort();

        BinaryHandler browserBinaryHandler = getBrowserBinaryHandler(startSeleniumServer.getCapabilities(), browser);

        CommandBuilder javaCommand = new CommandBuilder("java");
        if (browserBinaryHandler != null) {
            try {
                String driverBinary = browserBinaryHandler.checkAndSetBinary(true);
                if (!Validate.empty(driverBinary)) {
                    javaCommand
                        .parameter(
                            "-D" + browserBinaryHandler.getSystemBinaryProperty() + "=" + new File(driverBinary)
                                .getAbsolutePath());
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Something bad happened when Drone was trying to download and extract driver binary of a browser: "
                        + browser
                        + "\nFor more information see the cause.", e);
            }
        }

        try {
            List<String> parameterList =
                new ArrayList<>(Arrays.asList("-jar", seleniumServer, "-port", String.valueOf(port)));

            if (seleniumServerArgs != null && !seleniumServerArgs.isEmpty()) {
                parameterList.addAll(Arrays.asList(seleniumServerArgs.split(" ")));
            }

            Command build = javaCommand.parameters(parameterList).build();

            SeleniumServerExecution execution = new SeleniumServerExecution().execute(build);

            seleniumServerExecutionInstanceProducer.set(execution);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Something bad happened when Drone was trying to run Selenium Server binary: " + seleniumServer
                    + " For more information see the cause.", e);
        }
    }

    /**
     * Stops an instance of Selenium Server
     */
    public void stopSeleniumServer(@Observes AfterSuite afterClass, SeleniumServerExecution seleniumServerExecution) {
        seleniumServerExecution.stop();
    }

    /**
     * Returns an instance of a {@link BinaryHandler} according to a given browser
     *
     * @param browser
     *     A browser name an associated {@link BinaryHandler} should be returned
     *
     * @return An instance of a {@link BinaryHandler} according to given browser
     */
    private BinaryHandler getBrowserBinaryHandler(DesiredCapabilities capabilities, String browser) {
        if (new BrowserCapabilitiesList.Firefox().getReadableName().equals(browser)) {
            return new FirefoxDriverBinaryHandler(capabilities);
        } else if (new BrowserCapabilitiesList.Edge().getReadableName().equals(browser)) {
            return new EdgeDriverBinaryHandler(capabilities);
        } else if (new BrowserCapabilitiesList.Chrome().getReadableName().equals(browser)) {
            return new ChromeDriverBinaryHandler(capabilities);
        } else if (new BrowserCapabilitiesList.InternetExplorer().getReadableName().equals(browser)) {
            return new InternetExplorerBinaryHandler(capabilities);
        } else if (new BrowserCapabilitiesList.PhantomJS().getReadableName().equals(browser)) {
            log.warning("Make sure that you are using Selenium server compatible with PhantomJS."
                + " PhantomJS is not supported in remote webdriver since Selenium 3.8.0 - see this commit for reference"
                + " https://github.com/SeleniumHQ/selenium/commit/de5c81fd86a3228195d2f6d5d9526bbc4b3c3534"
                + " To use Selenium server 3.7.1 add <property name=\"seleniumServerVersion\">3.7.1</property> to your arquillian.xml file."
                + " You can also execute Selenium server on command line by adding PhantomJS driver jar on classpath.");
            return new PhantomJSDriverBinaryHandler(capabilities);
        } else if (new BrowserCapabilitiesList.ChromeHeadless().getReadableName().equals(browser)) {
            return new ChromeDriverBinaryHandler(capabilities);
        }
        return null;
    }

    class SeleniumServerExecution {

        private Execution<ProcessResult> server;

        SeleniumServerExecution() {
        }

        SeleniumServerExecution execute(Command command) throws InterruptedException {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            log.info("Running Selenium server process: " + command.toString());
            server = Spacelift
                .task(CommandTool.class)
                .command(command)
                .interaction(new BinaryInteraction()
                    .outputPrefix("[Selenium server] ")
                    .printToOut(".*")
                    .when(".+Selenium.+is up and running$")
                    .thenCountDown(countDownLatch)
                    .build())
                .execute();
            server.registerShutdownHook();
            countDownLatch.await(10, TimeUnit.SECONDS);
            return this;
        }

        void stop() {
            if (server != null) {
                log.info("Stopping selenium server ...");
                server.terminate();
            }
        }
    }
}
