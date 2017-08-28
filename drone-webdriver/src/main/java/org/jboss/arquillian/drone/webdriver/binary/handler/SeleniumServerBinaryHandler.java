package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.SeleniumGoogleStorageSource;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.logging.Logger;

import static org.jboss.arquillian.drone.webdriver.utils.Validate.empty;

/**
 * A class for handling selenium server binaries. It also runs the selenium server with properties that are
 * appropriately configured
 */
public class SeleniumServerBinaryHandler extends AbstractBinaryHandler {

    public static final String SELENIUM_SERVER_VERSION_PROPERTY = "seleniumServerVersion";
    private static final String SELENIUM_SERVER_SYSTEM_DRIVER_BINARY_PROPERTY = "selenium.server.binary.path";
    private static final String SELENIUM_SERVER_DRIVER_BINARY_PROPERTY = "seleniumServerBinary";
    private static final String SELENIUM_SERVER_URL_PROPERTY = "seleniumServerUrl";

    private Logger log = Logger.getLogger(SeleniumServerBinaryHandler.class.toString());

    private DesiredCapabilities capabilities;

    public SeleniumServerBinaryHandler(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
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
        return new SeleniumServerStorage((String) capabilities.getCapability(SELENIUM_SERVER_VERSION_PROPERTY),
            new HttpClient());
    }

    @Override
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return SELENIUM_SERVER_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return SELENIUM_SERVER_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    static class SeleniumServerStorage extends SeleniumGoogleStorageSource {

        private String version;

        SeleniumServerStorage(String version, HttpClient httpClient) {
            super(httpClient);
            this.version = version;
        }

        @Override
        protected String getExpectedKeyRegex(String requiredVersion, String directory) {
            if (empty(version)) {
                return directory + "/" + getFileNameRegexToDownload(directory + ".*");
            } else {
                return getDirectoryFromFullVersion(version) + "/" + getFileNameRegexToDownload(version);
            }
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            return String.format("selenium-server-standalone-%s.jar", version);
        }
    }
}
