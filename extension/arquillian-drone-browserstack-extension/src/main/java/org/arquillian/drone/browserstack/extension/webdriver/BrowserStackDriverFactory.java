/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
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
package org.arquillian.drone.browserstack.extension.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.arquillian.drone.browserstack.extension.local.BrowserStackLocalRunner;
import org.arquillian.drone.browserstack.extension.utils.Utils;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilitiesRegistry;
import org.openqa.selenium.Capabilities;

import static org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities.ACCESS_KEY;
import static org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities.BROWSERSTACK_LOCAL;
import static org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities.BROWSERSTACK_LOCAL_ARGS;
import static org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities.BROWSERSTACK_LOCAL_BINARY;
import static org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities.BROWSERSTACK_LOCAL_MANAGED;
import static org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities.READABLE_NAME;
import static org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities.URL;
import static org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities.USERNAME;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * {@link BrowserStackDriver}.
 */
public class BrowserStackDriverFactory implements
    Configurator<BrowserStackDriver, WebDriverConfiguration>,
    Instantiator<BrowserStackDriver, WebDriverConfiguration>,
    Destructor<BrowserStackDriver> {

    private static final Logger log = Logger.getLogger(BrowserStackDriverFactory.class.getName());

    @Inject
    private Instance<BrowserCapabilitiesRegistry> registryInstance;

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor arquillianDescriptor,
        DronePoint<BrowserStackDriver> dronePoint) {

        BrowserCapabilitiesRegistry registry = registryInstance.get();
        BrowserCapabilities browser = registry.getEntryFor(READABLE_NAME);

        WebDriverConfiguration configuration = new WebDriverConfiguration(browser).configure(arquillianDescriptor,
            dronePoint.getQualifier());
        return configuration;
    }

    @Override
    public void destroyInstance(BrowserStackDriver browserStackDriver) {
        browserStackDriver.quit();
    }

    @Override
    public BrowserStackDriver createInstance(WebDriverConfiguration configuration) {
        Capabilities capabilities = configuration.getCapabilities();
        String url = (String) capabilities.getCapability(URL);
        String accessKey = null;

        if (Utils.isNullOrEmpty(url)) {
            String username = (String) capabilities.getCapability(USERNAME);
            accessKey = (String) capabilities.getCapability(ACCESS_KEY);

            if (Utils.isNullOrEmpty(accessKey)) {
                accessKey = (String) capabilities.getCapability("automate.key");
            }
            if (Utils.isNullOrEmpty(username) || Utils.isNullOrEmpty(accessKey)) {
                throw new IllegalArgumentException(
                    "You have to specify either an username and an access.key or the whole url in your arquillian descriptor");
            } else {
                url = "http://" + username + ":" + accessKey + "@hub.browserstack.com/wd/hub";
            }
        }

        try {
            URL browserStackUrl = new URL(url);

            boolean isSetBrowserStackLocal = capabilities.is(BROWSERSTACK_LOCAL);
            boolean isSetBrowserStackLocalManaged = capabilities.is(BROWSERSTACK_LOCAL_MANAGED);

            if (isSetBrowserStackLocal && isSetBrowserStackLocalManaged) {
                if (Utils.isNullOrEmpty(accessKey)) {
                    accessKey = url.substring(url.lastIndexOf(":") + 1, url.indexOf("@"));
                }
                String additionalArgs = (String) capabilities.getCapability(BROWSERSTACK_LOCAL_ARGS);
                String localBinary = (String) capabilities.getCapability(BROWSERSTACK_LOCAL_BINARY);

                BrowserStackLocalRunner.getBrowserStackLocalInstance().runBrowserStackLocal(accessKey,
                    additionalArgs,
                    localBinary);
            }

            return new BrowserStackDriver(browserStackUrl, capabilities, isSetBrowserStackLocal,
                isSetBrowserStackLocalManaged);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                "The BrowserStack url: " + url + " has been detected as a malformed URL. ", e);
        }
    }

    @Override
    public int getPrecedence() {
        return 0;
    }
}
