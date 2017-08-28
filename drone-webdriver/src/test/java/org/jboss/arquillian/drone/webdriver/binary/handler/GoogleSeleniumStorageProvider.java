package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;

/**
 *
 */
public class GoogleSeleniumStorageProvider {

    public static ExternalBinarySource getIeStorageSource(String version, HttpClient httpClient) {
        return new InternetExplorerBinaryHandler.IeStorageSource(version, null, httpClient);
    }

    public static ExternalBinarySource getSeleniumServerStorageSource(String version, HttpClient httpClient) {
        return new SeleniumServerBinaryHandler.SeleniumServerStorage(version, httpClient);
    }
}
