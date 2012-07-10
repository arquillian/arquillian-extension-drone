package org.jboss.arquillian.drone.webdriver.configuration;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * An internal mapping between browser capabilities property, implementation class and DesiredCapabilities. This class also
 * supports implemenationClass property which is now legacy configuration value.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see DesiredCapabilities
 */
enum CapabilityMap {

    ANDROID {
        @Override
        public String getImplementationClass() {
            return "org.openqa.selenium.android.AndroidDriver";
        }

        @Override
        public DesiredCapabilities getCapabilities() {
            return DesiredCapabilities.android();
        }

        @Override
        public String getReadableName() {
            return "android";
        }

        @Override
        public Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType() {
            return AndroidDriverConfiguration.class;
        }

    },
    CHROME {

        @Override
        public String getImplementationClass() {
            return "org.openqa.selenium.chrome.ChromeDriver";
        }

        @Override
        public DesiredCapabilities getCapabilities() {
            return DesiredCapabilities.chrome();
        }

        @Override
        public String getReadableName() {
            return "chrome";
        }

        @Override
        public Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType() {
            return ChromeDriverConfiguration.class;
        }

    },
    FIREFOX {

        @Override
        public String getImplementationClass() {
            return "org.openqa.selenium.firefox.FirefoxDriver";
        }

        @Override
        public DesiredCapabilities getCapabilities() {
            return DesiredCapabilities.firefox();
        }

        @Override
        public String getReadableName() {
            return "firefox";
        }

        @Override
        public Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType() {
            return FirefoxDriverConfiguration.class;
        }

    },
    HTMLUNIT {

        @Override
        public String getImplementationClass() {
            return "org.openqa.selenium.htmlunit.HtmlUnitDriver";
        }

        @Override
        public DesiredCapabilities getCapabilities() {
            return DesiredCapabilities.htmlUnit();
        }

        @Override
        public String getReadableName() {
            return "htmlUnit";
        }

        @Override
        public Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType() {
            return HtmlUnitDriverConfiguration.class;
        }

    },
    INTERNETEXPLORER {

        @Override
        public String getImplementationClass() {
            return "org.openqa.selenium.ie.InternetExplorerDriver";
        }

        @Override
        public DesiredCapabilities getCapabilities() {
            return DesiredCapabilities.internetExplorer();
        }

        @Override
        public String getReadableName() {
            return "internetExplorer";
        }

        @Override
        public Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType() {
            return InternetExplorerDriverConfiguration.class;
        }

    },
    IPHONE {

        @Override
        public String getImplementationClass() {
            return "org.openqa.selenium.iphone.IPhoneDriver";
        }

        @Override
        public DesiredCapabilities getCapabilities() {
            return DesiredCapabilities.iphone();
        }

        @Override
        public String getReadableName() {
            return "iphone";
        }

        @Override
        public Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType() {
            return IPhoneDriverConfiguration.class;
        }

    },
    OPERA {

        @Override
        public String getImplementationClass() {
            return "com.opera.core.systems.OperaDriver";
        }

        @Override
        public DesiredCapabilities getCapabilities() {
            return DesiredCapabilities.opera();
        }

        @Override
        public String getReadableName() {
            return "opera";
        }

        @Override
        public Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType() {
            return OperaDriverConfiguration.class;
        }

    },
    REMOTE {

        @Override
        public String getReadableName() {
            return null;
        }

        @Override
        public String getImplementationClass() {
            return "org.openqa.selenium.remote.RemoteWebDriver";
        }

        @Override
        public DesiredCapabilities getCapabilities() {
            return null;
        }

        @Override
        public Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType() {
            return RemoteReusableWebDriverConfiguration.class;
        }

    };

    /**
     * Gets a human readable name of the browser capabilities
     *
     * @return readable name
     */
    public abstract String getReadableName();

    /**
     * Gets an implementation class of given browser
     *
     * @return Implementation class
     */
    public abstract String getImplementationClass();

    /**
     * Gets desired capabilities of given browser
     *
     * @return desired capabilities
     */
    public abstract DesiredCapabilities getCapabilities();

    public abstract Class<? extends WebDriverConfigurationType> getWebDriverConfigurationType();

    /**
     * Returns a capability map based on desired capabilities
     *
     * @param capabilities Name of desired capabilities set
     * @return The capability map which reflects given browser
     */
    public static CapabilityMap byDesiredCapabilities(String capabilities) {

        if (capabilities == null || capabilities.length() == 0) {
            return null;
        }
        try {
            return CapabilityMap.valueOf(CapabilityMap.class, capabilities.toUpperCase());
        } catch (IllegalArgumentException e) {
            StringBuilder sb = new StringBuilder();
            for (CapabilityMap map : values()) {
                sb.append(map.getReadableName()).append(" ");
            }

            throw new IllegalArgumentException("Capabilities \"" + capabilities
                    + "\" are not supported. The list of supported browser via capabilities is: " + sb.toString());
        }
    }

    /**
     * Gets a capability map based on a implementation class
     *
     * @param implementationClass Name of implementation class
     * @return
     */
    public static CapabilityMap byImplementationClass(String implementationClass) {

        if (implementationClass == null || implementationClass.length() == 0) {
            return null;
        }

        return IMPLEMENTATION_CLASS_CACHE.get(implementationClass);
    }

    public static CapabilityMap byWebDriverConfigurationType(Class<? extends WebDriverConfigurationType> configurationType) {

        if (configurationType == null) {
            return null;
        }

        return CONFIGURATION_TYPE_CACHE.get(configurationType);
    }

    private static final Map<String, CapabilityMap> IMPLEMENTATION_CLASS_CACHE = new HashMap<String, CapabilityMap>() {
        private static final long serialVersionUID = 1L;
        {
            for (CapabilityMap map : CapabilityMap.values()) {
                this.put(map.getImplementationClass(), map);
            }
        }
    };

    private static final Map<Class<? extends WebDriverConfigurationType>, CapabilityMap> CONFIGURATION_TYPE_CACHE = new HashMap<Class<? extends WebDriverConfigurationType>, CapabilityMap>() {
        private static final long serialVersionUID = 1L;
        {
            for (CapabilityMap map : CapabilityMap.values()) {
                this.put(map.getWebDriverConfigurationType(), map);
            }
        }
    };

}
