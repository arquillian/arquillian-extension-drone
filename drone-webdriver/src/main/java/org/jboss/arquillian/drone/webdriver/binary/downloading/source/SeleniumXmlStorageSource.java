package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.utils.HttpClient;

/**
 * {@link XmlStorageSource} for Selenium binaries. Sets the url to selenium storage and provides useful methods.
 */
public abstract class SeleniumXmlStorageSource extends XmlStorageSource {

    public static final String SELENIUM_BASE_STORAGE_URL = "http://selenium-release.storage.googleapis.com/";

    public SeleniumXmlStorageSource(HttpClient httpClient) {
        super(SELENIUM_BASE_STORAGE_URL, httpClient);
    }

    protected String getDirectoryFromFullVersion(String version) {
        return getDirectoryFromFullVersion(version, true);
    }

    /**
     * Parses the full version of a selenium release and returns directory name the binaries should be stored in.
     *
     * @param version            Full version of a selenium release
     * @param includeFullVersion Flag whether full version should be included or not (f.e. 4.0-alpha-2)
     * @return A directory name (parsed from the given version) the binaries should be stored in.
     */
    protected String getDirectoryFromFullVersion(String version, boolean includeFullVersion) {
        if (version.contains("-")) {
            int index = version.indexOf("-");
            final String number = version.substring(0, index);
            return includeFullVersion ? getShortNumber(number) + version.substring(index) : getShortNumber(number);
        }
        return getShortNumber(version);
    }

    private String getShortNumber(String fullNumber) {
        return fullNumber.substring(0, fullNumber.lastIndexOf("."));
    }
}
