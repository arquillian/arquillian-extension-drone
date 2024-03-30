package org.jboss.arquillian.drone.webdriver.utils;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;

public class ChromeUtils {

    public static int CHROME_FOR_TESTING_MIN_VERSION = 115;

    public static String getChromeVersion(Capabilities capabilities) {
        return (String) capabilities.getCapability("chromeDriverVersion");
    }

    public static boolean isChromeForTesting(String chromeVersion) {
        if (StringUtils.isBlank(chromeVersion)) {
            // we consider the Chrome for Testing as the default
            return true;
        }

        try {
            return Integer.parseInt(chromeVersion.substring(0, chromeVersion.indexOf('.'))) >= CHROME_FOR_TESTING_MIN_VERSION;
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.format("Cannot parse chrome version '%s'", chromeVersion), e);
        }
    }
}
