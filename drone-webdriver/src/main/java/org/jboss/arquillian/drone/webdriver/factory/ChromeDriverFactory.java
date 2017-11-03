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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.binary.handler.ChromeDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.jboss.arquillian.drone.webdriver.window.Dimensions;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * ChromeDriver.
 */
public class ChromeDriverFactory extends AbstractWebDriverFactory<ChromeDriver> implements
    Configurator<ChromeDriver, WebDriverConfiguration>, Instantiator<ChromeDriver, WebDriverConfiguration>,
    Destructor<ChromeDriver> {

    private static final Logger log = Logger.getLogger(ChromeDriverFactory.class.getName());

    public static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.Chrome().getReadableName();
    public static final String HEADLESS_BROWSER_CAPABILITIES = new BrowserCapabilitiesList.ChromeHeadless().getReadableName();
    private static final String CHROME_PRINT_OPTIONS = "chromePrintOptions";

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
    public void destroyInstance(ChromeDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public ChromeDriver createInstance(WebDriverConfiguration configuration) {

        Capabilities capabilities = getCapabilities(configuration, true);

        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] {Capabilities.class},
            new Object[] {capabilities}, ChromeDriver.class);
    }

    /**
     * Returns a {@link Capabilities} instance with set all necessary properties.
     * It also validates if the defined chrome.binary and chromeDriverBinary are executable binaries.
     * This validation can be set off/on by using variable performValidations; if set to true the IllegalArgumentException
     * can be thrown in case when requirements are not met
     *
     * @param configuration
     *     A configuration object for Drone extension
     * @param performValidations
     *     Whether a potential validation should be performed;
     *     if set to true an IllegalArgumentException (or other exception) can be thrown in case requirements are not met
     *
     * @return A {@link Capabilities} instance with set all necessary properties.
     */
    public Capabilities getCapabilities(WebDriverConfiguration configuration, boolean performValidations) {
        // set capabilities
        DesiredCapabilities capabilities = new DesiredCapabilities(configuration.getCapabilities());

        String binary = (String) capabilities.getCapability("chrome.binary");

        new ChromeDriverBinaryHandler(capabilities).checkAndSetBinary(performValidations);

        // verify binary capabilities
        if (Validate.nonEmpty(binary)) {
            if (performValidations) {
                Validate.isExecutable(binary, "Chrome binary must point to an executable file, " + binary);
            }
        }

        setChromeOptions(configuration, capabilities);

        return capabilities;
    }

    public void setChromeOptions(WebDriverConfiguration configuration, DesiredCapabilities capabilities) {
        ChromeOptions chromeOptions = new ChromeOptions();
        manageChromeHeadless(configuration, chromeOptions);

        CapabilitiesOptionsMapper.mapCapabilities(chromeOptions, capabilities, BROWSER_CAPABILITIES);

        String binary = (String) capabilities.getCapability("chrome.binary");
        if (Validate.nonEmpty(binary)) {
            // ARQ-1823 - setting chrome binary path through ChromeOptions
            chromeOptions.setBinary(binary);
        }

        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        String printChromeOptions = (String) capabilities.getCapability(CHROME_PRINT_OPTIONS);
        if (Validate.nonEmpty(printChromeOptions) && Boolean.valueOf(printChromeOptions.trim())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            StringBuffer chromeOptionsLog = new StringBuffer("\n");
            chromeOptionsLog.append("======== Chrome options =========").append("\n");
            chromeOptionsLog.append(gson.toJson(chromeOptions)).append("\n");
            chromeOptionsLog.append("===== End of Chrome options =====");
            log.info(chromeOptionsLog.toString());
        }
    }

    public void manageChromeHeadless(WebDriverConfiguration configuration, ChromeOptions chromeOptions) {
        String browser = configuration.getBrowser().toLowerCase();
        if (browser.equals(HEADLESS_BROWSER_CAPABILITIES)) {
            chromeOptions.addArguments("--headless");

            Dimensions dimensions = new Dimensions(configuration);
            if (dimensions.isFullscreenSet()) {
                dimensions.setWidth(1366);
                dimensions.setHeight(768);
                log.info(
                    String.format("Chrome Headless does not support fullscreen. Setting default window-size to %dx%d",
                        dimensions.getWidth(), dimensions.getHeight()));
            }
            if  (dimensions.areDimensionsPositive()) {
                chromeOptions.addArguments(
                    String.format("--window-size=%d,%d", dimensions.getWidth(), dimensions.getHeight()));
            }
        }
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, DronePoint<ChromeDriver>
        dronePoint) {
        WebDriverConfiguration configuration = super.createConfiguration(descriptor, dronePoint);
        if (!configuration.isRemote()) {
            configuration.setRemote(true);
            log.log(Level.FINE, "Forcing ChromeDriver configuration to be remote-based.");
        }
        return configuration;
    }
}
