/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.drone.webdriver.configuration;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Configuration shared among all WebDriver implementations. This means that all configurations maps to the same namespace,
 * however user decides which configuration is chosen by requesting a type of driver in the test.
 *
 * Safely retrieves only the value compatible with current browser type.
 *
 * When user uses <code>@Drone WebDriver drive</code> in the code, the configurator doesn't know the implementation class, so it
 * has to provide an object implementing all possible configuration interfaces. Afterwards this object is passed as an argument
 * to the proper factory method.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 * @see ArquillianDescriptor
 * @see org.jboss.arquillian.drone.configuration.ConfigurationMapper
 *
 */
public class TypedWebDriverConfiguration<T extends WebDriverConfigurationType> implements
        DroneConfiguration<TypedWebDriverConfiguration<T>>, AndroidDriverConfiguration, ChromeDriverConfiguration,
        FirefoxDriverConfiguration, HtmlUnitDriverConfiguration, InternetExplorerDriverConfiguration,
        IPhoneDriverConfiguration, WebDriverConfiguration, RemoteReusableWebDriverConfiguration {

    private abstract class CallInterceptor<R> {
        private Boolean exists = null;

        private String decamelize(String name) {
            int prefixLength = name.startsWith("is") ? 2 : 3;
            StringBuilder sb = new StringBuilder(name.substring(prefixLength));
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            return sb.toString();
        }

        public R intercept(String name, Class<?>... parameterTypes) {
            if (methodExists(name, parameterTypes)) {
                return invoke();
            }
            // this should never happen in the implementation
            else {
                String propertyName = decamelize(name);
                String configurationName = type.getSimpleName().replaceAll("ConfigurationType", "");
                throw new IllegalStateException("Property with name " + propertyName + " is not valid for " + configurationName
                        + ". Make sure you are using proper configuration element for the WebDriver browser type");
            }
        }

        public abstract R invoke();

        private boolean methodExists(String name, Class<?>[] parameterTypes) {

            if (exists != null) {
                return exists;
            }
            // get method in the interface
            try {
                type.getMethod(name, parameterTypes);
                this.exists = true;
            } catch (NoSuchMethodException e) {
                this.exists = false;
            }

            return exists;
        }
    }

    private static final Logger log = Logger.getLogger(TypedWebDriverConfiguration.class.getName());

    public static final String CONFIGURATION_NAME = "webdriver";

    public static URL DEFAULT_REMOTE_URL;
    static {
        try {
            DEFAULT_REMOTE_URL = new URL("http://localhost:14444/wd/hub");
        } catch (MalformedURLException e) {
            // ignore invalid url exception
        }
    }

    public static final String DEFAULT_BROWSER_CAPABILITIES = CapabilityMap.HTMLUNIT.getReadableName();

    // shared configuration
    protected Class<T> type;

    @Deprecated
    protected String implementationClass;

    // configuration holders for WebDriver Types
    protected int iePort;

    @Deprecated
    protected String applicationName;

    @Deprecated
    protected String applicationVersion;

    @Deprecated
    protected String userAgent;

    @Deprecated
    protected String firefoxProfile;

    @Deprecated
    protected String firefoxBinary;

    @Deprecated
    protected String chromeBinary;

    protected String chromeDriverBinary;

    @Deprecated
    protected String chromeSwitches;

    protected URL remoteAddress;

    @Deprecated
    protected float browserVersionNumeric;

    @Deprecated
    protected boolean useJavaScript = true;

    @Deprecated
    protected String operaArguments;

    @Deprecated
    protected boolean operaAutostart = true;

    @Deprecated
    protected String operaBinary;

    @Deprecated
    protected int operaDisplay = -1;

    @Deprecated
    protected boolean operaIdle = false;

    @Deprecated
    protected String operaLauncher;

    @Deprecated
    protected String operaLoggingFile;

    @Deprecated
    protected String operaLoggingLevel = "INFO";

    @Deprecated
    protected int operaPort = 0;

    @Deprecated
    protected String operaProfile;

    @Deprecated
    protected String operaProduct;

    @Deprecated
    protected boolean operaQuit = true;

    @Deprecated
    protected boolean operaRestart = true;

    protected Map<String, Object> capabilityMap;

    protected String browserCapabilities;

    // ARQ-1022
    protected String originalBrowserCapabilities;

    protected boolean remoteReusable;

    protected boolean remote;

    public TypedWebDriverConfiguration(Class<T> type) {
        this.type = type;
        CapabilityMap capabilityMap = CapabilityMap.byWebDriverConfigurationType(type);
        if (capabilityMap != null) {
            this.originalBrowserCapabilities = capabilityMap.getReadableName();
            this.browserCapabilities = originalBrowserCapabilities;
        }
    }

    @Deprecated
    public TypedWebDriverConfiguration(Class<T> type, String implementationClass) {
        this.type = type;
        this.implementationClass = implementationClass;
    }

    @Override
    public TypedWebDriverConfiguration<T> configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
        ConfigurationMapper.fromSystemConfiguration(this, qualifier);

        // ARQ-1022, we need to check if we haven't overriden original browser capabilities in an incompatible way
        if (originalBrowserCapabilities != null && !originalBrowserCapabilities.equals(this.browserCapabilities)) {
            log.log(Level.WARNING,
                    "Arquillian configuration is specifying a Drone of type {0}, however test class specifically asked for {1}. As Drone cannot guarantee that those two are compatible, Arquillian configuration will be ignored.",
                    new Object[] { browserCapabilities, originalBrowserCapabilities });
            this.browserCapabilities = originalBrowserCapabilities;
        }
        return this;
    }

    @Override
    @Deprecated
    public String getApplicationName() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return applicationName;
            }
        };
        return interceptor.intercept("getApplicationName");
    }

    @Override
    @Deprecated
    public String getApplicationVersion() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return applicationVersion;
            }
        };
        return interceptor.intercept("getApplicationVersion");
    }

    @Override
    public String getBrowserCapabilities() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                CapabilityMap capabilityMap = CapabilityMap.byWebDriverConfigurationType(type);
                if (capabilityMap != null && capabilityMap.getReadableName() != null) {
                    return capabilityMap.getReadableName();
                }
                return TypedWebDriverConfiguration.this.browserCapabilities;
            }
        };
        return interceptor.intercept("getBrowserCapabilities");
    }

    @Override
    @Deprecated
    public float getBrowserVersionNumeric() {
        final CallInterceptor<Float> interceptor = new CallInterceptor<Float>() {
            @Override
            public Float invoke() {
                return browserVersionNumeric;
            }
        };
        return interceptor.intercept("getBrowserVersionNumeric");
    }

    @Override
    public Capabilities getCapabilities() {

        final CallInterceptor<Capabilities> interceptor = new CallInterceptor<Capabilities>() {
            @Override
            public Capabilities invoke() {
                CapabilityMap capabilityMap = CapabilityMap.byImplementationClass(getImplementationClass());
                DesiredCapabilities merged = new DesiredCapabilities();
                if (capabilityMap != null) {
                    merged = new DesiredCapabilities(capabilityMap.getCapabilities());
                }

                merged = new DesiredCapabilities(merged,
                        new DesiredCapabilities(TypedWebDriverConfiguration.this.capabilityMap));

                return merged;
            }
        };
        return interceptor.intercept("getCapabilities");

    }

    @Override
    @Deprecated
    public String getChromeBinary() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return chromeBinary;
            }
        };
        return interceptor.intercept("getChromeBinary");
    }

    @Override
    public String getChromeDriverBinary() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return chromeDriverBinary;
            }
        };
        return interceptor.intercept("getChromeDriverBinary");
    }

    @Override
    @Deprecated
    public String getChromeSwitches() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return chromeSwitches;
            }
        };
        return interceptor.intercept("getChromeSwitches");
    }

    @Override
    public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }

    @Override
    @Deprecated
    public String getFirefoxBinary() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return firefoxBinary;
            }
        };
        return interceptor.intercept("getFirefoxBinary");
    }

    @Override
    @Deprecated
    public String getFirefoxProfile() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return firefoxProfile;
            }
        };
        return interceptor.intercept("getFirefoxProfile");
    }

    @Override
    public int getIePort() {
        final CallInterceptor<Integer> interceptor = new CallInterceptor<Integer>() {
            @Override
            public Integer invoke() {
                return iePort;
            }
        };
        return interceptor.intercept("getIePort");
    }

    @Override
    public String getImplementationClass() {

        String browserCapabilities = getBrowserCapabilities();
        @SuppressWarnings("deprecation")
        String implementationClassName = this.implementationClass;
        CapabilityMap capabilityMap = CapabilityMap.byImplementationClass(implementationClassName);
        if (capabilityMap == null) {
            capabilityMap = CapabilityMap.byDesiredCapabilities(browserCapabilities);
        }

        // get real implementation class value
        if (implementationClassName == null && capabilityMap != null) {
            implementationClassName = capabilityMap.getImplementationClass();
        }

        return implementationClassName;
    }

    @Override
    @Deprecated
    public String getOperaArguments() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return operaArguments;
            }
        };
        return interceptor.intercept("getOperaArguments");
    }

    @Override
    @Deprecated
    public String getOperaBinary() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return operaBinary;
            }
        };
        return interceptor.intercept("getOperaBinary");
    }

    @Override
    @Deprecated
    public int getOperaDisplay() {
        final CallInterceptor<Integer> interceptor = new CallInterceptor<Integer>() {
            @Override
            public Integer invoke() {
                return operaDisplay;
            }
        };
        return interceptor.intercept("getOperaDisplay");
    }

    @Override
    @Deprecated
    public String getOperaLauncher() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return operaLauncher;
            }
        };
        return interceptor.intercept("getOperaLauncher");
    }

    @Override
    @Deprecated
    public String getOperaLoggingFile() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return operaLoggingFile;
            }
        };
        return interceptor.intercept("getOperaLoggingFile");
    }

    @Override
    @Deprecated
    public String getOperaLoggingLevel() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return operaLoggingLevel;
            }
        };
        return interceptor.intercept("getOperaLoggingLevel");
    }

    @Override
    @Deprecated
    public int getOperaPort() {
        final CallInterceptor<Integer> interceptor = new CallInterceptor<Integer>() {
            @Override
            public Integer invoke() {
                return operaPort;
            }
        };
        return interceptor.intercept("getOperaPort");
    }

    @Override
    @Deprecated
    public String getOperaProduct() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return operaProduct;
            }
        };
        return interceptor.intercept("getOperaProduct");
    }

    @Override
    @Deprecated
    public String getOperaProfile() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return operaProfile;
            }
        };
        return interceptor.intercept("getOperaProfile");
    }

    @Override
    public URL getRemoteAddress() {
        final CallInterceptor<URL> interceptor = new CallInterceptor<URL>() {
            @Override
            public URL invoke() {
                return remoteAddress;
            }
        };
        return interceptor.intercept("getRemoteAddress");
    }

    @Override
    @Deprecated
    public String getUserAgent() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return userAgent;
            }
        };
        return interceptor.intercept("getUserAgent");
    }

    @Override
    @Deprecated
    public boolean isOperaAutostart() {
        final CallInterceptor<Boolean> interceptor = new CallInterceptor<Boolean>() {
            @Override
            public Boolean invoke() {
                return operaAutostart;
            }
        };
        return interceptor.intercept("isOperaAutostart");
    }

    @Override
    @Deprecated
    public boolean isOperaIdle() {
        final CallInterceptor<Boolean> interceptor = new CallInterceptor<Boolean>() {
            @Override
            public Boolean invoke() {
                return operaIdle;
            }
        };
        return interceptor.intercept("isOperaIdle");
    }

    @Override
    @Deprecated
    public boolean isOperaQuit() {
        final CallInterceptor<Boolean> interceptor = new CallInterceptor<Boolean>() {
            @Override
            public Boolean invoke() {
                return operaQuit;
            }
        };
        return interceptor.intercept("isOperaQuit");
    }

    @Override
    @Deprecated
    public boolean isOperaRestart() {
        final CallInterceptor<Boolean> interceptor = new CallInterceptor<Boolean>() {
            @Override
            public Boolean invoke() {
                return operaRestart;
            }
        };
        return interceptor.intercept("isOperaRestart");
    }

    @Override
    public boolean isRemote() {
        final CallInterceptor<Boolean> interceptor = new CallInterceptor<Boolean>() {
            @Override
            public Boolean invoke() {
                return TypedWebDriverConfiguration.this.remote;
            }
        };
        return interceptor.intercept("isRemote");
    }

    @Override
    public boolean isRemoteReusable() {
        final CallInterceptor<Boolean> interceptor = new CallInterceptor<Boolean>() {
            @Override
            public Boolean invoke() {
                return TypedWebDriverConfiguration.this.remoteReusable;
            }
        };
        return interceptor.intercept("isRemoteReusable");
    }

    @Override
    @Deprecated
    public boolean isUseJavaScript() {
        final CallInterceptor<Boolean> interceptor = new CallInterceptor<Boolean>() {
            @Override
            public Boolean invoke() {
                return useJavaScript;
            }
        };
        return interceptor.intercept("isUseJavaScript");
    }

    @Override
    @Deprecated
    public void setApplicationName(final String applicationName) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.applicationName = applicationName;
                return null;
            }
        };
        interceptor.intercept("setApplicationName", String.class);

    }

    @Override
    @Deprecated
    public void setApplicationVersion(final String applicationVersion) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.applicationVersion = applicationVersion;
                return null;
            }
        };
        interceptor.intercept("setApplicationVersion", String.class);
    }

    @Override
    public void setBrowserCapabilities(final String browserCapabilities) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.browserCapabilities = browserCapabilities;
                return null;
            }
        };
        interceptor.intercept("setBrowserCapabilities", String.class);
    }

    @Override
    @Deprecated
    public void setBrowserVersionNumeric(final float browserVersionNumeric) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.browserVersionNumeric = browserVersionNumeric;
                return null;
            }
        };
        interceptor.intercept("setBrowserVersionNumeric", float.class);
    }

    @Override
    @Deprecated
    public void setChromeBinary(final String chromeBinary) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.chromeBinary = chromeBinary;
                return null;
            }
        };
        interceptor.intercept("setChromeBinary", String.class);
    }

    @Override
    public void setChromeDriverBinary(final String chromeDriverBinary) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.chromeDriverBinary = chromeDriverBinary;
                return null;
            }
        };
        interceptor.intercept("setChromeDriverBinary", String.class);
    }

    @Override
    @Deprecated
    public void setChromeSwitches(final String chromeSwitches) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.chromeSwitches = chromeSwitches;
                return null;
            }
        };
        interceptor.intercept("setChromeSwitches", String.class);
    }

    @Override
    @Deprecated
    public void setFirefoxBinary(final String firefoxBinary) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            public Void invoke() {
                TypedWebDriverConfiguration.this.firefoxBinary = firefoxBinary;
                return null;
            }
        };
        interceptor.intercept("setFirefoxBinary", String.class);

    }

    @Override
    @Deprecated
    public void setFirefoxProfile(final String firefoxProfile) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.firefoxProfile = firefoxProfile;
                return null;
            }
        };
        interceptor.intercept("setFirefoxProfile", String.class);

    }

    @Override
    public void setIePort(final int iePort) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.iePort = iePort;
                return null;
            }
        };
        interceptor.intercept("setIePort", int.class);

    }

    @Deprecated
    @Override
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    @Override
    @Deprecated
    public void setOperaArguments(final String operaArguments) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaArguments = operaArguments;
                return null;
            }
        };
        interceptor.intercept("setOperaArguments", String.class);
    }

    @Override
    @Deprecated
    public void setOperaAutostart(final boolean operaAutostart) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaAutostart = operaAutostart;
                return null;
            }
        };
        interceptor.intercept("setOperaAutostart", int.class);
    }

    @Override
    @Deprecated
    public void setOperaBinary(final String operaBinary) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaBinary = operaBinary;
                return null;
            }
        };
        interceptor.intercept("setOperaBinary", String.class);
    }

    @Override
    @Deprecated
    public void setOperaDisplay(final int operaDisplay) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaDisplay = operaDisplay;
                return null;
            }
        };
        interceptor.intercept("setOperaDisplay", int.class);
    }

    @Override
    @Deprecated
    public void setOperaIdle(final boolean operaIdle) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaIdle = operaIdle;
                return null;
            }
        };
        interceptor.intercept("setOperaIdle", boolean.class);
    }

    @Override
    @Deprecated
    public void setOperaLauncher(final String operaLauncher) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaLauncher = operaLauncher;
                return null;
            }
        };
        interceptor.intercept("setOperaLauncher", String.class);
    }

    @Override
    @Deprecated
    public void setOperaLoggingFile(final String operaLoggingFile) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaLoggingFile = operaLoggingFile;
                return null;
            }
        };
        interceptor.intercept("setOperaLoggingFile", String.class);
    }

    @Override
    @Deprecated
    public void setOperaLoggingLevel(final String operaLoggingLevel) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaLoggingLevel = operaLoggingLevel;
                return null;
            }
        };
        interceptor.intercept("setOperaLoggingLevel", String.class);
    }

    @Override
    @Deprecated
    public void setOperaPort(final int operaPort) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaPort = operaPort;
                return null;
            }
        };
        interceptor.intercept("setOperaPort", int.class);
    }

    @Override
    @Deprecated
    public void setOperaProduct(final String operaProduct) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaProduct = operaProduct;
                return null;
            }
        };
        interceptor.intercept("setOperaProduct", String.class);
    }

    @Override
    @Deprecated
    public void setOperaProfile(final String operaProfile) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaProfile = operaProfile;
                return null;
            }
        };
        interceptor.intercept("setOperaProfile", String.class);
    }

    @Override
    @Deprecated
    public void setOperaQuit(final boolean operaQuit) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaQuit = operaQuit;
                return null;
            }
        };
        interceptor.intercept("setOperaQuit", boolean.class);
    }

    @Override
    @Deprecated
    public void setOperaRestart(final boolean operaRestart) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.operaRestart = operaRestart;
                return null;
            }
        };
        interceptor.intercept("setOperaRestart", boolean.class);
    }

    @Override
    public void setRemote(final boolean remote) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.remote = remote;
                return null;
            }
        };
        interceptor.intercept("setRemote", boolean.class);
    }

    @Override
    public void setRemoteAddress(final URL remoteAddress) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.remoteAddress = remoteAddress;
                return null;
            }
        };
        interceptor.intercept("setRemoteAddress", URL.class);
    }

    @Override
    public void setRemoteReusable(final boolean remoteReusable) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.remoteReusable = remoteReusable;
                return null;
            }
        };
        interceptor.intercept("setRemoteReusable", boolean.class);
    }

    @Override
    @Deprecated
    public void setUseJavaScript(final boolean useJavaScript) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.useJavaScript = useJavaScript;
                return null;
            }
        };
        interceptor.intercept("setUseJavaScript", boolean.class);
    }

    @Override
    @Deprecated
    public void setUserAgent(final String userAgent) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.userAgent = userAgent;
                return null;
            }
        };
        interceptor.intercept("setUserAgent", String.class);

    }

}