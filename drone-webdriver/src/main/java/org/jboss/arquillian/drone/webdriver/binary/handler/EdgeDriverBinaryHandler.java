package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
        return new EdgeStorageSources();
    }

    @Override
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    private class EdgeStorageSources implements ExternalBinarySource {

        private static final String EDGE_WEB_DRIVERS_URL =
            "https://msedgedriver.azureedge.net/";
        private static final String DRIVERS_REGEX = "https:(.*)";
        private Logger log = Logger.getLogger(EdgeStorageSources.class.toString());

        @Override
        public ExternalBinary getLatestRelease() throws Exception {
            List<String> urlsList = getDriversList();
            String webDriverUrl = urlsList.get(urlsList.size() - 1);
            String driverVersion = getDriverVersion(webDriverUrl);

            if (webDriverUrl.isEmpty()) {
                log.warning("Could not find the latest release.");
            }

            return new ExternalBinary(driverVersion, webDriverUrl);
        }

        @Override
        public ExternalBinary getReleaseForVersion(String version) throws Exception {
            String webDriverUrl = "";
            String driverVersion = "";
            List<String> urls = getDriversList();

            for (int driverItemNumber = urls.size() - 1; driverItemNumber > 0; driverItemNumber--) {
                String url = urls.get(driverItemNumber);
                driverVersion = getDriverVersion(url);

                if (driverVersion.equals(version)) {
                    webDriverUrl = url;
                    break;
                }
            }

            if (webDriverUrl.isEmpty()) {
                log.warning("WebDriver with specified version was not found.");
            }

            return new ExternalBinary(driverVersion, webDriverUrl);
        }

        /**
         * This method returns only null as in the case of Edge webdriver, we don't know what will be the name of the file
         * that will be downloaded. As this method returns null, the AbstractBinaryHandler won't be looking for any file
         * in the cache directory and will jump to downloading phase.
         */
        public String getFileNameRegexToDownload(String version) {
            return null;
        }

        private List<String> getDriversList() throws Exception {
            List<String> urls = new ArrayList<>();
            String responseString = new HttpClient().get(EDGE_WEB_DRIVERS_URL).getPayload().trim();

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(responseString.substring(responseString.indexOf("<"))));
            Document doc = db.parse(is);
            NodeList contentNodes = ((Element) doc.getFirstChild()).getElementsByTagName("Url");

            for (int i = 0; i < contentNodes.getLength(); i++) {
                String url = contentNodes.item(i).getTextContent();
                String file = url.split("/")[url.split("/").length - 1];

                if ((PlatformUtils.isMac() && (file.contains("mac64") || file.contains("macos")))
                    || (PlatformUtils.isWindows() && file.contains("win")
                    && ((PlatformUtils.is32() && file.contains("win32"))
                    || (PlatformUtils.is64() && (file.contains("win64") || file.contains("windows")))))
                    || (PlatformUtils.isUnix() && file.contains("arm64"))) {
                    urls.add(url);
                }
            }

            return urls;
        }

        private String getDriverVersion(String driver) {
            String[] urlParts = driver.split("/");

            return urlParts[urlParts.length - (driver.toLowerCase().contains("latest") ? 1 : 2)];
        }
    }
}
