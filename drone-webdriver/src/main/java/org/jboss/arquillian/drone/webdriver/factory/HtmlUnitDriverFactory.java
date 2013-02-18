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

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.CapabilityMap;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for HtmlUnitDriver.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class HtmlUnitDriverFactory extends AbstractWebDriverFactory<HtmlUnitDriver> implements
        Configurator<HtmlUnitDriver, WebDriverConfiguration>, Instantiator<HtmlUnitDriver, WebDriverConfiguration>,
        Destructor<HtmlUnitDriver> {

    private static final Logger log = Logger.getLogger(HtmlUnitDriverFactory.class.getName());

    private static final String BROWSER_CAPABILITIES = new CapabilityMap.HtmlUnit().getReadableName();

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
    public void destroyInstance(HtmlUnitDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public HtmlUnitDriver createInstance(WebDriverConfiguration configuration) {

        // this is support for legacy constructor
        String applicationName = configuration.getApplicationName();
        String applicationVersion = configuration.getApplicationVersion();
        String userAgent = configuration.getUserAgent();
        float browserVersionNumeric = configuration.getBrowserVersionNumeric();

        Capabilities capabilities = configuration.getCapabilities();

        // use capability based constructor if possible
        if (Validate.empty(applicationName) || Validate.empty(applicationVersion) || Validate.empty(userAgent)) {

            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { Capabilities.class },
                    new Object[] { capabilities }, HtmlUnitDriver.class);
        }
        // plain old constructor
        // this configuration is deprecated and should not be used anymore
        else {
            log.log(Level.WARNING,
                    "Creating HtmlUnitDriver using legacy configuration. ApplicationName={0} ApplicationVersion={1} UserAgent={2} BrowserVersionNumeric={3}",
                    new Object[] { applicationName, applicationVersion, userAgent, browserVersionNumeric });

            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { BrowserVersion.class },
                    new Object[] { new BrowserVersion(applicationName, applicationVersion, userAgent, browserVersionNumeric) },
                    HtmlUnitDriver.class);
        }
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }
}
