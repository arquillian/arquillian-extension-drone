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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.RemoteWebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.InitializationParameter;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.InitializationParametersMap;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.PersistReusedSessionsEvent;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusedSession;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusedSessionStore;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.UnableReuseSessionException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class RemoteWebDriverFactory implements
        Configurator<RemoteWebDriver, TypedWebDriverConfiguration<RemoteWebDriverConfiguration>>,
        Instantiator<RemoteWebDriver, TypedWebDriverConfiguration<RemoteWebDriverConfiguration>>,
        Destructor<RemoteWebDriver> {

    private static final Logger LOGGER = Logger.getLogger(RemoteWebDriverFactory.class.getCanonicalName());

    @Inject
    Instance<ReusedSessionStore> sessionStore;
    @Inject
    Instance<InitializationParametersMap> initParams;
    @Inject
    Event<PersistReusedSessionsEvent> persistEvent;

    @Override
    public TypedWebDriverConfiguration<RemoteWebDriverConfiguration> createConfiguration(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        return new TypedWebDriverConfiguration<RemoteWebDriverConfiguration>(RemoteWebDriverConfiguration.class,
                "org.openqa.selenium.remote.RemoteWebDriver").configure(descriptor, qualifier);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public RemoteWebDriver createInstance(TypedWebDriverConfiguration<RemoteWebDriverConfiguration> configuration) {
        if (configuration.getBrowserCapabilities() == null) {
            throw new IllegalArgumentException("The browser capabilities are not set.");
        }
        if (configuration.getRemoteAddress() == null) {
            throw new IllegalArgumentException("The remote address has to be set.");
        }
        // construct remote address URL
        Validate.isValidUrl(configuration.getRemoteAddress(), "Remote address must be a valid url, " + configuration.getRemoteAddress());
        URL remoteAddress = null;
        try {
            remoteAddress = new URL(configuration.getRemoteAddress());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Can't create a new instance of RemoteWebDriver, because the URL <"+ configuration.getRemoteAddress() + "> doesn't seem to be valid.", e);
        }
        // construct capabilities
        DesiredCapabilities desiredCapabilities = getDesiredCapabilities(configuration.getBrowserCapabilities());
        Map<String, String> capabilities = configuration.getCapabilities(configuration.getBrowserCapabilities());
        for (Entry<String, String> entry: capabilities.entrySet()) {
            desiredCapabilities.setCapability(entry.getKey(), entry.getValue());
        }
        // construct init params
        InitializationParameter initParam = new InitializationParameter(remoteAddress, desiredCapabilities, configuration.isRemoteReusable());
        RemoteWebDriver driver = null;
        // create a new instance of webdriver
        if (configuration.isRemoteReusable()) {
            // try to reuse the session
            // retrieve the session id
            ReusedSession stored = sessionStore.get().pull(initParam);
            if (stored != null) {
                try {
                    driver = new ReusableRemoteWebDriver(remoteAddress, desiredCapabilities, stored.getSessionId());
                } catch (UnableReuseSessionException ex) {
                    LOGGER.log(Level.WARNING, "Unable to reuse session: {0}", stored.getSessionId());
                }
            }
        }
        if (driver == null) {
            // create a new instance with a new session
            driver = new RemoteWebDriver(remoteAddress, desiredCapabilities);
        }
        initParams.get().put(driver.getSessionId(), initParam);
        return driver;
    }

    @Override
    public void destroyInstance(RemoteWebDriver instance) {
        if (instance.getSessionId() == null) {
            LOGGER.warning("The driver has been already destroyed and can't be destroyed again.");
            return;
        }
        InitializationParameter param = initParams.get().remove(instance.getSessionId());
        if (param == null) {
            LOGGER.warning("Can't load initialization parameter for driver instance. The driver and its session will be destroyed.");
            instance.quit();
            return;
        }
        if (param.isReusable()) {
            ReusedSession session = new ReusedSession(instance.getSessionId(), instance.getCapabilities());
            sessionStore.get().store(param, session);
            persistEvent.fire(new PersistReusedSessionsEvent());
        } else {
            instance.quit();
        }
    }

    protected DesiredCapabilities getDesiredCapabilities(String browserCapabilities) {
        for (Method method: DesiredCapabilities.class.getDeclaredMethods()) {
            if (method.getName().equals(browserCapabilities) && method.getReturnType().equals(DesiredCapabilities.class) && Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 0) {
                try {
                    return DesiredCapabilities.class.cast(method.invoke(null, new Object[] {}));
                } catch (Exception e) {
                    throw new IllegalStateException("Can't invoke method " + DesiredCapabilities.class + "#" + browserCapabilities + "()");
                }
            }
        }
        throw new IllegalStateException("There is no method " + DesiredCapabilities.class + "#" + browserCapabilities + "()");
    }

}
