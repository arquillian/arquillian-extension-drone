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
import java.util.Map;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.binary.handler.FirefoxDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.utils.FirefoxPrefsReader;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import static org.jboss.arquillian.drone.webdriver.binary.handler.FirefoxDriverBinaryHandler.FIREFOX_DRIVER_BINARY_PROPERTY;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * FirefoxDriver.
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
        FirefoxOptions firefoxOptions = getFirefoxOptions(configuration, true);

        FirefoxDriverService firefoxDriverService = new GeckoDriverService.Builder()
                .withLogOutput(System.out).build();
        return SecurityActions.newInstance(configuration.getImplementationClass(),
                new Class<?>[]{FirefoxDriverService.class, FirefoxOptions.class},
                new Object[]{firefoxDriverService, firefoxOptions}, FirefoxDriver.class);
    }

    /**
     * Returns a {@link FirefoxOptions} instance with set all necessary properties.
     * It also validates whether the defined firefoxDriverBinary are executable binaries
     * and creates/sets a prospective firefox profile as well as a firefox extension.
     * This validation can be set off/on by using variable performValidations; if set to true the IllegalArgumentException
     * can be thrown in case when requirements are not met
     *
     * @param configuration
     *     A configuration object for Drone extension
     * @param performValidations
     *     Whether a potential validation should be performed;
     *     if set to true an IllegalArgumentException (or other exception) can be thrown in case requirements are not met
     *
     * @return A {@link FirefoxOptions} instance with set all necessary properties; if set to true the
     * IllegalArgumentException
     * can be thrown in case when requirements are not met
     */
    public FirefoxOptions getFirefoxOptions(WebDriverConfiguration configuration, boolean performValidations) {
        Capabilities capabilities = configuration.getCapabilities();
        String binary = (String) capabilities.getCapability(FIREFOX_DRIVER_BINARY_PROPERTY);

        // verify firefox binary if set
        if (Validate.nonEmpty(binary) && performValidations) {
            Validate.isExecutable(binary, "Firefox binary does not point to a valid executable,  " + binary);
        }

        new FirefoxDriverBinaryHandler(capabilities).checkAndSetBinary(performValidations);

        // using FirefoxOptions which is now the preferred way for configuring GeckoDriver
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        CapabilitiesOptionsMapper.mapCapabilities(firefoxOptions, capabilities, BROWSER_CAPABILITIES);

        FirefoxProfile firefoxProfile = getFirefoxProfile(capabilities, performValidations);
        if (firefoxProfile != null) {
            firefoxOptions.setProfile(firefoxProfile);
        }

        // add user preferences from file
        addUserPreferencesFromFile(capabilities, firefoxOptions);

        return firefoxOptions;
    }

    private FirefoxProfile getFirefoxProfile(Capabilities capabilities, boolean performValidations) {
        String profile = (String) capabilities.getCapability("firefox_profile");
        if (profile == null) {
            profile = (String) capabilities.getCapability("firefoxProfile");
        }
        FirefoxProfile firefoxProfile;

        // use the explicit profile only if absolutely necessary;
        // the new GeckoDriver otherwise handles the profile itself and this e.g. enables manipulation with some User Preferences
        // which are frozen if the profile is specified (e.g. extensions.logging.enabled)
        boolean profileShouldBeSet = false;

        // set firefox profile from path if specified
        if (Validate.nonEmpty(profile)) {
            if (performValidations) {
                Validate.isValidPath(profile, "Firefox profile does not point to a valid path " + profile);
            }
            firefoxProfile = new FirefoxProfile(new File(profile));
            profileShouldBeSet = true;
        } else {
            firefoxProfile = new FirefoxProfile();
        }

        final String firefoxExtensions = (String) capabilities.getCapability("firefoxExtensions");
        // no check is needed here, it will return empty array if null
        for (String extensionPath : StringUtils.tokenize(firefoxExtensions)) {
            firefoxProfile.addExtension(new File(extensionPath));
            profileShouldBeSet = true;
        }

        return profileShouldBeSet ? firefoxProfile : null;
    }

    private void addUserPreferencesFromFile(Capabilities capabilities, FirefoxOptions firefoxOptions) {

        final String userPreferences = (String) capabilities.getCapability("firefoxUserPreferences");
        if (Validate.nonEmpty(userPreferences)) {
            Validate.isValidPath(userPreferences, "User preferences does not point to a valid path " + userPreferences);
            // we need to manually parse preferences, as Selenium provides no way to set these value
            for (Map.Entry<String, Object> preference : FirefoxPrefsReader.getPreferences(new File(userPreferences))
                .entrySet()) {
                String key = preference.getKey();
                Object value = preference.getValue();
                if (value instanceof Boolean) {
                    firefoxOptions.addPreference(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    firefoxOptions.addPreference(key, (Integer) value);
                } else if (value instanceof String) {
                    firefoxOptions.addPreference(key, (String) value);
                }
            }
        }
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }
}
