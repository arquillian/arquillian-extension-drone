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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * InternetExplorerDriver.
 */
public class InternetExplorerDriverFactory extends AbstractWebDriverFactory<InternetExplorerDriver> implements
    Configurator<InternetExplorerDriver, WebDriverConfiguration>,
    Instantiator<InternetExplorerDriver, WebDriverConfiguration>, Destructor<InternetExplorerDriver> {

    public static final int DEFAULT_INTERNET_EXPLORER_PORT = 0;
    private static final Logger log = Logger.getLogger(InternetExplorerDriverFactory.class.getName());
    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.InternetExplorer().getReadableName();

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
    public void destroyInstance(InternetExplorerDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public InternetExplorerDriver createInstance(WebDriverConfiguration configuration) {

        int port = configuration.getIePort();

        InternetExplorerDriverService service = new InternetExplorerDriverService.Builder()
                .withLogOutput(System.out).build();

        // capabilities based
        if (port == DEFAULT_INTERNET_EXPLORER_PORT) {
            return SecurityActions.newInstance(configuration.getImplementationClass(),
                    new Class<?>[]{InternetExplorerDriverService.class, InternetExplorerOptions.class},
                    new Object[]{service, getOptions(configuration, true)}, InternetExplorerDriver.class);
        }
        // port specified, we cannot use capabilities
        else {
            log.log(Level.FINE, "Creating InternetExplorerDriver bound to port {0}", port);

            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[]{int.class},
                    new Object[]{port}, InternetExplorerDriver.class);
        }
    }

    /**
     * Returns a {@link Capabilities} instance which is completely same as that one that is contained in the configuration
     * object itself - there is no necessary properties to be set.
     *
     * @param configuration
     *     A configuration object for Drone extension
     * @param performValidations
     *     Whether a potential validation should be performed;
     *     if set to true an IllegalArgumentException (or other exception) can be thrown in case requirements are not met
     *
     * @return A {@link Capabilities} instance
     */
    public Capabilities getOptions(WebDriverConfiguration configuration, boolean performValidations) {
        return new InternetExplorerOptions();
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }
}
