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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.htmlunit.HtmlUnitDriver;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * HtmlUnitDriver.
 */
public class HtmlUnitDriverFactory extends AbstractWebDriverFactory<HtmlUnitDriver> implements
    Configurator<HtmlUnitDriver, WebDriverConfiguration>, Instantiator<HtmlUnitDriver, WebDriverConfiguration>,
    Destructor<HtmlUnitDriver> {

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.HtmlUnit().getReadableName();

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
        Capabilities capabilities = getCapabilities(configuration, true);
        final HtmlUnitDriver htmlUnitDriver =
            SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] {Capabilities.class},
                new Object[] {capabilities}, HtmlUnitDriver.class);

        final String htmlUnitClientOptions = (String) capabilities.getCapability("htmlUnitWebClientOptions");
        if (Validate.nonEmpty(htmlUnitClientOptions)) {
            WebClient webClient = htmlUnitDriver.getWebClient();
            setClientOptions(webClient, htmlUnitClientOptions);
        }

        return htmlUnitDriver;
    }

    /**
     * Returns a {@link Capabilities} instance which is completely same as that one that is contained in the configuration
     * object itself - there is no necessary properties to be set
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
        return configuration.getCapabilities();
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

    private void setClientOptions(WebClient webClient, String htmlUnitWebClientOptions) {
        final WebClientOptions webClientOptions = webClient.getOptions();

        Map<String, String> clientOptions = new LinkedHashMap<>();
        final String[] options = htmlUnitWebClientOptions.split(",");
        for (String option : options) {
            final String[] keyValue = option.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                key = BROWSER_CAPABILITIES + Character.toUpperCase(key.charAt(0)) + key.substring(1);
                clientOptions.put(key, keyValue[1].trim());
            }
        }
        final DesiredCapabilities webClientCapabilities = new DesiredCapabilities(clientOptions);

        CapabilitiesOptionsMapper.mapCapabilities(webClientOptions, webClientCapabilities, BROWSER_CAPABILITIES);
    }
}
