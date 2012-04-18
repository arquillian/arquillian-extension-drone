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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Configuration shared among all WebDriver implementations. This means that all configurations maps to the same namespace,
 * however user decides which configuration is chosen by requesting a type of driver in the test.
 *
 * Safely retrieves only the value compatible with current browser type.
 *
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see ArquillianDescriptor
 * @see org.jboss.arquillian.drone.configuration.ConfigurationMapper
 *
 */
public class TypedWebDriverConfiguration<T extends WebDriverConfigurationType> implements
        DroneConfiguration<TypedWebDriverConfiguration<T>>, AndroidDriverConfiguration, ChromeDriverConfiguration,
        FirefoxDriverConfiguration, HtmlUnitDriverConfiguration, InternetExplorerDriverConfiguration,
        IPhoneDriverConfiguration, WebDriverConfiguration {

    public static final String CONFIGURATION_NAME = "webdriver";

    // shared configuration
    protected Class<T> type;

    protected String implementationClass;

    // configuration holders for WebDriver Types
    protected int iePort;

    protected String applicationName;

    protected String applicationVersion;

    protected String userAgent;

    protected String firefoxProfile;

    protected String firefoxBinary;

    protected String chromeBinary;

    protected String chromeDriverBinary;

    protected String chromeSwitches;

    protected String remoteAddress;

    protected float browserVersionNumeric;

    protected boolean useJavaScript;

    protected String operaArguments;

    protected boolean operaAutostart = true;

    protected String operaBinary;

    protected int operaDisplay = -1;

    protected boolean operaIdle = false;

    protected String operaLauncher;

    protected String operaLoggingFile;

    protected String operaLoggingLevel = "INFO";

    protected int operaPort = 0;

    protected String operaProfile;

    protected String operaProduct;

    protected boolean operaQuit = true;

    protected boolean operaRestart = true;

    public TypedWebDriverConfiguration(Class<T> type, String implementationClass) {
        this.type = type;
        this.implementationClass = implementationClass;
    }

    @Override
    public String getImplementationClass() {
        return implementationClass;
    }

    @Override
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
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

    @Override
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
    public void setFirefoxBinary(final String firefoxBinary) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.firefoxBinary = firefoxBinary;
                return null;
            }
        };
        interceptor.intercept("setFirefoxBinary", String.class);

    }

    @Override
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
    public void setRemoteAddress(final String remoteAddress) {
        final CallInterceptor<Void> interceptor = new CallInterceptor<Void>() {
            @Override
            public Void invoke() {
                TypedWebDriverConfiguration.this.remoteAddress = remoteAddress;
                return null;
            }
        };
        interceptor.intercept("setRemoteAddress", String.class);
    }

    @Override
    public String getRemoteAddress() {
        final CallInterceptor<String> interceptor = new CallInterceptor<String>() {
            @Override
            public String invoke() {
                return remoteAddress;
            }
        };
        return interceptor.intercept("getRemoteAddress");
    }

    @Override
    public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }

    @Override
    public TypedWebDriverConfiguration<T> configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
        return ConfigurationMapper.fromSystemConfiguration(this, qualifier);
    }

    @Override
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

    private abstract class CallInterceptor<R> {
        private Boolean exists = null;

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

        public abstract R invoke();

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

        private String decamelize(String name) {
            StringBuilder sb = new StringBuilder(name.substring(3));
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            return sb.toString();
        }
    }

}
