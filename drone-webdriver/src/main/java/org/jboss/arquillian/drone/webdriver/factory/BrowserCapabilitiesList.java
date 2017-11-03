package org.jboss.arquillian.drone.webdriver.factory;

import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

/**
 * An internal mapping between browser capabilities property, implementation class and DesiredCapabilities. This class
 * also
 * supports implemenationClass property which is now legacy configuration value.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see DesiredCapabilities
 */
public class BrowserCapabilitiesList {

    public static class Chrome implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.chrome.ChromeDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return DesiredCapabilities.chrome().asMap();
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
            return DesiredCapabilities.edge().asMap();
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
            return DesiredCapabilities.firefox().asMap();
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
            return DesiredCapabilities.htmlUnit().asMap();
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
            return DesiredCapabilities.internetExplorer().asMap();
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

    public static class Opera implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.opera.OperaDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return DesiredCapabilities.operaBlink().asMap();
        }

        @Override
        public String getReadableName() {
            return "opera";
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
            return DesiredCapabilities.safari().asMap();
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    public static class PhantomJS implements BrowserCapabilities {

        @Override
        public String getReadableName() {
            return "phantomjs";
        }

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.phantomjs.PhantomJSDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return DesiredCapabilities.phantomjs().asMap();
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
            return DesiredCapabilities.chrome().asMap();
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }
}
