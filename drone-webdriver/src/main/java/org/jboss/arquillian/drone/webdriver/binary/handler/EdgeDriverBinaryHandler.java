package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.MissingBinaryException;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.XmlStorageSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriverToDestroy;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A class for handling binaries for Edge
 */
public class EdgeDriverBinaryHandler extends AbstractBinaryHandler {

    private static final Logger log = Logger.getLogger(ReusableRemoteWebDriverToDestroy.class.getName());

    private static final String EDGE_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.edge.driver";
    private static final String EDGE_DRIVER_BINARY_PROPERTY = "edgeDriverBinary";
    private static final String EDGE_DRIVER_VERSION_PROPERTY = "edgeDriverVersion";
    private static final String EDGE_DRIVER_URL_PROPERTY = "edgeDriverUrl";

    private DesiredCapabilities capabilities;

    public EdgeDriverBinaryHandler(DesiredCapabilities capabilities) {
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
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    private class EdgeStorageSources extends XmlStorageSource {

        EdgeStorageSources(String baseUrl) {
            super("Blob", "Name", baseUrl, "https://msedgewebdriverstorage.blob.core.windows.net/edgewebdriver/LATEST_STABLE", new HttpClient());
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
        protected NodeList getDriverEntries(Document doc) {
            return ((Element) doc.getFirstChild().getFirstChild()).getElementsByTagName(this.nodeName);
        }

        @Override
        protected String getLastModified(Element element) {
            return getContentOfFirstElement((Element) element.getElementsByTagName("Properties").item(0), "Last-Modified");
        }

        @Override
        protected String getLocation(Element element) {
            return getContentOfFirstElement(element, "Url");
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            final StringBuilder fileName = new StringBuilder("edgedriver_");
            if (PlatformUtils.isMac()) {
                fileName.append("mac64");
            } else if (PlatformUtils.isWindows()) {
                fileName.append("win");
                if (PlatformUtils.is32()) {
                    fileName.append("32");
                } else {
                    fileName.append("64");
                }
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
