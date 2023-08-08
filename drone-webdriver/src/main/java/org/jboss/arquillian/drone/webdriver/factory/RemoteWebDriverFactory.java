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

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.augmentation.AugmentingEnhancer;
import org.jboss.arquillian.drone.webdriver.binary.handler.SeleniumServerBinaryHandler;
import org.jboss.arquillian.drone.webdriver.binary.process.StartSeleniumServer;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.InitializationParameter;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.InitializationParametersMap;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.PersistReusedSessionsEvent;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriverToDestroy;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusedSession;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusedSessionStore;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.UnableReuseSessionException;
import org.jboss.arquillian.drone.webdriver.utils.UrlUtils;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
 */
public class RemoteWebDriverFactory extends AbstractWebDriverFactory<RemoteWebDriver> implements
    Configurator<RemoteWebDriver, WebDriverConfiguration>, Instantiator<RemoteWebDriver, WebDriverConfiguration>,
    Destructor<RemoteWebDriver> {

    private static final Logger log = Logger.getLogger(RemoteWebDriverFactory.class.getName());

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.Remote().getReadableName();

    @Inject
    private Instance<ReusedSessionStore> sessionStore;
    @Inject
    private Instance<InitializationParametersMap> initParams;
    @Inject
    private Event<PersistReusedSessionsEvent> persistEvent;
    @Inject
    private Event<StartSeleniumServer> startSeleniumServerEvent;

    @Inject
    @ApplicationScoped
    private InstanceProducer<ReusableRemoteWebDriverToDestroy> lastRemoteWebDriverToDestroy;

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public RemoteWebDriver createInstance(WebDriverConfiguration configuration) {

        URL remoteAddress = configuration.getRemoteAddress();

        // default remote address
        if (Validate.empty(remoteAddress)) {
            remoteAddress = WebDriverConfiguration.DEFAULT_REMOTE_URL;
            log.log(Level.INFO, "Property \"remoteAddress\" was not specified, using default value of {0}",
                WebDriverConfiguration.DEFAULT_REMOTE_URL);
        }

        Validate.isValidUrl(remoteAddress, "Remote address must be a valid url, " + remoteAddress);

        String browser = configuration.getBrowserName().toLowerCase();
        if (Validate.empty(browser)) {
            configuration.setBrowserName(WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
            log.log(Level.INFO, "Property \"browser\" was not specified, using default value of {0}",
                WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
        }

        Validate.isEmpty(configuration.getBrowserName(), "The browser is not set.");

        // construct capabilities
        Capabilities options;
        boolean augmentationSupported = true;
        if (browser.equals(Browser.CHROME.browserName()) || browser.equals("chromeheadless")) {
            options = new ChromeDriverFactory().getChromeOptions(configuration);
        } else if(browser.equals(Browser.FIREFOX.browserName())) {
            options = new FirefoxDriverFactory().getFirefoxOptions(configuration, true);
            augmentationSupported = false;
        } else if(browser.equals(Browser.SAFARI.browserName())) {
            options = new SafariDriverFactory().getOptions(configuration, true);
        } else if(browser.equals("edge")) {
            options = new EdgeDriverFactory().getEdgeOptions(configuration);
        } else {
            options = new MutableCapabilities();
        }

        if (!UrlUtils.isReachable(remoteAddress)) {
            if (UrlUtils.isLocalhost(remoteAddress)) {
                log.info("The Selenium server is not running on: " + remoteAddress
                    + " and as the address seems to be a localhost address, Drone will start the Selenium Server automatically.");
                try {
                    downloadAndStartSeleniumServer(configuration, browser, remoteAddress);
                } catch (Exception e) {
                    throw new IllegalStateException(
                        "Something bad happened when Drone was trying to download and extract Selenium Server binary. "
                            + "For more information see the cause.", e);
                }
            } else {
                log.warning("The URL: " + remoteAddress
                    + " doesn't seem to be reachable. If there is no Selenium Server running, start it before the tests are run.");
            }
        }

        RemoteWebDriver driver = null;

        if (configuration.isRemoteReusable()) {
            driver = createReusableDriver(remoteAddress, options);
        } else {
            driver = createRemoteDriver(remoteAddress, options);
        }

        if (augmentationSupported) {
            // ARQ-1351
            // marks the driver instance for augmentation by AugmentingEnhancer
            ((MutableCapabilities) driver.getCapabilities()).setCapability(AugmentingEnhancer.DRONE_AUGMENTED, driver);
        } else {
            ((MutableCapabilities) driver.getCapabilities()).setCapability(AugmentingEnhancer.DRONE_AUGMENTED, Boolean.FALSE);
        }

        // ARQ-1206
        // by default, we are clearing Cookies on reusable browsers
        if (!configuration.isReuseCookies()) {
            driver.manage().deleteAllCookies();
        }

        return driver;
    }

    private void downloadAndStartSeleniumServer(WebDriverConfiguration configuration, String browser,
        URL remoteAddress) throws Exception {

        Capabilities capabilities = new ImmutableCapabilities(getCapabilities(configuration, true));
        String seleniumServer = new SeleniumServerBinaryHandler(capabilities).downloadAndPrepare().toString();

        if (!Validate.empty(seleniumServer)) {
            String seleniumServerArgs = configuration.getSeleniumServerArgs();

            if (Validate.empty(seleniumServerArgs)) {
                configuration.setSeleniumServerArgs(WebDriverConfiguration.DEFAULT_SELENIUM_SERVER_ARGS);
            }

            startSeleniumServerEvent.fire(
                new StartSeleniumServer(seleniumServer, browser, capabilities, remoteAddress,
                    seleniumServerArgs));
        }
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
        return configuration.getCapabilities();
    }

    @Override
    public void destroyInstance(RemoteWebDriver driver) {
        // there is no sessionId
        // this very likely mean that session was already destroyed
        if (driver.getSessionId() == null) {
            try {
                driver.quit();
            } catch (WebDriverException e) {
                log.log(Level.WARNING, "@Drone {0} has been already destroyed and can't be destroyed again.",
                    driver.getClass()
                        .getSimpleName());
            }
            return;
        }

        SessionId sessionId = driver.getSessionId();
        Capabilities driverCapabilities = driver.getCapabilities();

        InitializationParameter param = initParams.get().remove(sessionId);

        if (param != null) {
            ReusedSession session = ReusedSession.createInstance(sessionId, driverCapabilities);
            sessionStore.get().store(param, session);
            persistEvent.fire(new PersistReusedSessionsEvent());
            lastRemoteWebDriverToDestroy.set(new ReusableRemoteWebDriverToDestroy(driver));
        } else {
            driver.quit();
        }
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

    protected RemoteWebDriver createRemoteDriver(URL remoteAddress, Capabilities capabilities) {
        return new RemoteWebDriver(remoteAddress, capabilities);
    }

    private RemoteWebDriver createReusableDriver(URL remoteAddress, Capabilities capabilities) {
        // construct init params
        InitializationParameter initParam = new InitializationParameter(remoteAddress, capabilities);

        RemoteWebDriver driver = null;

        // try to reuse the session
        // retrieve the session id
        ReusedSession stored = sessionStore.get().pull(initParam);
        // get all the stored sessions for given initParam
        while (stored != null) {
            SessionId reusedSessionId = stored.getSessionId();
            Capabilities reusedCapabilities = stored.getCapabilities();
            try {
                driver = ReusableRemoteWebDriver.fromReusedSession(remoteAddress, reusedCapabilities, reusedSessionId);
                break;
            } catch (UnableReuseSessionException ex) {
                log.log(Level.WARNING, "Unable to reuse session: {0}", stored.getSessionId());
            }

            stored = sessionStore.get().pull(initParam);
        }

        if (driver == null) {
            // if either browser session isn't stored or can't be reused
            RemoteWebDriver newdriver = createRemoteDriver(remoteAddress, capabilities);
            driver = ReusableRemoteWebDriver.fromRemoteWebDriver(newdriver);
        }

        initParams.get().put(driver.getSessionId(), initParam);

        return driver;
    }
}
