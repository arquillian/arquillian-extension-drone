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

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.RemoteReusableWebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
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
public class RemoteWebDriverFactory implements
        Configurator<RemoteWebDriver, TypedWebDriverConfiguration<RemoteReusableWebDriverConfiguration>>,
        Instantiator<RemoteWebDriver, TypedWebDriverConfiguration<RemoteReusableWebDriverConfiguration>>,
        Destructor<RemoteWebDriver> {

    private static final Logger log = Logger.getLogger(RemoteWebDriverFactory.class.getName());

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
    public RemoteWebDriver createInstance(TypedWebDriverConfiguration<RemoteReusableWebDriverConfiguration> configuration) {

        URL remoteAddress = configuration.getRemoteAddress();

        // default remote address
        if (Validate.empty(remoteAddress)) {
            remoteAddress = TypedWebDriverConfiguration.DEFAULT_REMOTE_URL;
            log.log(Level.INFO, "Property \"remoteAdress\" was not specified, using default value of {0}",
                    TypedWebDriverConfiguration.DEFAULT_REMOTE_URL);
        }

        Validate.isValidUrl(remoteAddress, "Remote address must be a valid url, " + remoteAddress);

        String browserCapabilities = configuration.getBrowserCapabilities();
        if (Validate.empty(browserCapabilities)) {
            configuration.setBrowserCapabilities(TypedWebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
            log.log(Level.INFO, "Property \"browserCapabilities\" was not specified, using default value of {0}",
                    TypedWebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
        }

        Validate.isEmpty(configuration.getBrowserCapabilities(), "The browser capabilities are not set.");

        // construct capabilities
        Capabilities desiredCapabilities = configuration.getCapabilities();

        if (configuration.isRemoteReusable()) {
            return createReusableDriver(remoteAddress, desiredCapabilities);
        } else {
            return createRemoteDriver(remoteAddress, desiredCapabilities);
        }
    }

    @Override
    public void destroyInstance(RemoteWebDriver instance) {
        if (instance.getSessionId() == null) {
            log.warning("The driver has been already destroyed and can't be destroyed again.");
            return;
        }

        InitializationParameter param = initParams.get().remove(instance.getSessionId());

        if (param != null) {
            ReusedSession session = new ReusedSession(instance.getSessionId(), instance.getCapabilities());
            sessionStore.get().store(param, session);
            persistEvent.fire(new PersistReusedSessionsEvent());
        } else {
            instance.quit();
        }

    }

    @Override
    public TypedWebDriverConfiguration<RemoteReusableWebDriverConfiguration> createConfiguration(
            ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {

        TypedWebDriverConfiguration<RemoteReusableWebDriverConfiguration> configuration = new TypedWebDriverConfiguration<RemoteReusableWebDriverConfiguration>(
                RemoteReusableWebDriverConfiguration.class).configure(descriptor, qualifier);
        if (!configuration.isRemote()) {
            configuration.setRemote(true);
            log.log(Level.FINE, "Forcing RemoteWebDriver configuration to be remote-based.");
        }

        return configuration;
    }

    private RemoteWebDriver createRemoteDriver(URL remoteAddress, Capabilities desiredCapabilities) {
        return new RemoteWebDriver(remoteAddress, desiredCapabilities);
    }

    private ReusableRemoteWebDriver createReusableDriver(URL remoteAddress, Capabilities desiredCapabilities) {
        // construct init params
        InitializationParameter initParam = new InitializationParameter(remoteAddress, desiredCapabilities);

        ReusableRemoteWebDriver driver = null;

        // try to reuse the session
        // retrieve the session id
        ReusedSession stored = sessionStore.get().pull(initParam);
        // get all the stored sessions for given initParam
        while (stored != null) {
            SessionId reusedSessionId = stored.getSessionId();
            try {
                driver = ReusableRemoteWebDriver.fromReusedSession(remoteAddress, desiredCapabilities, reusedSessionId);
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
