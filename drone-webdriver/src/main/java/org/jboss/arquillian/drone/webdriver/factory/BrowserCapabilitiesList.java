package org.jboss.arquillian.drone.webdriver.factory;

import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariOptions;

import java.util.Collections;
import java.util.Map;

/**
 * An internal mapping between browser capabilities property, implementation class and Capabilities. This class
 * also
 * supports implemenationClass property which is now legacy configuration value.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see Capabilities
 */
public class BrowserCapabilitiesList {

    public static class Capabilities {
        public static ChromeOptions CHROME = new ChromeOptions();
        public static EdgeOptions EDGE = new EdgeOptions();
        public static FirefoxOptions FIREFOX = new FirefoxOptions();
        public static InternetExplorerOptions IE = new InternetExplorerOptions();
        public static SafariOptions SAFARI = new SafariOptions();
    }

    public static class Chrome implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.chrome.ChromeDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return Capabilities.CHROME.asMap();
        }

        @Override
        public String getReadableName() {
            return "chrome";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    public static class Edge implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.edge.EdgeDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return Capabilities.EDGE.asMap();
        }

        @Override
        public String getReadableName() {
            return "edge";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    public static class Firefox implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.firefox.FirefoxDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return Capabilities.FIREFOX.asMap();
        }

        @Override
        public String getReadableName() {
            return "firefox";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    public static class HtmlUnit implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.jboss.arquillian.drone.webdriver.htmlunit.DroneHtmlUnitDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return Collections.emptyMap();
        }

        @Override
        public String getReadableName() {
            return "htmlunit";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    public static class InternetExplorer implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.ie.InternetExplorerDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return Capabilities.IE.asMap();
        }

        @Override
        public String getReadableName() {
            return "internetexplorer";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    public static class Remote implements BrowserCapabilities {

        @Override
        public String getReadableName() {
            return null;
        }

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.remote.RemoteWebDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return null;
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    public static class Safari implements BrowserCapabilities {

        @Override
        public String getReadableName() {
            return "safari";
        }

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.safari.SafariDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return Capabilities.SAFARI.asMap();
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    public static class ChromeHeadless implements BrowserCapabilities {
        @Override
        public String getReadableName() {
            return "chromeheadless";
        }

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.chrome.ChromeDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return Capabilities.CHROME.asMap();
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }
}
