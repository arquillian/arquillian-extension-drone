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
import org.jboss.arquillian.drone.webdriver.binary.handler.ChromeForTestingDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.utils.ChromeUtils;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.jboss.arquillian.drone.webdriver.window.Dimensions;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

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
        instance.close();   // necessary to avoid "Connection Reset by peer" errors
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public ChromeDriver createInstance(WebDriverConfiguration configuration) {
        final ChromeOptions options = getChromeOptions(configuration);

        ChromeDriverService chromeDriverService = new ChromeDriverService.Builder()
                .withLogOutput(System.out).build();
        return SecurityActions.newInstance(configuration.getImplementationClass(),
                new Class<?>[]{ChromeDriverService.class, ChromeOptions.class},
                new Object[]{chromeDriverService, options}, ChromeDriver.class);
    }

    /**
     * Returns a {@link ChromeOptions} instance with set all necessary properties.
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
    public ChromeOptions getChromeOptions(WebDriverConfiguration configuration, boolean performValidations) {
        ChromeOptions chromeOptions = new ChromeOptions();
        Capabilities capabilities = configuration.getCapabilities();
        final String binary = (String) capabilities.getCapability("chrome.binary");
        final String version = ChromeUtils.getChromeVersion(capabilities);

        final ChromeDriverBinaryHandler handler = ChromeUtils.isChromeForTesting(version) ?
            new ChromeForTestingDriverBinaryHandler(capabilities) :
            new ChromeDriverBinaryHandler(capabilities);

        handler.checkAndSetBinary(performValidations);

        // verify binary capabilities
        if (Validate.nonEmpty(binary)) {
            if (performValidations) {
                Validate.isExecutable(binary, "Chrome binary must point to an executable file, " + binary);
            }
        }

        setChromeOptions(configuration, chromeOptions);

        return chromeOptions;
    }

    public ChromeOptions getChromeOptions(WebDriverConfiguration configuration) {
        return getChromeOptions(configuration, true);
    }

    public void setChromeOptions(WebDriverConfiguration configuration, ChromeOptions chromeOptions) {
        manageChromeHeadless(configuration, chromeOptions);

        CapabilitiesOptionsMapper.mapCapabilities(chromeOptions, configuration.getCapabilities(), BROWSER_CAPABILITIES);

        String binary = (String) configuration.getCapabilities().getCapability("chrome.binary");
        if (Validate.nonEmpty(binary)) {
            // ARQ-1823 - setting chrome binary path through ChromeOptions
            chromeOptions.setBinary(binary);
        }

        chromeOptions.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        String printChromeOptions = (String) configuration.getCapabilities().getCapability(CHROME_PRINT_OPTIONS);
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
        String browser = configuration.getBrowserName().toLowerCase();
        if (browser.equals(HEADLESS_BROWSER_CAPABILITIES)) {
            chromeOptions.addArguments("--headless=new");

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
