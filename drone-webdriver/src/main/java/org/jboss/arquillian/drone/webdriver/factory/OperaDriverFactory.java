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

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.binary.handler.OperaDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.utils.PropertySecurityAction;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * OperaDriver.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 */
public class OperaDriverFactory extends AbstractWebDriverFactory<OperaDriver> implements
    Configurator<OperaDriver, WebDriverConfiguration>, Instantiator<OperaDriver, WebDriverConfiguration>,
    Destructor<OperaDriver> {

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.Opera().getReadableName();
    private static final String OPERA_BINARY_KEY = "opera.binary";

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public OperaDriver createInstance(WebDriverConfiguration configuration) {

        Capabilities operaCapabilities = getCapabilities(configuration, true);
        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] {Capabilities.class},
            new Object[] {operaCapabilities}, OperaDriver.class);
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
    public Capabilities getCapabilities(WebDriverConfiguration configuration, boolean performValidations) {

        Capabilities capabilities = configuration.getCapabilities();

        new OperaDriverBinaryHandler(capabilities).checkAndSetBinary(performValidations);

        String binary = PropertySecurityAction.getProperty(OPERA_BINARY_KEY);

        if (Validate.empty(binary)) {
            binary = (String) capabilities.getCapability(OPERA_BINARY_KEY);
        }

        OperaOptions operaOptions = new OperaOptions();
        CapabilitiesOptionsMapper.mapCapabilities(operaOptions, capabilities, BROWSER_CAPABILITIES);

        if (Validate.nonEmpty(binary)) {
            if (performValidations) {
                Validate.isExecutable(binary, "Opera binary must point to an executable file, " + binary);
            }
            operaOptions.setBinary(binary);
        }

        return capabilities;
    }

    @Override
    public void destroyInstance(OperaDriver instance) {
        instance.quit();
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }
}
