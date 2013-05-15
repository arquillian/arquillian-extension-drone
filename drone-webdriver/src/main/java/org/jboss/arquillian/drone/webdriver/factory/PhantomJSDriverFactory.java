/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.phantom.resolver.ResolvingPhantomJSDriverService;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.service.DriverService;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * PhantomJSDriver.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class PhantomJSDriverFactory extends AbstractWebDriverFactory<PhantomJSDriver> implements
        Configurator<PhantomJSDriver, WebDriverConfiguration>, Instantiator<PhantomJSDriver, WebDriverConfiguration>,
        Destructor<PhantomJSDriver> {

    // that's the only property we need to verify here
    // the rest is already extracted from capabilities
    private static final String PHANTOMJS_DRIVER_BINARY_KEY = PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.PhantomJS().getReadableName();

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
    public void destroyInstance(PhantomJSDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public PhantomJSDriver createInstance(WebDriverConfiguration configuration) {

        // set capabilities
        DesiredCapabilities capabilities = new DesiredCapabilities(configuration.getCapabilities());

        String driverBinary = (String) capabilities.getCapability(PHANTOMJS_DRIVER_BINARY_KEY);

        if (Validate.empty(driverBinary)) {
            driverBinary = SecurityActions.getProperty(PHANTOMJS_DRIVER_BINARY_KEY);
            capabilities.setCapability(PHANTOMJS_DRIVER_BINARY_KEY, driverBinary);
        }

        // create an instance with the resolving binary
        if (Validate.nonEmpty(driverBinary)) {
            try {
                return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { DriverService.class, Capabilities.class },
                    new Object[] { ResolvingPhantomJSDriverService.createDefaultService(capabilities), capabilities }, PhantomJSDriver.class);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create an instance of " + configuration.getImplementationClass() + ".", e);
            }
        // create an instance with the common behaviour
        } else {
            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { Capabilities.class },
                    new Object[] { capabilities }, PhantomJSDriver.class);
        }
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

}