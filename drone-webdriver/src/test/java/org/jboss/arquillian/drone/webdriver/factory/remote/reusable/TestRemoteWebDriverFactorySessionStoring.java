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
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.webdriver.binary.handler.SeleniumServerBinaryHandler;
import org.jboss.arquillian.drone.webdriver.binary.process.SeleniumServerExecutor;
import org.jboss.arquillian.drone.webdriver.binary.process.StartSeleniumServer;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.factory.ChromeDriverFactory;
import org.jboss.arquillian.drone.webdriver.factory.RemoteWebDriverFactory;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class TestRemoteWebDriverFactorySessionStoring extends AbstractTestTestBase {

    @Inject
    Event<PersistReusedSessionsEvent> persistEvent;
    @Mock
    private ServiceLoader serviceLoader;
    @Mock
    private WebDriverConfiguration configuration;

    @Inject
    private Instance<ReusedSessionStore> sessionStore;
    @Inject
    private Instance<Injector> injector;
    private Capabilities desiredCapabilities;
    private URL hubUrl;
    private MockReusedSessionPermanentStorage permanentStorage;
    private InitializationParameter initializationParameter;

    @BeforeClass
    public static void skipIfEdgeBrowser() {
        Assume.assumeFalse(System.getProperty("browser", "phantomjs").equals("edge"));
    }

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ReusableRemoteWebDriverExtension.class);
        extensions.add(SeleniumServerExecutor.class);
    }

    @Before
    public void setupMocks() {

        // set browser capabilities to be the same as defined in arquillian.xml - webdriver-reusable configuration
        MockBrowserCapabilitiesRegistry registry = MockBrowserCapabilitiesRegistry.createSingletonRegistry();
        desiredCapabilities = new DesiredCapabilities(registry.getAllBrowserCapabilities().iterator().next()
            .getRawCapabilities());

        permanentStorage = new MockReusedSessionPermanentStorage();
        when(serviceLoader.onlyOne(ReusedSessionPermanentStorage.class)).thenReturn(permanentStorage);
        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);

        try {
            hubUrl = new URL("http://localhost:5555/wd/hub/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }

        runSeleniumServer();

        initializationParameter = new InitializationParameter(hubUrl, desiredCapabilities);

        String browser = System.getProperty("browser").toLowerCase();
        if (browser.equals("chromeheadless")) {
            when(configuration.getBrowser()).thenReturn("chromeheadless");
            new ChromeDriverFactory().setChromeOptions(configuration, (DesiredCapabilities) desiredCapabilities);
        }

        when(configuration.getBrowser()).thenReturn("xyz");
        when(configuration.isRemoteReusable()).thenReturn(true);
        when(configuration.getCapabilities()).thenReturn(desiredCapabilities);
        when(configuration.getRemoteAddress()).thenReturn(hubUrl);
        configuration.setSeleniumServerArgs("-debug true");
    }

    private void runSeleniumServer() {
        try {
            String browser = (String) desiredCapabilities.getCapability("browserName");
            DesiredCapabilities selServerCaps = new DesiredCapabilities(desiredCapabilities);
            String seleniumServerArgs = System.getProperty("seleniumServerArgs");

            // use selenium server version defined in arquillian.xml
            String selSerVersion = getSeleniumServerVersion(MockBrowserCapabilitiesRegistry.getArquillianDescriptor());
            if (!Validate.empty(selSerVersion)) {
                selServerCaps
                    .setCapability(SeleniumServerBinaryHandler.SELENIUM_SERVER_VERSION_PROPERTY, selSerVersion);
            }
            String seleniumServerBinary =
                new SeleniumServerBinaryHandler(selServerCaps).downloadAndPrepare().toString();

            fire(new StartSeleniumServer(seleniumServerBinary, browser, selServerCaps, hubUrl, seleniumServerArgs));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String getSeleniumServerVersion(ArquillianDescriptor arquillian) {
        ExtensionDef webdriver = arquillian.extension("webdriver-reusable");
        Map<String, String> props = webdriver.getExtensionProperties();
        return props.get("seleniumServerVersion");
    }

    @After
    public void stopServer() {
        fire(new AfterSuite());
    }

    @Test
    public void when_reusable_session_is_created_then_is_can_be_pulled_from_session_store() throws Exception {

        // having
        RemoteWebDriverFactory factory1 = new MockRemoteWebDriverFactory();
        injector.get().inject(factory1);

        // when
        fire(new BeforeSuite());

        RemoteWebDriver webdriver1 = factory1.createInstance(configuration);
        factory1.destroyInstance(webdriver1);
        webdriver1.quit();

        // then
        ReusedSession reusedSession = sessionStore.get().pull(initializationParameter);
        assertNotNull("reusedSession must be stored", reusedSession);
    }

    @Test
    public void when_session_is_created_and_persisted_but_nonreusable_then_next_creation_should_remove_it_from_list_of_reusable_sessions()
        throws Exception {

        // having
        RemoteWebDriverFactory factory1 = new MockRemoteWebDriverFactory();
        RemoteWebDriverFactory factory2 = new MockRemoteWebDriverFactory();
        injector.get().inject(factory1);
        injector.get().inject(factory2);

        // when
        fire(new BeforeSuite());

        // creates new session
        RemoteWebDriver webdriver1 = factory1.createInstance(configuration);
        // persists session into store
        factory1.destroyInstance(webdriver1);
        // makes driver non-reusable
        webdriver1.quit();

        // new suite
        fire(new BeforeSuite());
        // pulls non-reusable session from store, so creates new session
        RemoteWebDriver webdriver2 = factory2.createInstance(configuration);
        // quit newly created session
        factory2.destroyInstance(webdriver2);
        webdriver2.quit();
        // persists available sessions (none should be available)
        // persistEvent.fire(new PersistReusedSessionsEvent());

        // new suite
        fire(new BeforeSuite());
        // pulls session - should *NOT* be empty
        ReusedSession reusedSession = sessionStore.get().pull(initializationParameter);
        assertNotNull("reusedSession must be stored", reusedSession);
        // pulls session - should be empty - it was cleared by last pull
        reusedSession = sessionStore.get().pull(initializationParameter);
        assertNull("reusedSession must not be stored", reusedSession);
    }

    public static class MockReusedSessionPermanentStorage implements ReusedSessionPermanentStorage {

        private byte[] stored;

        @Override
        public ReusedSessionStore loadStore() {
            if (stored == null) {
                return null;
            }

            try {
                return SerializationUtils.deserializeFromBytes(ReusedSessionStoreImpl.class, stored);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void writeStore(ReusedSessionStore store) {
            try {
                stored = SerializationUtils.serializeToBytes(store);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public class MockRemoteWebDriverFactory extends RemoteWebDriverFactory {

        @Override
        protected RemoteWebDriver createRemoteDriver(URL remoteAddress, Capabilities desiredCapabilities) {
            return new RemoteWebDriver(hubUrl, desiredCapabilities);
        }
    }
}
