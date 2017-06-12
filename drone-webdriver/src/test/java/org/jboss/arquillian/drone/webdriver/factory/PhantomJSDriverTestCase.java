package org.jboss.arquillian.drone.webdriver.factory;

import java.io.IOException;
import java.net.URL;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PhantomJSDriverTestCase {

    @Rule
    public final SystemErrRule errContent = new SystemErrRule().enableLog();

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

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, "--debug true");

        PhantomJSDriverFactory phantomJSDriverFactory = new PhantomJSDriverFactory();
        WebDriverConfiguration configuration = getMockedConfiguration(capabilities);

        PhantomJSDriver instance = phantomJSDriverFactory.createInstance(configuration);
        phantomJSDriverFactory.destroyInstance(instance);

        assertThat(errContent.getLog()).as("The log output should contain [DEBUG] string").contains("[DEBUG]");
    }


    private WebDriverConfiguration getMockedConfiguration(DesiredCapabilities capabilities) {
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);

        when(configuration.getCapabilities()).thenReturn(capabilities);
        when(configuration.getImplementationClass())
            .thenReturn(new BrowserCapabilitiesList.PhantomJS().getImplementationClassName());

        return configuration;
    }
}
