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

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.binary.handler.PhantomJSDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * PhantomJSDriver.
 *
 * @author <a href="mjobanek@redhat.com">Matous Jobanek</a>
 */
public class PhantomJSDriverFactory extends AbstractWebDriverFactory<PhantomJSDriver> implements
    Configurator<PhantomJSDriver, WebDriverConfiguration>, Instantiator<PhantomJSDriver, WebDriverConfiguration>,
    Destructor<PhantomJSDriver> {

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.PhantomJS().getReadableName();
    private static final String PHANTOMJS_DEFAULT_EXECUTABLE = "phantomjs";

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

        Capabilities capabilities = getCapabilities(configuration, true);

        PhantomJSDriverService phantomJSDriverService = PhantomJSDriverService.createDefaultService(capabilities);

        return SecurityActions.newInstance(configuration.getImplementationClass(),
            new Class<?>[] {PhantomJSDriverService.class, Capabilities.class},
            new Object[] {phantomJSDriverService, capabilities},
            PhantomJSDriver.class);
    }

    /**
     * Returns a {@link Capabilities} instance with set all necessary properties (ie: phantomjs.binary.path).
     *
     * @param configuration
     *     A configuration object for Drone extension
     * @param performValidations
     *     Whether a potential validation should be performed;
     *     if set to true an IllegalArgumentException (or other exception) can be thrown in case requirements are not met
     *
     * @return A {@link Capabilities} instance with set all necessary properties.
     */
    public Capabilities getCapabilities(WebDriverConfiguration configuration, boolean performValidations) {
        // resolve capabilities
        DesiredCapabilities capabilities = new DesiredCapabilities(configuration.getCapabilities());
        reformatCLIArgumentsInCapToArray(capabilities);

        if (!isDefaultExecutablePresent()) {
            new PhantomJSDriverBinaryHandler(capabilities).checkAndSetBinary(performValidations);
        }

        return capabilities;
    }

    /**
     * Reformats {@link PhantomJSDriverService.PHANTOMJS_CLI_ARGS} and
     * {@link PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS} from String to String[]
     *
     * @param capabilities
     *     Capabilities
     */
    public void reformatCLIArgumentsInCapToArray(DesiredCapabilities capabilities) {
        reformatCapabilityToArray(capabilities, PhantomJSDriverService.PHANTOMJS_CLI_ARGS);
        reformatCapabilityToArray(capabilities, PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS);
    }

    private void reformatCapabilityToArray(DesiredCapabilities capabilities, String capabilityName) {
        Object capability = capabilities.getCapability(capabilityName);
        if (capability != null) {
            if (capability instanceof String) {
                String[] splitArgs = ((String) capability).split(" ");
                capabilities.setCapability(capabilityName, splitArgs);
            }
        }
    }

    private boolean isDefaultExecutablePresent() {
        return Validate.isCommandExecutable(PHANTOMJS_DEFAULT_EXECUTABLE);
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }
}
