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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.htmlunit.WebClient;
import org.htmlunit.WebClientOptions;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.htmlunit.DroneHtmlUnitDriver;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.MutableCapabilities;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * DroneHtmlUnitDriver.
 */
public class HtmlUnitDriverFactory extends AbstractWebDriverFactory<DroneHtmlUnitDriver> implements
    Configurator<DroneHtmlUnitDriver, WebDriverConfiguration>, Instantiator<DroneHtmlUnitDriver, WebDriverConfiguration>,
    Destructor<DroneHtmlUnitDriver> {

    private static final String BROWSER_NAME = new BrowserCapabilitiesList.HtmlUnit().getReadableName();
    static final String webClientOptions = "htmlUnitWebClientOptions";
    private static final Logger logger = Logger.getLogger(HtmlUnitDriverFactory.class.getName());
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
    public void destroyInstance(DroneHtmlUnitDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public DroneHtmlUnitDriver createInstance(WebDriverConfiguration configuration) {
        Capabilities capabilities = getCapabilities(configuration, true);
        final DroneHtmlUnitDriver droneHtmlUnitDriver =
            SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] {Capabilities.class},
                new Object[] {capabilities}, DroneHtmlUnitDriver.class);

        final String htmlUnitClientOptions = (String) capabilities.getCapability(webClientOptions);
        if (Validate.nonEmpty(htmlUnitClientOptions)) {
            WebClient webClient = droneHtmlUnitDriver.getWebClient();
            setClientOptions(webClient, htmlUnitClientOptions);
        }

        return droneHtmlUnitDriver;
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
        MutableCapabilities caps = new MutableCapabilities(configuration.getCapabilities());
        caps.setCapability("browserName", BROWSER_NAME);
        return new ImmutableCapabilities(caps);
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_NAME;
    }

    private void setClientOptions(WebClient webClient, String htmlUnitWebClientOptions) {
        final WebClientOptions webClientOptions = webClient.getOptions();
        Map<String, String> clientOptions = new LinkedHashMap<>();
        final String multiline = StringUtils.trimMultiline(htmlUnitWebClientOptions);
        final String[] options = multiline.split(";");
        for (String option : options) {
            final String[] keyValue = option.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                if (key.length() > 0) {
                    key = BROWSER_NAME + Character.toUpperCase(key.charAt(0)) + key.substring(1);
                    String value = keyValue[1].trim();
                    if (value.contains(",")) {
                        value = value.replaceAll("\\s*,\\s*", " ");
                    }
                    clientOptions.put(key, value);
                } else  {
                    logger.info("Excluding option : " + option + "to set for HtmlUnitDriver webClientOptions as it is not as per required format i.e. key=value");
                }
            }
        }
        final Capabilities webClientCapabilities = new ImmutableCapabilities(clientOptions);

        logger.info("Setting HtmlDriver web client options: " + clientOptions);
        CapabilitiesOptionsMapper.mapCapabilities(webClientOptions, webClientCapabilities, BROWSER_NAME);
    }
}
