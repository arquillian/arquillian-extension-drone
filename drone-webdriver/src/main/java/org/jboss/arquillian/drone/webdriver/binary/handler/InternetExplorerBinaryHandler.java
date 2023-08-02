package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.SeleniumXmlStorageSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.Capabilities;

import static org.jboss.arquillian.drone.webdriver.utils.Validate.empty;

/**
 * A class for handling driver binaries for internet explorer
 */
public class InternetExplorerBinaryHandler extends AbstractBinaryHandler {

    public static final String IE_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.ie.driver";
    private static final String IE_DRIVER_BINARY_PROPERTY = "ieDriverBinary";
    private static final String IE_DRIVER_VERSION_PROPERTY = "ieDriverVersion";
    private static final String IE_DRIVER_URL_PROPERTY = "ieDriverUrl";
    private static final String IE_DRIVER_ARCH_PROPERTY = "ieDriverArch";

    private static final String ARCH_32 = "Win32";
    private static final String ARCH_64 = "x64";

    private Capabilities capabilities;

    public InternetExplorerBinaryHandler(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.InternetExplorer().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return IE_DRIVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return IE_DRIVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new IeStorageSource(
            (String) capabilities.getCapability(IE_DRIVER_VERSION_PROPERTY),
            (String) capabilities.getCapability(IE_DRIVER_ARCH_PROPERTY),
            new HttpClient());
    }

    @Override
    protected Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return IE_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return IE_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    public File downloadAndPrepare() throws Exception {
        return super.downloadAndPrepare();
    }

    static class IeStorageSource extends SeleniumXmlStorageSource {

        private String version;
        private String architecture;

        protected IeStorageSource(String version, String architecture, HttpClient httpClient) {
            super(httpClient);
            this.version = version;

            if (!empty(architecture)) { // architecture is explicitly specified
                if (!architecture.equals(ARCH_32) && !architecture.equals(ARCH_64)) {
                    throw new InvalidArgumentException("Invalid value for \"" + IE_DRIVER_ARCH_PROPERTY + "\"; valid values are: " + ARCH_32 + ", " + ARCH_64);
                }
                this.architecture = architecture;
            } else if (PlatformUtils.is32()) { // architecture selection is based on the OS
                this.architecture = ARCH_32;
            } else {
                this.architecture = ARCH_64;
            }
        }

        @Override
        protected String getExpectedKeyRegex(String requiredVersion, String directory) {
            if (empty(version)) {
                return directory + "/" +  getFileNameRegexToDownload(directory + ".*");
            } else {
                return getDirectoryFromFullVersion(version) + "/" +  getFileNameRegexToDownload(version);
            }
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            final StringBuilder fileName = new StringBuilder("IEDriverServer_");
            fileName.append(architecture).append("_").append(version).append(".zip");
            return fileName.toString();
        }
    }
}
