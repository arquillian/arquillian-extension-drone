package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.openqa.selenium.Capabilities;

/**
 * A class for handling binaries for Opera
 * <br/>
 * <b>Not fully implemented - downloading is not supported so far</b>
 */
public class OperaDriverBinaryHandler extends AbstractBinaryHandler {

    private static final String OPERA_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.opera.driver";
    private static final String OPERA_DRIVER_BINARY_PROPERTY = "operaDriverBinary";
    private static final String OPERA_DRIVER_VERSION_PROPERTY = "operaDriverVersion";
    private static final String OPERA_DRIVER_URL_PROPERTY = "operaDriverUrl";

    private Capabilities capabilities;

    public OperaDriverBinaryHandler(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return OPERA_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return OPERA_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.Opera().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return OPERA_DRIVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return OPERA_DRIVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return null;
    }

    @Override
    protected Capabilities getCapabilities() {
        return capabilities;
    }
}
