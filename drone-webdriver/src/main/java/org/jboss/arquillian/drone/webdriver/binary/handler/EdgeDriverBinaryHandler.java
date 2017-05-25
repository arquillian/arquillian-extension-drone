package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
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
            "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/";
        private static final String DRIVERS_LIST = ".driver-downloads li";
        private static final String INFO_PARAGRAPH = "p";
        private static final String LINK = "a";
        private static final String URL = "href";
        private static final int VERSION_POSITION = 1;
        private static final int LATEST_DRIVER = 0;
        private Logger log = Logger.getLogger(EdgeStorageSources.class.toString());

        @Override
        public ExternalBinary getLatestRelease() throws Exception {
            Element driver = getDriversList().get(LATEST_DRIVER);
            String driverVersion = getDriverVersion(driver);
            String webDriverUrl = getDriverUrl(driver);

            return new ExternalBinary(driverVersion, webDriverUrl);
        }

        @Override
        public ExternalBinary getReleaseForVersion(String version) throws Exception {

            String webDriverUrl = "";
            String driverVersion = "";

            Elements driversList = getDriversList();

            for (int driverItemNumber = 0; driverItemNumber < driversList.size(); driverItemNumber++) {
                Element driver = driversList.get(driverItemNumber);
                driverVersion = getDriverVersion(driver);

                if (driverVersion.equals(version)) {
                    webDriverUrl = getDriverUrl(driver);
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

        private Elements getDriversList() throws IOException {
            Document edgeWebDriversPage = Jsoup.connect(EDGE_WEB_DRIVERS_URL).get();
            return edgeWebDriversPage.select(DRIVERS_LIST);
        }

        private String getDriverVersion(Element driver) {
            return driver.select(INFO_PARAGRAPH).text().split(" ")[VERSION_POSITION];
        }

        private String getDriverUrl(Element driver) {
            return driver.select(LINK).attr(URL);
        }
    }
}
