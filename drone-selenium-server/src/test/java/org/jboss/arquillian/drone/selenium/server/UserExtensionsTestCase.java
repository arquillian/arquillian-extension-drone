package org.jboss.arquillian.drone.selenium.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerConfigured;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerStarted;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerStopped;
import org.jboss.arquillian.drone.selenium.server.impl.SeleniumServerCreator;
import org.jboss.arquillian.drone.selenium.server.impl.SeleniumServerDestructor;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.server.SeleniumServer;

/**
 * Test for loading of user extensions by a Selenium server
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UserExtensionsTestCase extends AbstractTestTestBase {

    private static final String USER_EXTENSIONS_FILE = "src/main/resources/simple-extension.js";

    @Spy
    SeleniumServerConfiguration configuration = new SeleniumServerConfiguration();

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(SeleniumServerCreator.class);
        extensions.add(SeleniumServerDestructor.class);
    }

    @Before
    public void setMocks() {
        bind(SuiteScoped.class, SeleniumServerConfiguration.class, configuration);
        Mockito.when(configuration.isSkip()).thenReturn(false);
        Mockito.when(configuration.getUserExtensions()).thenReturn(USER_EXTENSIONS_FILE);
    }

    @Test
    public void userExtensionsLoaded() throws Exception {

        fire(new SeleniumServerConfigured(configuration));

        SeleniumServer server = getManager().getContext(SuiteContext.class).getObjectStore().get(SeleniumServer.class);

        Assert.assertNotNull("Selenium configuration object is present in context", server);
        Assert.assertNotNull("Selenium Server configuration is present", server.getConfiguration());
        Assert.assertNotNull("Selenium Server user extensions were set", server.getConfiguration().getUserExtensions());
        InputStream is = null;
        try {
            is = server.getResourceAsStream("simple-extension.js");
        } catch (IOException e) {
            Assert.fail("simple-extension.js was loaded.");
        }
        String simpleExtension = IOUtil.asUTF8String(is);
        Assert.assertTrue("simple-extension.js contains doTypeRepeated(locator,text) function",
                simpleExtension.contains("doTypeRepeated"));

        assertEventFired(SeleniumServerStarted.class, 1);

        fire(new AfterSuite());

        assertEventFired(SeleniumServerStopped.class, 1);
    }
}