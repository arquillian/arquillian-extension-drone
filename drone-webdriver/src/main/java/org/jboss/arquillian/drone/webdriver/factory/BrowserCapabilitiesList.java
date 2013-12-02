package org.jboss.arquillian.drone.webdriver.factory;

import java.util.Map;

import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * An internal mapping between browser capabilities property, implementation class and DesiredCapabilities. This class also
 * supports implemenationClass property which is now legacy configuration value.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see DesiredCapabilities
 */
public class BrowserCapabilitiesList {

    public static class Android implements BrowserCapabilities {
        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.android.AndroidDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return DesiredCapabilities.android().asMap();
        }

        @Override
        public String getReadableName() {
            return "android";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }

    };

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
    };

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
    };

    public static class HtmlUnit implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.htmlunit.HtmlUnitDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return DesiredCapabilities.htmlUnit().asMap();
        }

        @Override
        public String getReadableName() {
            return "htmlUnit";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    };

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
            return "internetExplorer";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    };

    public static class IPhone implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "org.openqa.selenium.iphone.IPhoneDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return DesiredCapabilities.iphone().asMap();
        }

        @Override
        public String getReadableName() {
            return "iphone";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }

    };

    public static class Opera implements BrowserCapabilities {

        @Override
        public String getImplementationClassName() {
            return "com.opera.core.systems.OperaDriver";
        }

        @Override
        public Map<String, ?> getRawCapabilities() {
            return DesiredCapabilities.opera().asMap();
        }

        @Override
        public String getReadableName() {
            return "opera";
        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    };

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

    };

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

    };

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

    };

}
