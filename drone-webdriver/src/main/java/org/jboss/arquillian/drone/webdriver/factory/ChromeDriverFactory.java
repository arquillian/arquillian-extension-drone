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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for ChromeDriver.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class ChromeDriverFactory extends AbstractWebDriverFactory<ChromeDriver> implements
        Configurator<ChromeDriver, WebDriverConfiguration>, Instantiator<ChromeDriver, WebDriverConfiguration>,
        Destructor<ChromeDriver> {

    private static final Logger log = Logger.getLogger(ChromeDriverFactory.class.getName());

    private static final String CHROME_DRIVER_BINARY_KEY = "webdriver.chrome.driver";

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.Chrome().getReadableName();

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

        // set capabilities
        DesiredCapabilities capabilities = new DesiredCapabilities(configuration.getCapabilities());

        String driverBinary = configuration.getChromeDriverBinary();
        String binary = (String) capabilities.getCapability("chrome.binary");
        String chromeSwitches = (String) capabilities.getCapability("chrome.switches");

        if (Validate.empty(driverBinary)) {
            driverBinary = SecurityActions.getProperty(CHROME_DRIVER_BINARY_KEY);
        }

        // driver binary configuration
        // this is setting system property
        if (Validate.nonEmpty(driverBinary)) {
            Validate.isExecutable(driverBinary, "Chrome driver binary must point to an executable file, " + driverBinary);
            SecurityActions.setProperty(CHROME_DRIVER_BINARY_KEY, driverBinary);
        }

        // verify binary capabilities
        if (Validate.nonEmpty(binary)) {
            Validate.isExecutable(binary, "Chrome binary must point to an executable file, " + binary);
        }

        // convert chrome switches to an array of strings
        if (Validate.nonEmpty(chromeSwitches)) {
            capabilities.setCapability("chrome.switches", getChromeSwitches(chromeSwitches));
        }

        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { Capabilities.class },
                new Object[] { capabilities }, ChromeDriver.class);
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, InjectionPoint<ChromeDriver>
            injectionPoint) {
        WebDriverConfiguration configuration = super.createConfiguration(descriptor, injectionPoint);
        if(!configuration.isRemote()) {
            configuration.setRemote(true);
            log.log(Level.FINE, "Forcing ChromeDriver configuration to be remote-based.");
        }
        return configuration;
    }

    private List<String> getChromeSwitches(String valueString) {
        List<String> chromeSwitches = new ArrayList<String>();
        for (String property : StringUtils.tokenize(valueString)) {
            if (property.startsWith("--")) {
                chromeSwitches.add(property);
            }
        }
        return chromeSwitches;
    }

}