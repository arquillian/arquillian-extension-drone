/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
package org.arquillian.drone.saucelabs.extension.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.arquillian.drone.saucelabs.extension.connect.SauceConnectRunner;
import org.arquillian.drone.saucelabs.extension.connect.Utils;
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

import static org.arquillian.drone.saucelabs.extension.webdriver.SauceLabsCapabilities.ACCESS_KEY;
import static org.arquillian.drone.saucelabs.extension.webdriver.SauceLabsCapabilities.READABLE_NAME;
import static org.arquillian.drone.saucelabs.extension.webdriver.SauceLabsCapabilities.SAUCE_CONNECT_ARGS;
import static org.arquillian.drone.saucelabs.extension.webdriver.SauceLabsCapabilities.SAUCE_CONNECT_BINARY;
import static org.arquillian.drone.saucelabs.extension.webdriver.SauceLabsCapabilities.SAUCE_CONNECT_MANAGED;
import static org.arquillian.drone.saucelabs.extension.webdriver.SauceLabsCapabilities.URL;
import static org.arquillian.drone.saucelabs.extension.webdriver.SauceLabsCapabilities.USERNAME;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * {@link SauceLabsDriver}.
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class SauceLabsDriverFactory implements
    Configurator<SauceLabsDriver, WebDriverConfiguration>,
    Instantiator<SauceLabsDriver, WebDriverConfiguration>,
    Destructor<SauceLabsDriver> {

    private static final Logger log = Logger.getLogger(SauceLabsDriverFactory.class.getName());

    @Inject
    protected Instance<BrowserCapabilitiesRegistry> registryInstance;

    public WebDriverConfiguration createConfiguration(ArquillianDescriptor arquillianDescriptor,
        DronePoint<SauceLabsDriver> dronePoint) {

        BrowserCapabilitiesRegistry registry = registryInstance.get();

        // first, try to create a BrowserCapabilities object based on Field/Parameter type of @Drone annotated field
        BrowserCapabilities browser = registry.getEntryFor(READABLE_NAME);

        WebDriverConfiguration configuration = new WebDriverConfiguration(browser).configure(arquillianDescriptor,
                                                                                             dronePoint.getQualifier());

        return configuration;
    }

    public void destroyInstance(SauceLabsDriver sauceLabsDriver) {
        sauceLabsDriver.quit();
    }

    public SauceLabsDriver createInstance(WebDriverConfiguration configuration) {
        try {
            Capabilities capabilities = configuration.getCapabilities();
            String url = (String) capabilities.getCapability(URL);
            String username = null;
            String accessKey = null;
            if (Utils.isEmpty(url)) {
                username = (String) capabilities.getCapability(USERNAME);
                accessKey = (String) capabilities.getCapability(ACCESS_KEY);
                if (accessKey == null) {
                    accessKey = (String) capabilities.getCapability("automate.key");
                }
                if (Utils.isEmpty(username) || Utils.isEmpty(accessKey)) {
                    log.warning(
                        "You have to specify either the whole url or an username and an access.key in your arquillian descriptor");
                    return null;
                } else {
                    url = "http://" + username + ":" + accessKey + "@ondemand.saucelabs.com:80/wd/hub";
                }
            }

            boolean isSetSauceConnectManaged = capabilities.is(SAUCE_CONNECT_MANAGED);
            if (isSetSauceConnectManaged
                && ((!Utils.isEmpty(accessKey) && !Utils.isEmpty(username)) || !Utils.isEmpty(url))) {

                if (Utils.isEmpty(accessKey)) {
                    username = url.substring(url.lastIndexOf("://") + 3, url.indexOf(":"));
                    accessKey = url.substring(url.lastIndexOf(":") + 1, url.indexOf("@"));
                }

                String additionalArgs = (String) capabilities.getCapability(SAUCE_CONNECT_ARGS);
                String localBinary = (String) capabilities.getCapability(SAUCE_CONNECT_BINARY);

                SauceConnectRunner.createSauceConnectRunnerInstance().runSauceConnect(username, accessKey,
                                                                                      additionalArgs, localBinary);
            }

            return new SauceLabsDriver(new URL(url), capabilities, isSetSauceConnectManaged);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPrecedence() {
        return 0;
    }
}
