package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.LocalBinarySource;
import org.openqa.selenium.Capabilities;

public class LocalBinaryHandler extends AbstractBinaryHandler {

    public static final String LOCAL_SOURCE_BINARY_PROPERTY = "localSourceBinary";
    public static final String LOCAL_SOURCE_SYSTEM_BINARY_PROPERTY = "system.local.source.binary";
    public static final String LOCAL_SOURCE_BINARY_VERSION_PROPERTY = "localSourceVersionBinary";
    public static final String LOCAL_SOURCE_BINARY_URL_PROPERTY = "localSourceUrlBinary";
    public static final String LOCAL_SOURCE_CACHE_SUBDIR = "local-source";

    private Capabilities capabilities;

    public LocalBinaryHandler(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return LOCAL_SOURCE_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return LOCAL_SOURCE_SYSTEM_BINARY_PROPERTY;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return LOCAL_SOURCE_CACHE_SUBDIR;
    }

    @Override
    protected String getDesiredVersionProperty() {
        return LOCAL_SOURCE_BINARY_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return LOCAL_SOURCE_BINARY_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new LocalBinarySource();
    }

    @Override
    protected Capabilities getCapabilities() {
        return capabilities;
    }
}
