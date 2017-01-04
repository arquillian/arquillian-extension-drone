package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.execution.Execution;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessResult;
import org.arquillian.spacelift.task.os.CommandTool;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.SeleniumGoogleStorageSource;
import org.jboss.arquillian.drone.webdriver.binary.process.BinaryInteraction;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * A class for handling selenium server binaries. It also runs the selenium server with properties that are
 * appropriately configured
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class SeleniumServerBinaryHandler extends AbstractBinaryHandler {

    private static final String SELENIUM_SERVER_VERSION_PROPERTY = "seleniumServerVersion";
    private static final String SELENIUM_SERVER_URL_PROPERTY = "seleniumServerUrl";

    private Logger log = Logger.getLogger(SeleniumServerBinaryHandler.class.toString());

    private DesiredCapabilities capabilities;

    public SeleniumServerBinaryHandler(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Returns an instance of a {@link BinaryHandler} according to given browser
     *
     * @param browser A browser name an associated {@link BinaryHandler} should be returned
     * @return An instance of a {@link BinaryHandler} according to given browser
     */
    public BinaryHandler getBrowserBinaryHandler(String browser) {
        if (new BrowserCapabilitiesList.Firefox().getReadableName().equals(browser)) {
            return new FirefoxDriverBinaryHandler(capabilities);

        } else if (new BrowserCapabilitiesList.Chrome().getReadableName().equals(browser)) {
            return new ChromeDriverBinaryHandler(capabilities);

        } else if (new BrowserCapabilitiesList.InternetExplorer().getReadableName().equals(browser)) {
            return new InternetExplorerBinaryHandler(capabilities);

        } else if (new BrowserCapabilitiesList.PhantomJS().getReadableName().equals(browser)){
            return new PhantomJSDriverBinaryHandler(capabilities);
        }
        return null;
    }

    /**
     * Downloads and runs selenium server jar file
     *
     * @param browser A browser name the selenium server should be run with
     */
    public void downloadAndRun(String browser) {
        String seleniumServer = null;
        try {
            seleniumServer = downloadAndPrepare().toString();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Something bad happened when Drone was trying to download and extract Selenium Server binary. "
                    + "For more information see the cause.", e);

        }
        BinaryHandler browserBinaryHandler = getBrowserBinaryHandler(browser);

        CommandBuilder javaCommand = new CommandBuilder("java");
        if (browserBinaryHandler != null) {
            try {
                File driverBinary = browserBinaryHandler.downloadAndPrepare();
                if (driverBinary != null) {
                    javaCommand
                        .parameter(
                            "-D" + browserBinaryHandler.getSystemBinaryProperty() + "=" + driverBinary.getAbsolutePath());
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Something bad happened when Drone was trying to download and extract driver binary of a browser: "
                        + browser
                        + "\nFor more information see the cause.", e);
            }
        }

        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Command build = javaCommand.parameters("-jar", seleniumServer).build();
            log.info("Running Selenium server process: " + build.toString());
            Execution<ProcessResult> server = Spacelift
                .task(CommandTool.class)
                .command(build)
                .runAsDaemon()
                .interaction(new BinaryInteraction()
                                 .outputPrefix("[Selenium server] ")
                                 .printToOut(".*")
                                 .when(".*Selenium Server is up and running")
                                 .thenCountDown(countDownLatch)
                                 .build())
                .execute();
            server.registerShutdownHook();

            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Something bad happened when Drone was trying to run Selenium Server binary: " + seleniumServer
                    + " For more information see the cause.", e);
        }
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return "selenium-server";
    }

    @Override
    protected String getDesiredVersionProperty() {
        return SELENIUM_SERVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return SELENIUM_SERVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new SeleniumServerStorage((String) capabilities.getCapability(SELENIUM_SERVER_VERSION_PROPERTY));
    }

    @Override
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return "";
    }

    @Override
    public String getSystemBinaryProperty() {
        return "";
    }

    static class SeleniumServerStorage extends SeleniumGoogleStorageSource {

        private String version;

        SeleniumServerStorage(String version) {
            this.version = version;
        }

        @Override
        protected String getExpectedKeyRegex(String requiredVersion, String directory) {
            StringBuffer regexBuffer = new StringBuffer("%s/selenium-server-standalone-");
            regexBuffer.append("%s.jar");

            String regex;
            if (version == null) {
                regex = String.format(regexBuffer.toString(), directory, directory + ".*");
            } else {
                regex = String.format(regexBuffer.toString(), getDirectoryFromFullVersion(version), version);
            }
            return regex;
        }
    }
}
