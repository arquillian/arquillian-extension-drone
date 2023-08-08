/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for a generic
 * WebDriver browser.
 */
public class WebDriverFactory extends AbstractWebDriverFactory<WebDriver> implements
    Configurator<WebDriver, WebDriverConfiguration>, Instantiator<WebDriver, WebDriverConfiguration>,
    Destructor<WebDriver> {

    private static final Logger log = Logger.getLogger(WebDriverFactory.class.getName());

    @Inject
    private Instance<DroneRegistry> registryInstance;

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
    @SuppressWarnings("unchecked")
    @Override
    public void destroyInstance(WebDriver instance) {

        @SuppressWarnings("rawtypes")
        Destructor destructor = null;

        // get destructor which is able to handle RemoteReusable logic if set
        if (instance instanceof ReusableRemoteWebDriver) {
            destructor = getRemoteWebDriverDestructor();
        } else {
            try {
                destructor = registryInstance.get().getEntryFor(instance.getClass(), Destructor.class);
            } catch (Exception ignored) {
                log.log(Level.WARNING,
                    "Unable to get destructor for @Drone WebDriver, real class {0}, quitting instance using default disposal method",
                    instance.getClass().getSimpleName());
            }
        }

        if (destructor != null && !destructor.getClass().equals(this.getClass())) {
            destructor.destroyInstance(instance);
        }
        // this is default destructor
        else {
            instance.quit();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @SuppressWarnings("unchecked")
    @Override
    public WebDriver createInstance(WebDriverConfiguration configuration) {

        @SuppressWarnings("rawtypes")
        Instantiator instantiator;
        String implementationClassName = null;

        // get remote or remote-reusable instantiator
        if (configuration.isRemoteReusable() || configuration.isRemote()) {
            instantiator = getRemoteWebDriverInstantiator();
        }
        // get real implementation class name based on capabilitiesBrowser and
        // deprecated implementationClassName
        else {

            implementationClassName = configuration.getImplementationClass();

            Validate.isEmpty(implementationClassName,
                "The combination of browser=" + configuration.getBrowserName()
                    + ", implemenationClass=" + implementationClassName
                    + " does not represent a valid browser. Please specify supported browser.");

            DroneRegistry registry = registryInstance.get();
            Class<?> implementationClass = SecurityActions.getClass(implementationClassName);
            instantiator = registry.getEntryFor(implementationClass, Instantiator.class);
        }

        // if we've found an instantiator and at the same time we are sure that it is not the current one
        // invoke it instead in order to get capabilities and other advanced stuff
        if (instantiator != null && instantiator.getClass() != this.getClass()) {
            return (WebDriver) instantiator.createInstance(configuration);
        }

        // this is a simple constructor which does not know anything advanced
        if (Validate.empty(implementationClassName)) {
            WebDriver driver = SecurityActions.newInstance(implementationClassName, new Class<?>[0], new Object[0],
                WebDriver.class);
            return driver;
        }

        throw new IllegalStateException(
            "Unable to create Arquillian WebDriver browser, please set \"browser\" property");
    }

    @Override
    protected String getDriverReadableName() {
        return null;
    }

    @SuppressWarnings({"rawtypes"})
    private Instantiator getRemoteWebDriverInstantiator() {
        DroneRegistry registry = registryInstance.get();
        return registry.getEntryFor(RemoteWebDriver.class, Instantiator.class);
    }

    @SuppressWarnings({"rawtypes"})
    private Destructor getRemoteWebDriverDestructor() {
        DroneRegistry registry = registryInstance.get();
        return registry.getEntryFor(RemoteWebDriver.class, Destructor.class);
    }
}
