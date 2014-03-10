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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * AndroidDriver.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 */
public class AndroidDriverFactory extends AbstractWebDriverFactory<AndroidDriver> implements
        Configurator<AndroidDriver, WebDriverConfiguration>, Instantiator<AndroidDriver, WebDriverConfiguration>,
        Destructor<AndroidDriver> {

    private static final Logger log = Logger.getLogger(AndroidDriverFactory.class.getName());

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.Android().getReadableName();

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
    public void destroyInstance(AndroidDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi
     * .DroneConfiguration)
     */
    @Override
    public AndroidDriver createInstance(WebDriverConfiguration configuration) {

        URL remoteAddress = configuration.getRemoteAddress();

        // default remote address
        if (Validate.empty(remoteAddress)) {
            remoteAddress = WebDriverConfiguration.DEFAULT_REMOTE_URL;
            log.log(Level.INFO, "Property \"remoteAdress\" was not specified, using default value of {0}",
                    WebDriverConfiguration.DEFAULT_REMOTE_URL);
        }

        Validate.isValidUrl(remoteAddress, "Remote address must be a valid url, " + remoteAddress);

        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { URL.class,
                DesiredCapabilities.class },
                new Object[] { remoteAddress, new DesiredCapabilities(configuration.getCapabilities()) },
                AndroidDriver.class);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.core.spi.LoadableExtension#createConfiguration(org.jboss.arquillian.impl
     * .configuration.api.
     * ArquillianDescriptor, java.lang.Class)
     */

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, InjectionPoint<AndroidDriver>
            injectionPoint) {
        WebDriverConfiguration configuration = super.createConfiguration(descriptor, injectionPoint);
        if (!configuration.isRemote()) {
            configuration.setRemote(true);
            log.log(Level.FINE, "Forcing AndroidDriver configuration to be remote-based.");
        }
        return configuration;
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

}