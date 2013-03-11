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
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.InitializationParameter;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.InitializationParametersMap;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.PersistReusedSessionsEvent;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusedSession;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusedSessionStore;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.UnableReuseSessionException;
import org.openqa.selenium.Capabilities;
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
    Instance<ReusedSessionStore> sessionStore;
    @Inject
    Instance<InitializationParametersMap> initParams;
    @Inject
    Event<PersistReusedSessionsEvent> persistEvent;

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
            log.log(Level.INFO, "Property \"remoteAdress\" was not specified, using default value of {0}",
                    WebDriverConfiguration.DEFAULT_REMOTE_URL);
        }

        Validate.isValidUrl(remoteAddress, "Remote address must be a valid url, " + remoteAddress);

        String browserCapabilities = configuration.getBrowserCapabilities();
        if (Validate.empty(browserCapabilities)) {
            configuration.setBrowserCapabilities(WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
            log.log(Level.INFO, "Property \"browserCapabilities\" was not specified, using default value of {0}",
                    WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
        }

        Validate.isEmpty(configuration.getBrowserCapabilities(), "The browser capabilities are not set.");

        // construct capabilities
        Capabilities desiredCapabilities = configuration.getCapabilities();

        RemoteWebDriver driver = null;

        if (configuration.isRemoteReusable()) {
            driver = createReusableDriver(remoteAddress, desiredCapabilities);
        } else {
            driver = createRemoteDriver(remoteAddress, desiredCapabilities);
        }

        // ARQ-1206
        // by default, we are clearing Cookies on reusable browsers
        if (!configuration.isReuseCookies()) {
            driver.manage().deleteAllCookies();
        }

        return driver;
    }

    @Override
    public void destroyInstance(RemoteWebDriver driver) {
        if (driver.getSessionId() == null) {
            log.warning("The driver has been already destroyed and can't be destroyed again.");
            return;
        }

        SessionId sessionId = driver.getSessionId();
        Capabilities driverCapabilities = driver.getCapabilities();

        InitializationParameter param = initParams.get().remove(sessionId);

        if (param != null) {
            ReusedSession session = ReusedSession.createInstance(sessionId, driverCapabilities);
            sessionStore.get().store(param, session);
            persistEvent.fire(new PersistReusedSessionsEvent());
        } else {
            driver.quit();
        }
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

    protected RemoteWebDriver createRemoteDriver(URL remoteAddress, Capabilities desiredCapabilities) {
        return new RemoteWebDriver(remoteAddress, desiredCapabilities);
    }

    private RemoteWebDriver createReusableDriver(URL remoteAddress, Capabilities desiredCapabilities) {
        // construct init params
        InitializationParameter initParam = new InitializationParameter(remoteAddress, desiredCapabilities);

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
            RemoteWebDriver newdriver = createRemoteDriver(remoteAddress, desiredCapabilities);
            driver = ReusableRemoteWebDriver.fromRemoteWebDriver(newdriver);
        }

        initParams.get().put(driver.getSessionId(), initParam);
        return driver;
    }
}
