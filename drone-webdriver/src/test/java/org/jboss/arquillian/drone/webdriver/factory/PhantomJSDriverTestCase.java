package org.jboss.arquillian.drone.webdriver.factory;

import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PhantomJSDriverTestCase {

    @Test
    public void testOpenSimplePage() throws IOException {
        PhantomJSDriverFactory phantomJSDriverFactory = new PhantomJSDriverFactory();

        DesiredCapabilities phantomJSCaps =
            new DesiredCapabilities(new BrowserCapabilitiesList.PhantomJS().getRawCapabilities());
        WebDriverConfiguration configuration = getMockedConfiguration(phantomJSCaps);

        WebDriver driver = phantomJSDriverFactory.createInstance(configuration);
        URL page = this.getClass().getClassLoader().getResource("simple.html");
        driver.get(page.toString());
        Assert.assertEquals("The page title doesn't match.", "Simple Page", driver.getTitle());
        driver.quit();
    }

    @Test
    public void testReformatCLIArgumentsInCapToArray() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(outContent));

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, "--debug true");

        PhantomJSDriverFactory phantomJSDriverFactory = new PhantomJSDriverFactory();
        WebDriverConfiguration configuration = getMockedConfiguration(capabilities);

        PhantomJSDriver instance = phantomJSDriverFactory.createInstance(configuration);
        phantomJSDriverFactory.destroyInstance(instance);

        assertThat(outContent.toString()).as("The log output should contain [DEBUG] string").contains("[DEBUG]");
    }

    @After
    public void resetOutputStreams() {
        System.setOut(System.out);
        System.setErr(System.out);
    }

    private WebDriverConfiguration getMockedConfiguration(DesiredCapabilities capabilities) {
        capabilities
            .setCapability("phantomjsBinaryVersion", System.getProperty("default.supported.phantomjs.binary.version"));
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);

        when(configuration.getCapabilities()).thenReturn(capabilities);
        when(configuration.getImplementationClass())
            .thenReturn(new BrowserCapabilitiesList.PhantomJS().getImplementationClassName());

        return configuration;
    }
}
