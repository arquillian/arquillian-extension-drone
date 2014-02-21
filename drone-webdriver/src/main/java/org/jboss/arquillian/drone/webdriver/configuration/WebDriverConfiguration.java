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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Generic configuration for WebDriver Driver. By default, it uses HtmlUnit Driver.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class WebDriverConfiguration implements DroneConfiguration<WebDriverConfiguration> {

    private static final Logger log = Logger.getLogger(WebDriverConfiguration.class.getName());

    public static final String CONFIGURATION_NAME = "webdriver";

    public static URL DEFAULT_REMOTE_URL;
    static {
        try {
            DEFAULT_REMOTE_URL = new URL("http://localhost:14444/wd/hub");
        } catch (MalformedURLException e) {
            // ignore invalid url exception
        }
    }

    public static final String DEFAULT_BROWSER_CAPABILITIES = new BrowserCapabilitiesList.HtmlUnit().getReadableName();

    @Deprecated
    private String implementationClass;

    private int iePort;

    private String ieDriverBinary;

    @Deprecated
    private String applicationName;

    @Deprecated
    private String applicationVersion;

    @Deprecated
    private String userAgent;

    @Deprecated
    private String firefoxProfile;

    @Deprecated
    private String firefoxBinary;

    @Deprecated
    private String chromeBinary;

    private String chromeDriverBinary;

    @Deprecated
    private String chromeSwitches;

    private URL remoteAddress;

    @Deprecated
    private float browserVersionNumeric;

    @Deprecated
    private boolean useJavaScript = true;

    @Deprecated
    private String operaArguments;

    @Deprecated
    private boolean operaAutostart = true;

    @Deprecated
    private String operaBinary;

    @Deprecated
    private int operaDisplay = -1;

    @Deprecated
    private boolean operaIdle = false;

    @Deprecated
    private String operaLauncher;

    @Deprecated
    private String operaLoggingFile;

    @Deprecated
    private String operaLoggingLevel = "INFO";

    @Deprecated
    private int operaPort = 0;

    @Deprecated
    private String operaProfile;

    @Deprecated
    private String operaProduct;

    @Deprecated
    private boolean operaQuit = true;

    @Deprecated
    private boolean operaRestart = true;

    private String browser;

    @Deprecated
    private String browserCapabilities;

    private boolean remoteReusable;

    private boolean remote;

    // ARQ-1206, ability to delete all cookies in reused browsers
    private boolean reuseCookies;

    private Map<String, Object> capabilityMap;

    // internal variables
    // ARQ-1022
    private String _originalBrowser;

    private BrowserCapabilities _browser;

    private String dimensions;

    public WebDriverConfiguration(BrowserCapabilities browser) {
        if (browser != null) {
            this._browser = browser;
            this._originalBrowser = browser.getReadableName();
            this.browser = _originalBrowser;
        }
    }

    public void setBrowserInternal(BrowserCapabilities browser) {
        if (browser != null) {
            this._browser = browser;
            this.browser = browser.getReadableName();
        }
    }

    @Deprecated
    public WebDriverConfiguration(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    @Override
    public WebDriverConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
        ConfigurationMapper.fromSystemConfiguration(this, qualifier);

        // ARQ-1022, we need to check if we haven't overriden original browser
        // capabilities in an incompatible way
        if (_originalBrowser != null && !_originalBrowser.equals(this.browser)) {
            log.log(Level.WARNING,
                    "Arquillian configuration is specifying a Drone of type {0}, however test class specifically asked for {1}. As Drone cannot guarantee that those two are compatible, \"browser\" property will be set to {1}.",
                    new Object[] { browser, _originalBrowser });
            this.browser = _originalBrowser;
        }
        return this;
    }

    @Deprecated
    public String getApplicationName() {
        return applicationName;
    }

    @Deprecated
    public String getApplicationVersion() {
        return applicationVersion;
    }

    @Deprecated
    public String getBrowserCapabilities() {

        // if we have created a browser capability object by using a specific factory, e.g. AndroidDriverFactory, ignore value
        // specified by end user
        if (_browser != null) {
            return _browser.getReadableName();
        }

        return browserCapabilities;
    }

    public String getBrowser() {

        if (_browser != null) {
            return _browser.getReadableName();
        }

        return browser;
    }

    @Deprecated
    public float getBrowserVersionNumeric() {
        return browserVersionNumeric;
    }

    public Capabilities getCapabilities() {
        // return a merge of original capability plus capabilities user has specified in configuration
        // safely ignore null value here
        return new DesiredCapabilities(new DesiredCapabilities(
                _browser.getRawCapabilities() == null ? new HashMap<String, Object>()
                        : _browser.getRawCapabilities()),
                new DesiredCapabilities(this.capabilityMap));
    }

    @Deprecated
    public String getChromeBinary() {
        return chromeBinary;
    }

    public String getChromeDriverBinary() {
        return chromeDriverBinary;
    }

    @Deprecated
    public String getChromeSwitches() {
        return chromeSwitches;
    }

    @Override
    public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }

    @Deprecated
    public String getFirefoxBinary() {
        return firefoxBinary;
    }

    @Deprecated
    public String getFirefoxProfile() {
        return firefoxProfile;
    }

    public int getIePort() {
        return iePort;
    }

    public String getIeDriverBinary() {
        return ieDriverBinary;
    }

    public String getImplementationClass() {

        String implementationClassName = this.implementationClass;

        // get real implementation class value
        if (implementationClassName == null && _browser != null) {
            implementationClassName = _browser.getImplementationClassName();
        }

        return implementationClassName;
    }

    @Deprecated
    public String getOperaArguments() {
        return operaArguments;
    }

    @Deprecated
    public String getOperaBinary() {
        return operaBinary;
    }

    @Deprecated
    public int getOperaDisplay() {
        return operaDisplay;
    }

    @Deprecated
    public String getOperaLauncher() {
        return operaLauncher;
    }

    @Deprecated
    public String getOperaLoggingFile() {
        return operaLoggingFile;
    }

    @Deprecated
    public String getOperaLoggingLevel() {
        return operaLoggingLevel;
    }

    @Deprecated
    public int getOperaPort() {
        return operaPort;
    }

    @Deprecated
    public String getOperaProduct() {
        return operaProduct;
    }

    @Deprecated
    public String getOperaProfile() {
        return operaProfile;
    }

    public URL getRemoteAddress() {
        return remoteAddress;
    }

    @Deprecated
    public String getUserAgent() {
        return userAgent;
    }

    @Deprecated
    public boolean isOperaAutostart() {
        return operaAutostart;
    }

    @Deprecated
    public boolean isOperaIdle() {
        return operaIdle;
    }

    @Deprecated
    public boolean isOperaQuit() {
        return operaQuit;
    }

    @Deprecated
    public boolean isOperaRestart() {
        return operaRestart;
    }

    public boolean isRemote() {
        return remote;
    }

    public boolean isRemoteReusable() {
        return remoteReusable;
    }

    @Deprecated
    public boolean isUseJavaScript() {
        return useJavaScript;
    }

    public String getDimensions() {
        return dimensions;
    }

    @Deprecated
    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    @Deprecated
    public void setApplicationVersion(final String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Deprecated
    public void setBrowserCapabilities(final String browserCapabilities) {
        this.browserCapabilities = browserCapabilities;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    @Deprecated
    public void setBrowserVersionNumeric(final float browserVersionNumeric) {
        this.browserVersionNumeric = browserVersionNumeric;
    }

    @Deprecated
    public void setChromeBinary(final String chromeBinary) {
        this.chromeBinary = chromeBinary;
    }

    public void setChromeDriverBinary(final String chromeDriverBinary) {
        this.chromeDriverBinary = chromeDriverBinary;
    }

    @Deprecated
    public void setChromeSwitches(final String chromeSwitches) {
        this.chromeSwitches = chromeSwitches;
    }

    @Deprecated
    public void setFirefoxBinary(final String firefoxBinary) {
        this.firefoxBinary = firefoxBinary;
    }

    @Deprecated
    public void setFirefoxProfile(final String firefoxProfile) {
        this.firefoxProfile = firefoxProfile;
    }

    public void setIePort(final int iePort) {
        this.iePort = iePort;
    }

    public void setIeDriverBinary(String ieDriverBinary) {
        this.ieDriverBinary = ieDriverBinary;
    }

    @Deprecated
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    @Deprecated
    public void setOperaArguments(final String operaArguments) {
        this.operaArguments = operaArguments;
    }

    @Deprecated
    public void setOperaAutostart(final boolean operaAutostart) {
        this.operaAutostart = operaAutostart;
    }

    @Deprecated
    public void setOperaBinary(final String operaBinary) {
        this.operaBinary = operaBinary;
    }

    @Deprecated
    public void setOperaDisplay(final int operaDisplay) {
        this.operaDisplay = operaDisplay;
    }

    @Deprecated
    public void setOperaIdle(final boolean operaIdle) {
        this.operaIdle = operaIdle;
    }

    @Deprecated
    public void setOperaLauncher(final String operaLauncher) {
        this.operaLauncher = operaLauncher;
    }

    @Deprecated
    public void setOperaLoggingFile(final String operaLoggingFile) {
        this.operaLoggingFile = operaLoggingFile;
    }

    @Deprecated
    public void setOperaLoggingLevel(final String operaLoggingLevel) {
        this.operaLoggingLevel = operaLoggingLevel;
    }

    @Deprecated
    public void setOperaPort(final int operaPort) {
        this.operaPort = operaPort;
    }

    @Deprecated
    public void setOperaProduct(final String operaProduct) {
        this.operaProduct = operaProduct;
    }

    @Deprecated
    public void setOperaProfile(final String operaProfile) {
        this.operaProfile = operaProfile;
    }

    @Deprecated
    public void setOperaQuit(final boolean operaQuit) {
        this.operaQuit = operaQuit;
    }

    @Deprecated
    public void setOperaRestart(final boolean operaRestart) {
        this.operaRestart = operaRestart;
    }

    public void setRemote(final boolean remote) {
        this.remote = remote;
    }

    public void setRemoteAddress(final URL remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public void setRemoteReusable(final boolean remoteReusable) {
        this.remoteReusable = remoteReusable;
    }

    @Deprecated
    public void setUseJavaScript(final boolean useJavaScript) {
        this.useJavaScript = useJavaScript;
    }

    @Deprecated
    public void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isReuseCookies() {
        return reuseCookies;
    }

    public void setReuseCookies(boolean reuseCookies) {
        this.reuseCookies = reuseCookies;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

}
