package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.util.regex.Pattern;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.XmlStorageSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
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
        protected NodeList getDriverEntries(Document doc) {
            return ((Element) doc.getFirstChild().getFirstChild()).getElementsByTagName(this.nodeName);
        }

        @Override
        protected String getLastModified(Element item) {
            return getContentOfFirstElement((Element) item.getElementsByTagName("Properties").item(0), "Last-Modified");
        }

        @Override
        protected String getLocation(String key, Element item) {
            return getContentOfFirstElement(item, "Url");
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            StringBuilder fileName = new StringBuilder("edgedriver_");
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
