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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.ChromeDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
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
public class ChromeDriverFactory implements Configurator<ChromeDriver, TypedWebDriverConfiguration<ChromeDriverConfiguration>>,
        Instantiator<ChromeDriver, TypedWebDriverConfiguration<ChromeDriverConfiguration>>, Destructor<ChromeDriver> {

    private static final String CHROME_DRIVER_BINARY_KEY = "webdriver.chrome.driver";

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
    public ChromeDriver createInstance(TypedWebDriverConfiguration<ChromeDriverConfiguration> configuration) {

        String binary = configuration.getChromeBinary();
        String driverBinary = configuration.getChromeDriverBinary();
        String chromeSwitches = configuration.getChromeSwitches();

        if (Validate.empty(driverBinary)) {
            driverBinary = SecurityActions.getProperty(CHROME_DRIVER_BINARY_KEY);
        }

        // driver binary configuration
        // this is setting system property
        if (Validate.nonEmpty(driverBinary)) {
            Validate.isExecutable(driverBinary, "Chrome driver binary must point to an executable file, " + driverBinary);
            SecurityActions.setProperty(CHROME_DRIVER_BINARY_KEY, driverBinary);
        }

        // create the instance, chrome binary was not set
        if (Validate.empty(binary) || Validate.empty(chromeSwitches)) {
            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[0], new Object[0],
                    ChromeDriver.class);
        }

        // set capabilities
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();

        // binary was set, so set the capability
        if (Validate.nonEmpty(binary)) {
            Validate.isExecutable(binary, "Chrome binary must point to an executable file, " + binary);

            // set path to chrome
            capabilities.setCapability("chrome.binary", binary);
        }

        if (Validate.nonEmpty(chromeSwitches)) {
            capabilities.setCapability("chrome.switches", getChromeSwitches(chromeSwitches));
        }

        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { Capabilities.class },
                new Object[] { capabilities }, ChromeDriver.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.core.spi.LoadableExtension#createConfiguration(org.jboss.arquillian.impl.configuration.api.
     * ArquillianDescriptor, java.lang.Class)
     */
    @Override
    public TypedWebDriverConfiguration<ChromeDriverConfiguration> createConfiguration(ArquillianDescriptor descriptor,
            Class<? extends Annotation> qualifier) {
        return new TypedWebDriverConfiguration<ChromeDriverConfiguration>(ChromeDriverConfiguration.class,
                "org.openqa.selenium.chrome.ChromeDriver").configure(descriptor, qualifier);
    }

    private List<String> getChromeSwitches(String valueString) {
        List<String> properties = new ArrayList<String>();

        // FIXME this should accept properties encapsulated in quotes as well
        StringTokenizer tokenizer = new StringTokenizer(valueString, " ");
        while (tokenizer.hasMoreTokens()) {
            String property = tokenizer.nextToken().trim();

            if (property.indexOf("--") == -1) {
                continue;
            }

            properties.add(property);
        }

        return properties;
    }

}