package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.GitHubSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.phantom.resolver.maven.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * A class for handling chromeDriver binaries
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class FirefoxDriverBinaryHandler extends AbstractBinaryHandler {


    public static final String FIREFOX_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.gecko.driver";
    public static final String FIREFOX_DRIVER_BINARY_PROPERTY = "firefoxDriverBinary";
    private static final String FIREFOX_DRIVER_VERSION_PROPERTY = "firefoxDriverVersion";
    private static final String FIREFOX_DRIVER_URL_PROPERTY = "firefoxDriverUrl";

    private DesiredCapabilities capabilities;

    public FirefoxDriverBinaryHandler(DesiredCapabilities capabilities){
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
        return new GeckoDriverGitHubSource(new HttpClient()); // TODO improve design for testability
    }

    @Override protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    @Override protected String getBinaryProperty() {
        return FIREFOX_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return FIREFOX_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    private class GeckoDriverGitHubSource extends GitHubSource {

        GeckoDriverGitHubSource(HttpClient httpClient) {
            super("mozilla", "geckodriver", httpClient);
        }

        @Override
        protected String getExpectedFileNameRegex() {
            StringBuilder fileNameRegex = new StringBuilder("geckodriver-");
            fileNameRegex.append(getBinaryRelease().getVersion()).append("-");
            if (PlatformUtils.isMac()) {
                fileNameRegex.append("macos").toString();
            } else {
                if (PlatformUtils.isWindows() || PlatformUtils.isUnix()) {
                    if (PlatformUtils.isWindows()) {
                        fileNameRegex.append("win");
                    } else {
                        fileNameRegex.append("linux");
                    }
                    if (PlatformUtils.is32()) {
                        fileNameRegex.append("32").toString();
                    } else {
                        fileNameRegex.append("64").toString();
                    }
                } else {
                    fileNameRegex.append("arm7hf").toString();
                }
            }
            return fileNameRegex.append(".*").toString();
        }
    }
}
