/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.arquillian.drone.browserstack.extension.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.arquillian.drone.browserstack.extension.webdriver.local.BrowserStackLocalRunner;
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

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * {@link BrowserStackDriver}.
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BrowserStackDriverFactory implements
    Configurator<BrowserStackDriver, WebDriverConfiguration>,
    Instantiator<BrowserStackDriver, WebDriverConfiguration>,
    Destructor<BrowserStackDriver> {

    private static final Logger log = Logger.getLogger(BrowserStackDriverFactory.class.getName());

    @Inject
    protected Instance<BrowserCapabilitiesRegistry> registryInstance;

    public WebDriverConfiguration createConfiguration(ArquillianDescriptor arquillianDescriptor,
        DronePoint<BrowserStackDriver> dronePoint) {

        BrowserCapabilitiesRegistry registry = registryInstance.get();

        // first, try to create a BrowserCapabilities object based on Field/Parameter type of @Drone annotated field
        BrowserCapabilities browser = registry.getEntryFor(BrowserStackDriver.READABLE_NAME);

        WebDriverConfiguration configuration = new WebDriverConfiguration(browser).configure(arquillianDescriptor,
                                                                                             dronePoint.getQualifier());

        return configuration;
    }

    public void destroyInstance(BrowserStackDriver browserStackDriver) {
        browserStackDriver.quit();
    }

    public BrowserStackDriver createInstance(WebDriverConfiguration configuration) {
        try {
            Capabilities capabilities = configuration.getCapabilities();

            String url = (String) capabilities.getCapability("url");
            String accessKey = null;
            if (isEmpty(url)) {
                String username = (String) capabilities.getCapability("username");
                accessKey = (String) capabilities.getCapability("access.key");
                if (isEmpty(accessKey)) {
                    accessKey = (String) capabilities.getCapability("automate.key");
                }
                if (isEmpty(username) || isEmpty(accessKey)) {
                    log.severe(
                        "You have to specify either the whole url or an username and an access.key in your arquillian descriptor");
                    return null;
                } else {
                    url = "http://" + username + ":" + accessKey + "@hub.browserstack.com/wd/hub";
                }
            }

            if (capabilities.is(BrowserStackDriver.BROWSERSTACK_LOCAL) && capabilities.is(
                BrowserStackDriver.BROWSERSTACK_LOCAL_MANAGED) && (!isEmpty(accessKey) || !isEmpty(url))) {
                if (isEmpty(accessKey)) {
                    accessKey = url.substring(url.lastIndexOf(":") + 1, url.indexOf("@"));
                }
                String localIdentifier =
                    (String) capabilities.getCapability(BrowserStackDriver.BROWSERSTACK_LOCAL_IDENTIFIER);
                String localBinary = (String) capabilities.getCapability(BrowserStackDriver.BROWSERSTACK_LOCAL_BINARY);

                BrowserStackLocalRunner.createBrowserStackLocalInstance()
                    .runBrowserStackLocal(accessKey, localIdentifier, localBinary);
            }

            return new BrowserStackDriver(new URL(url), capabilities);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isEmpty(String object) {
        return object == null || object.isEmpty();
    }

    public int getPrecedence() {
        return 0;
    }
}
