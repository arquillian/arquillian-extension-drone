package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.util.logging.Logger;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.SeleniumGoogleStorageSource;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * A class for handling selenium server binaries. It also runs the selenium server with properties that are
 * appropriately configured
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class SeleniumServerBinaryHandler extends AbstractBinaryHandler {

    private static final String SELENIUM_SERVER_SYSTEM_DRIVER_BINARY_PROPERTY = "selenium.server.binary.path";
    private static final String SELENIUM_SERVER_DRIVER_BINARY_PROPERTY = "seleniumServerBinary";
    public static final String SELENIUM_SERVER_VERSION_PROPERTY = "seleniumServerVersion";
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
        return new SeleniumServerStorage((String) capabilities.getCapability(SELENIUM_SERVER_VERSION_PROPERTY));
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
