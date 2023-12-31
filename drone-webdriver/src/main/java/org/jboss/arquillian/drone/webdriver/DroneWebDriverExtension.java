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
package org.jboss.arquillian.drone.webdriver;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneInstanceEnhancer;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.augmentation.AugmentingEnhancer;
import org.jboss.arquillian.drone.webdriver.binary.process.SeleniumServerExecutor;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.factory.ChromeDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.EdgeDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.FirefoxDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.HtmlUnitDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.InternetExplorerDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.RemoteWebDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.SafariDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.WebDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriverExtension;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusedSessionPermanentFileStorage;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusedSessionPermanentStorage;
import org.jboss.arquillian.drone.webdriver.impl.BrowserCapabilitiesRegistrar;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.window.WindowResizer;

import static org.jboss.arquillian.drone.webdriver.utils.Constants.CHROME_DRIVER;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.EDGE_DRIVER;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.FIREFOX_DRIVER;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.HTMLUNIT_DRIVER;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.IE_DRIVER;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.REMOTE_DRIVER;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.SAFARI_DRIVER;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.WEB_DRIVER;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.WEB_DRIVER_NOT_FOUND_ERROR_MESSAGE;

/**
 * Arquillian Drone support for WebDriver
 */
public class DroneWebDriverExtension implements LoadableExtension {

    private static final Logger log = Logger.getLogger(DroneWebDriverExtension.class.getName());

    public void register(ExtensionBuilder builder) {

        registerFactoryService(builder, ChromeDriverFactory.class, CHROME_DRIVER);
        registerFactoryService(builder, EdgeDriverFactory.class, EDGE_DRIVER);
        registerFactoryService(builder, FirefoxDriverFactory.class, FIREFOX_DRIVER);
        registerFactoryService(builder, HtmlUnitDriverFactory.class, HTMLUNIT_DRIVER);
        registerFactoryService(builder, InternetExplorerDriverFactory.class, IE_DRIVER);
        registerFactoryService(builder, WebDriverFactory.class, WEB_DRIVER);
        registerFactoryService(builder, RemoteWebDriverFactory.class, REMOTE_DRIVER);
        registerFactoryService(builder, SafariDriverFactory.class, SAFARI_DRIVER);

        builder.observer(BrowserCapabilitiesRegistrar.class);

        builder.service(BrowserCapabilities.class, BrowserCapabilitiesList.Chrome.class);
        builder.service(BrowserCapabilities.class, BrowserCapabilitiesList.Edge.class);
        builder.service(BrowserCapabilities.class, BrowserCapabilitiesList.Firefox.class);
        builder.service(BrowserCapabilities.class, BrowserCapabilitiesList.HtmlUnit.class);
        builder.service(BrowserCapabilities.class, BrowserCapabilitiesList.InternetExplorer.class);
        builder.service(BrowserCapabilities.class, BrowserCapabilitiesList.Remote.class);
        builder.service(BrowserCapabilities.class, BrowserCapabilitiesList.Safari.class);
        builder.service(BrowserCapabilities.class, BrowserCapabilitiesList.ChromeHeadless.class);

        builder.observer(ReusableRemoteWebDriverExtension.class);
        builder.service(ReusedSessionPermanentStorage.class, ReusedSessionPermanentFileStorage.class);

        builder.service(DroneInstanceEnhancer.class, AugmentingEnhancer.class);
        builder.observer(WindowResizer.class);
        builder.observer(SeleniumServerExecutor.class);
    }

    private <T extends Configurator & Instantiator & Destructor> void registerFactoryService(
        ExtensionBuilder builder, Class<T> factory, String expectedDriver) {

        try {
            Class.forName(expectedDriver, false, this.getClass().getClassLoader());

            builder.service(Configurator.class, factory);
            builder.service(Instantiator.class, factory);
            builder.service(Destructor.class, factory);
        } catch (ClassNotFoundException e) {
            log.log(Level.WARNING, WEB_DRIVER_NOT_FOUND_ERROR_MESSAGE,
                new Object[] {expectedDriver, factory.getCanonicalName()});
        }
    }
}
