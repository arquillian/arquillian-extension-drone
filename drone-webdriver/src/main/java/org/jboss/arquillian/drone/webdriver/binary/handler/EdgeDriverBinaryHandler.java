package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.UrlStorageSource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.MissingBinaryException;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriverToDestroy;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.Capabilities;

/**
 * A class for handling binaries for Edge
 */
public class EdgeDriverBinaryHandler extends AbstractBinaryHandler {

    private static final Logger log = Logger.getLogger(ReusableRemoteWebDriverToDestroy.class.getName());

    private static final String EDGE_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.edge.driver";
    private static final String EDGE_DRIVER_BINARY_PROPERTY = "edgeDriverBinary";
    private static final String EDGE_DRIVER_VERSION_PROPERTY = "edgeDriverVersion";
    private static final String EDGE_DRIVER_URL_PROPERTY = "edgeDriverUrl";

    private Capabilities capabilities;

    public EdgeDriverBinaryHandler(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return EDGE_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return EDGE_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.Edge().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return EDGE_DRIVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return EDGE_DRIVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new EdgeDriverBinaryHandler.EdgeStorageSources("https://msedgedriver.azureedge.net/");
    }

    @Override
    protected Capabilities getCapabilities() {
        return capabilities;
    }

    public static class EdgeStorageSources extends UrlStorageSource {

        public EdgeStorageSources(String baseUrl) {
            this(baseUrl, new HttpClient());
        }

        public EdgeStorageSources(String baseUrl, HttpClient client) {
            super("Blob", "Name", baseUrl, "https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/LATEST_STABLE", client);
        }

        protected String getExpectedKeyRegex(String requiredVersion, String directory) {
            return Pattern.quote(requiredVersion + "/" + getFileNameRegexToDownload(requiredVersion));
        }

        public ExternalBinary getLatestRelease() throws Exception {
            return getLatestRelease("UTF-16");
        }

        @Override
        protected ExternalBinary getLatestRelease(String charset) throws Exception {
            ExternalBinary latestRelease;
            try {
             latestRelease = super.getLatestRelease(charset);
            } catch (MissingBinaryException e) {
                final String latestPlatformVersion = findLatestPlatformReleaseVersion(charset);
                log.log(Level.WARNING, "Failed downloading latest stable release. Reason: ", e);
                log.log(Level.WARNING, "Downloading version {0} instead.", latestPlatformVersion);
                latestRelease = getReleaseForVersion(latestPlatformVersion);
            }
            return latestRelease;
        }

        private String findLatestPlatformReleaseVersion(String charset) throws IOException {
            // It can happen that "LATEST_STABLE" has not been released for the given platform
            // In such a case roll back to the latest know release.
            // See https://github.com/arquillian/arquillian-extension-drone/issues/296
            final String latestVersion = getVersion(urlToLatestRelease, charset);
            final String majorVersion = latestVersion.split("\\.")[0];
            String latestPlatformRelease = "LATEST_RELEASE_" + majorVersion + "_";
            if (PlatformUtils.isMac()) {
                latestPlatformRelease += "MACOS";
            } else if (PlatformUtils.isWindows()) {
                latestPlatformRelease += "WINDOWS";
            } else if (PlatformUtils.isLinux()) {
                latestPlatformRelease += "LINUX";
            }
            return getVersion(urlToLatestRelease.replaceFirst("LATEST_STABLE", latestPlatformRelease), charset);
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            return getFileNameRegexToDownload(version, Architecture.AUTO_DETECT);
        }

        @Override
        public String getFileNameRegexToDownload(String version, Architecture architecture) {
            final StringBuilder fileName = new StringBuilder(version);
            fileName.append("/");
            fileName.append("edgedriver_");
            if (PlatformUtils.isMac()) {
                fileName.append("mac64");
            } else if (PlatformUtils.isWindows()) {
                fileName.append("win");
                fileName.append(architecture.getValue());
            } else if (PlatformUtils.isUnix()) {
                fileName.append("linux");
                fileName.append(architecture.getValue());
            }

            return fileName.append(".zip").toString();
        }
    }
}
