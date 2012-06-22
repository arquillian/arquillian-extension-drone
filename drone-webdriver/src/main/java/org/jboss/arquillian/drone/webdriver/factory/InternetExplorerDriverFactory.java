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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.InternetExplorerDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * InternetExplorerDriver.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class InternetExplorerDriverFactory implements
        Configurator<InternetExplorerDriver, TypedWebDriverConfiguration<InternetExplorerDriverConfiguration>>,
        Instantiator<InternetExplorerDriver, TypedWebDriverConfiguration<InternetExplorerDriverConfiguration>>,
        Destructor<InternetExplorerDriver> {

    private static final Logger log = Logger.getLogger(InternetExplorerDriverFactory.class.getName());

    public static final int DEFAULT_INTERNET_EXPLORER_PORT = 0;

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
    public InternetExplorerDriver createInstance(TypedWebDriverConfiguration<InternetExplorerDriverConfiguration> configuration) {

        int port = configuration.getIePort();

        // capabilities based
        if (port == DEFAULT_INTERNET_EXPLORER_PORT) {
            return SecurityActions.newInstance(configuration.getImplementationClass(),
                    new Class<?>[] { DesiredCapabilities.class }, new Object[] { configuration.getCapabilities() },
                    InternetExplorerDriver.class);
        }
        // port specified, we cannot use capabilities
        else {
            log.log(Level.FINE, "Creating InternetExplorerDriver bound to port {0}", port);

            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { int.class },
                    new Object[] { port }, InternetExplorerDriver.class);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.core.spi.LoadableExtension#createConfiguration(org.jboss.arquillian.impl.configuration.api.
     * ArquillianDescriptor, java.lang.Class)
     */
    @Override
    public TypedWebDriverConfiguration<InternetExplorerDriverConfiguration> createConfiguration(
            ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        return new TypedWebDriverConfiguration<InternetExplorerDriverConfiguration>(InternetExplorerDriverConfiguration.class)
                .configure(descriptor, qualifier);
    }

}