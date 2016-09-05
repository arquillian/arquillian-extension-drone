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
package org.jboss.arquillian.drone.webdriver.factory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.utils.FirefoxPrefsReader;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for FirefoxDriver.
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class FirefoxDriverFactory extends AbstractWebDriverFactory<FirefoxDriver> implements
        Configurator<FirefoxDriver, WebDriverConfiguration>, Instantiator<FirefoxDriver, WebDriverConfiguration>,
        Destructor<FirefoxDriver> {

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.Firefox().getReadableName();

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Sortable#getPrecedence()
     */
    @Override
    public int getPrecedence() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Destructor#destroyInstance(java.lang.Object)
     */
    @Override
    public void destroyInstance(FirefoxDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public FirefoxDriver createInstance(WebDriverConfiguration configuration) {

        Capabilities capabilities = getCapabilities(configuration);

        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { Capabilities.class },
                new Object[] { capabilities }, FirefoxDriver.class);

    }

    /**
     * Returns a {@link Capabilities} instance with set all necessary properties.
     * It also validates whether the defined firefox_binary is an executable binary and creates/sets a prospective
     * firefox profile as well as a firefox extension.
     *
     * @param configuration A configuration object for Drone extension
     * @return A {@link Capabilities} instance with set all necessary properties.
     */
    public Capabilities getCapabilities(WebDriverConfiguration configuration){
        DesiredCapabilities capabilities = new DesiredCapabilities(configuration.getCapabilities());

        String binary = (String) configuration.getCapabilities().getCapability(FirefoxDriver.BINARY);
        String profile = (String) configuration.getCapabilities().getCapability(FirefoxDriver.PROFILE);

        // verify firefox binary if set
        if (Validate.nonEmpty(binary)) {
            Validate.isExecutable(binary, "Firefox binary does not point to a valid executable,  " + binary);
        }

        // set firefox profile from path if specified
        FirefoxProfile firefoxProfile;
        if (Validate.nonEmpty(profile)) {
            Validate.isValidPath(profile, "Firefox profile does not point to a valid path " + profile);
            firefoxProfile = new FirefoxProfile(new File(profile));
        }
        else {
            firefoxProfile = new FirefoxProfile();
        }

        // enable or disable the native events if specified
        Boolean nativeEvents = (Boolean) configuration.getCapabilities().getCapability("nativeEvents");
        if (!Validate.empty(nativeEvents)) {
            firefoxProfile.setEnableNativeEvents(nativeEvents);
        }

        capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);

        final String firefoxExtensions = (String) capabilities.getCapability("firefoxExtensions");
        // no check is needed here, it will return empty array if null
        for (String extensionPath : StringUtils.tokenize(firefoxExtensions)) {
            try {
                firefoxProfile.addExtension(new File(extensionPath));
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot read XPI extension file: " + extensionPath, e);
            }
        }

        // add user preferences from file
        final String userPreferences = (String) capabilities.getCapability("firefoxUserPreferences");
        if (Validate.nonEmpty(userPreferences)) {
            Validate.isValidPath(userPreferences, "User preferences does not point to a valid path " + userPreferences);
            // we need to manually parse preferences, as Selenium provides no way to set these value
            for (Map.Entry<String, Object> preference : FirefoxPrefsReader.getPreferences(new File(userPreferences)).entrySet()) {
                String key = preference.getKey();
                Object value = preference.getValue();
                if (value instanceof Boolean) {
                    firefoxProfile.setPreference(key, (Boolean) value);
                }
                else if (value instanceof Integer) {
                    firefoxProfile.setPreference(key, (Integer) value);
                }
                else if (value instanceof String) {
                    firefoxProfile.setPreference(key, (String) value);
                }
            }
        }

        return capabilities;
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

}