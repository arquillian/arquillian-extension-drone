package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.SeleniumGoogleStorageSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.phantom.resolver.maven.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

/**
 * A class for handling driver binaries for internet explorer
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class InternetExplorerBinaryHandler extends AbstractBinaryHandler {

    public static final String IE_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.ie.driver";
    private static final String IE_DRIVER_BINARY_PROPERTY = "ieDriverBinary";
    private static final String IE_DRIVER_VERSION_PROPERTY = "ieDriverVersion";
    private static final String IE_DRIVER_URL_PROPERTY = "ieDriverUrl";

    private DesiredCapabilities capabilities;

    public InternetExplorerBinaryHandler(DesiredCapabilities capabilities){
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
        return new IeStorageSource((String) capabilities.getCapability(IE_DRIVER_VERSION_PROPERTY), new HttpClient()); // TODO rethink
    }

    @Override protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    @Override protected String getBinaryProperty() {
        return IE_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return IE_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    public File downloadAndPrepare() throws Exception {
        return super.downloadAndPrepare();
    }


    static class IeStorageSource extends SeleniumGoogleStorageSource {

        private String version;

        protected IeStorageSource(String version, HttpClient httpClient) {
            super(httpClient);
            this.version = version;
        }

        @Override
        protected String getExpectedKeyRegex(String requiredVersion, String directory) {
            StringBuffer regexBuffer = new StringBuffer("%s/IEDriverServer_");
            if (PlatformUtils.is32()) {
                regexBuffer.append("Win32");
            } else {
                regexBuffer.append("x64");
            }
            regexBuffer.append("_%s.zip");

            String regex;
            if (version == null) {
                regex = String.format(regexBuffer.toString(), directory, directory + ".*");
            } else {
                regex = String.format(regexBuffer.toString(), getDirectoryFromFullVersion(version), version);
            }
            return regex;
        }
    }
}
