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
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;

/**
 * Generic configuration for WebDriver Driver. By default, it uses HtmlUnit Driver.
 */
public class WebDriverConfiguration implements DroneConfiguration<WebDriverConfiguration> {

    public static final String CONFIGURATION_NAME = "webdriver";
    public static final String DEFAULT_BROWSER_CAPABILITIES = new BrowserCapabilitiesList.HtmlUnit().getReadableName();
    public static final String DEFAULT_SELENIUM_SERVER_ARGS = "";
    private static final Logger log = Logger.getLogger(WebDriverConfiguration.class.getName());
    public static URL DEFAULT_REMOTE_URL;

    static {
        try {
            DEFAULT_REMOTE_URL = new URL("http://localhost:4444/wd/hub");
        } catch (MalformedURLException e) {
            // ignore invalid url exception
        }
    }

    private int iePort;

    private URL remoteAddress;

    private String browser;

    private boolean remoteReusable;

    private boolean remote;

    private String seleniumServerArgs;

    // ARQ-1206, ability to delete all cookies in reused browsers
    private boolean reuseCookies;

    private String dimensions;

    private Map<String, Object> capabilityMap;

    // internal variables
    // ARQ-1022
    private String _originalBrowser;

    private BrowserCapabilities _browser;

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

    @Override
    public WebDriverConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);

        // ARQ-1022, we need to check if we haven't overriden original browser
        // capabilities in an incompatible way
        if (_originalBrowser != null && !_originalBrowser.equals(this.browser)) {
            log.log(Level.WARNING,
                "Arquillian configuration is specifying a Drone of type {0}, however test class specifically asked for {1}. As Drone cannot guarantee that those two are compatible, \"browser\" property will be set to {1}.",
                new Object[] {browser, _originalBrowser});
            this.browser = _originalBrowser;
        }
        return this;
    }

    public String getBrowserName() {

        if (_browser != null) {
            return _browser.getReadableName();
        }

        return browser;
    }

    @Deprecated
    public String getBrowser() {
        return getBrowserName();
    }

    public void setBrowserName(String browser) {
        this.browser = browser;
    }

    @Deprecated
    public void setBrowser(String browser) {
        setBrowserName(browser);
    }

    public Capabilities getCapabilities() {
        return new ImmutableCapabilities(this.capabilityMap);
    }

    public String getSeleniumServerArgs() {
        return seleniumServerArgs;
    }

    public void setSeleniumServerArgs(String seleniumServerArgs) {
        this.seleniumServerArgs = seleniumServerArgs;
    }

    @Override
    public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }

    public int getIePort() {
        return iePort;
    }

    public void setIePort(final int iePort) {
        this.iePort = iePort;
    }

    public String getImplementationClass() {
        return _browser.getImplementationClassName();
    }

    public URL getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final URL remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(final boolean remote) {
        this.remote = remote;
    }

    public boolean isRemoteReusable() {
        return remoteReusable;
    }

    public void setRemoteReusable(final boolean remoteReusable) {
        this.remoteReusable = remoteReusable;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public boolean isReuseCookies() {
        return reuseCookies;
    }

    public void setReuseCookies(boolean reuseCookies) {
        this.reuseCookies = reuseCookies;
    }
}
