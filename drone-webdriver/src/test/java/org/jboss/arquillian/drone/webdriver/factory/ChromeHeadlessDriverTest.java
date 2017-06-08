package org.jboss.arquillian.drone.webdriver.factory;

import java.io.IOException;
import java.net.URL;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChromeHeadlessDriverTest {

    @BeforeClass
    public static void executeOnlyIfChromeHeadlessBrowser() {
        String browser = System.getProperty("browser");
        Assume.assumeTrue(browser.equals("chromeHeadless") || browser.equals("chromeheadless"));
    }

    @Test
    public void testOpenSimplePageUsingHeadlessChrome() throws IOException {
        ChromeDriverFactory chromeDriverFactory = new ChromeDriverFactory();

        DesiredCapabilities chromeCaps =
            new DesiredCapabilities(new BrowserCapabilitiesList.chromeHeadless().getRawCapabilities());
        WebDriverConfiguration configuration = getMockedConfiguration(chromeCaps);

        WebDriver driver = chromeDriverFactory.createInstance(configuration);
        URL page = this.getClass().getClassLoader().getResource("simple.html");
        driver.get(page.toString());
        Assert.assertEquals("The page title doesn't match.", "Simple Page", driver.getTitle());
        driver.quit();
    }

    @Test
    public void shouldSetChromeOptionsForHeadlessChromeHeadlessDriver() throws IOException {
        ChromeDriverFactory chromeDriverFactory = new ChromeDriverFactory();

        DesiredCapabilities chromeCaps =
            new DesiredCapabilities(new BrowserCapabilitiesList.chromeHeadless().getRawCapabilities());
        WebDriverConfiguration configuration = getMockedConfiguration(chromeCaps);

        Capabilities capabilities = chromeDriverFactory.getCapabilities(configuration, true);
        ChromeOptions chromeOptions = (ChromeOptions) capabilities.getCapability(ChromeOptions.CAPABILITY);

        Assertions.assertThat(chromeOptions.toJson().toString()).contains("headless");
    }

    private WebDriverConfiguration getMockedConfiguration(DesiredCapabilities capabilities) {
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);

        when(configuration.getCapabilities()).thenReturn(capabilities);
        when(configuration.getBrowser()).thenReturn("chromeHeadless");
        when(configuration.getImplementationClass())
            .thenReturn(new BrowserCapabilitiesList.chromeHeadless().getImplementationClassName());

        return configuration;
    }
}
