package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.GoogleStorageSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.regex.Pattern;

/**
 * A class for handling chromeDriver binaries
 */
public class ChromeDriverBinaryHandler extends AbstractBinaryHandler {

    public static final String CHROME_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.chrome.driver";
    public static final String CHROME_DRIVER_BINARY_PROPERTY = "chromeDriverBinary";
    private static final String CHROME_DRIVER_VERSION_PROPERTY = "chromeDriverVersion";
    private static final String CHROME_DRIVER_URL_PROPERTY = "chromeDriverUrl";

    private DesiredCapabilities capabilities;

    public ChromeDriverBinaryHandler(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.Chrome().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return CHROME_DRIVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return CHROME_DRIVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new ChromeStorageSources("https://chromedriver.storage.googleapis.com/");
    }

    @Override
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return CHROME_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return CHROME_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    private class ChromeStorageSources extends GoogleStorageSource {

        ChromeStorageSources(String baseUrl) {
            super(baseUrl, baseUrl + "LATEST_RELEASE", new HttpClient());
        }

        protected String getExpectedKeyRegex(String requiredVersion, String directory) {
            return Pattern.quote(requiredVersion + "/" + getFileNameRegexToDownload(requiredVersion));
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            StringBuilder fileName = new StringBuilder("chromedriver_");
            if (PlatformUtils.isMac()) {
                fileName.append("mac64");
            } else if (PlatformUtils.isWindows()) {
                fileName.append("win32");
            } else if (PlatformUtils.isUnix()) {
                fileName.append("linux");
                if (PlatformUtils.is32()) {
                    fileName.append("32");
                } else {
                    fileName.append("64");
                }
            }
            return fileName.append(".zip").toString();
        }
    }
}
