package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.utils.HttpClient;

/**
 * {@link GoogleStorageSource} for Selenium binaries. Sets the url to selenium storage and provides useful methods.
 */
public abstract class SeleniumGoogleStorageSource extends GoogleStorageSource {

    public static final String SELENIUM_BASE_STORAGE_URL = "http://selenium-release.storage.googleapis.com/";

    public SeleniumGoogleStorageSource(HttpClient httpClient) {
        super(SELENIUM_BASE_STORAGE_URL, httpClient);
    }

    /**
     * Parses the full version of a selenium release and returns directory name the binaries should be stored in.
     *
     * @param version
     *     Full version of a selenium release
     *
     * @return A directory name (parsed from the given version) the binaries should be stored in.
     */
    protected String getDirectoryFromFullVersion(String version) {
        if (version.contains("-")) {
            int index = version.indexOf("-");
            String number = version.substring(0, index);
            return getShortNumber(number) + version.substring(index);
        }
        return getShortNumber(version);
    }

    private String getShortNumber(String fullNumber) {
        return fullNumber.substring(0, fullNumber.lastIndexOf("."));
    }
}
