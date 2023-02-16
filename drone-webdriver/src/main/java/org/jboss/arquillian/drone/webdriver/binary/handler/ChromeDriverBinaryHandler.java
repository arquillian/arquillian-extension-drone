package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.MissingBinaryException;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.XmlStorageSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A class for handling chromeDriver binaries
 */
public class ChromeDriverBinaryHandler extends AbstractBinaryHandler {

    private static final Logger log = Logger.getLogger(ChromeDriverBinaryHandler.class.getName());

    public static final String CHROME_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.chrome.driver";
    public static final String CHROME_DRIVER_BINARY_PROPERTY = "chromeDriverBinary";
    private static final String CHROME_DRIVER_VERSION_PROPERTY = "chromeDriverVersion";
    private static final String CHROME_DRIVER_URL_PROPERTY = "chromeDriverUrl";

    public static final String CHROME_DRIVER_BINARY_NAME = "chromedriver" + (PlatformUtils.isWindows() ? ".exe" : "");

    private final DesiredCapabilities capabilities;

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

    @Override
    protected File prepare(File downloaded) throws Exception {
        File extraction = BinaryFilesUtils.extract(downloaded);
        File[] files = extraction.listFiles(file -> file.isFile() && file.getName().equals(CHROME_DRIVER_BINARY_NAME));

        if (files == null || files.length != 1) {
            throw new IllegalStateException(
                "Missing ChromeDriver executable (" + CHROME_DRIVER_BINARY_NAME + ") in the directory " + extraction);
        }

        return markAsExecutable(files[0]);
    }

    public static class ChromeStorageSources extends XmlStorageSource {

        public ChromeStorageSources(String baseUrl) {
            this(baseUrl, new HttpClient());
        }

        public ChromeStorageSources(String baseUrl, HttpClient client) {
            super(baseUrl, baseUrl + "LATEST_RELEASE", client);
        }

        @Override
        protected String getExpectedKeyRegex(String requiredVersion, String directory, Architecture architecture) {
            return Pattern.quote(requiredVersion + "/" + getFileNameRegexToDownload(requiredVersion, architecture));
        }

        @Override
        protected String getExpectedKeyRegex(String requiredVersion, String directory) {
            return getExpectedKeyRegex(requiredVersion, directory, Architecture.AUTO_DETECT);
        }

        @Override
        protected ExternalBinary getLatestRelease(String charset) throws Exception {
            ExternalBinary latestRelease;
            try {
                latestRelease = super.getLatestRelease(charset);
            } catch (MissingBinaryException e) {
                if (PlatformUtils.isWindows()) {
                    final String latestVersion = getVersion(urlToLatestRelease, charset);
                    log.log(Level.WARNING, "Failed downloading 64-bit version of Chrome Driver. Reason: ", e);
                    log.log(Level.WARNING, "Downloading 32-bit version of Chrome Driver instead. ({0})", latestVersion);

                    latestRelease = getReleaseForVersion(latestVersion, Architecture.BIT32);
                } else {
                    throw new MissingBinaryException(e.getMessage());
                }
            }
            return latestRelease;
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            return getFileNameRegexToDownload(version, Architecture.AUTO_DETECT);
        }

        @Override
        public String getFileNameRegexToDownload(String version, Architecture architecture) {
            final StringBuilder fileName = new StringBuilder("chromedriver_");
            if (PlatformUtils.isMac()) {
                fileName.append("mac");
            } else if (PlatformUtils.isWindows()) {
                fileName.append("win");
            } else if (PlatformUtils.isUnix()) {
                fileName.append("linux");
            }

            fileName.append(architecture.getValue());

            return fileName.append(".zip").toString();
        }
    }
}
