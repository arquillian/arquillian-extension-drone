package org.jboss.arquillian.drone.webdriver.utils;

import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Assume;

public class ArqDescPropertyUtil {

    public static final String WEBDRIVER_REUSABLE_EXT = "webdriver-reusable";
    public static final String WEBDRIVER_REUSABLECOOKIES_EXT = "webdriver-reusecookies";
    public static final String WEBDRIVER_EXT = "webdriver";
    private static final Logger log = Logger.getLogger(ArqDescPropertyUtil.class.getName());

    public static String getBrowserProperty() {
        return getBrowserProperty(WEBDRIVER_EXT);
    }

    public static String getSeleniumServerVersionProperty() {
        return getSeleniumServerVersionProperty(WEBDRIVER_EXT);
    }

    public static String getSeleniumServerVersionProperty(String extensionName) {
        return getProperty("seleniumServerVersion", extensionName, "4.14.1");
    }

    public static String getBrowserProperty(String extensionName) {
        return getProperty("browser", extensionName, WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES).toLowerCase();
    }

    public static String getProperty(String property, String extensionName, String defaultValue) {
        String value = System.getProperty(property);

        if (Validate.empty(value)) {
            ExtensionDef webdriver = getArquillianDescriptor().extension(extensionName);
            Assert.assertNotNull(" extension should be defined in arquillian.xml", webdriver);
            value = webdriver.getExtensionProperties().get(property);

            if (Validate.empty(value) || value.startsWith("${")) {
                log.log(Level.INFO, "Property \"{0}\" was not specified, using default value of {1}",
                    new Object[] {property, defaultValue});
                value = defaultValue;
            }
        }

        return value;
    }

    public static void assumeBrowserNotEqual(String browserName) {
        Assume.assumeFalse(getBrowserProperty().equals(browserName));
    }

    public static void assumeBrowserEqual(String browserName) {
        Assume.assumeTrue(getBrowserProperty().equals(browserName));
    }

    public static ArquillianDescriptor getArquillianDescriptor() {
        return Descriptors.importAs(ArquillianDescriptor.class).fromStream(
            URLClassLoader.getSystemResourceAsStream("arquillian.xml"), true);
    }
}
