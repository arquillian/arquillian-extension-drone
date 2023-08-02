package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.GeckoDriverGitHubSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.openqa.selenium.Capabilities;

/**
 * A class for handling chromeDriver binaries
 */
public class FirefoxDriverBinaryHandler extends AbstractBinaryHandler {

    public static final String FIREFOX_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.gecko.driver";
    public static final String FIREFOX_DRIVER_BINARY_PROPERTY = "firefoxDriverBinary";
    private static final String FIREFOX_DRIVER_VERSION_PROPERTY = "firefoxDriverVersion";
    private static final String FIREFOX_DRIVER_URL_PROPERTY = "firefoxDriverUrl";

    private Capabilities capabilities;

    public FirefoxDriverBinaryHandler(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.Firefox().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return FIREFOX_DRIVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return FIREFOX_DRIVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new GeckoDriverGitHubSource(new HttpClient(), new GitHubLastUpdateCache(), capabilities);
    }

    @Override
    protected Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return FIREFOX_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return FIREFOX_SYSTEM_DRIVER_BINARY_PROPERTY;
    }
}
