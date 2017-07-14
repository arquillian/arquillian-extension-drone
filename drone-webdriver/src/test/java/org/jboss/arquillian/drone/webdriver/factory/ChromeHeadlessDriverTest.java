package org.jboss.arquillian.drone.webdriver.factory;

import java.io.IOException;
import java.net.URL;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil.assumeBrowserEqual;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChromeHeadlessDriverTest {

    @BeforeClass
    public static void executeOnlyIfChromeHeadlessBrowser() {
        assumeBrowserEqual("chromeheadless");
    }

    @Test
    public void testOpenSimplePageUsingChromeHeadlessBrowser() throws IOException {
        ChromeDriverFactory chromeDriverFactory = new ChromeDriverFactory();

        DesiredCapabilities chromeCaps =
            new DesiredCapabilities(new BrowserCapabilitiesList.ChromeHeadless().getRawCapabilities());
        WebDriverConfiguration configuration = getMockedConfiguration(chromeCaps);

        WebDriver driver = chromeDriverFactory.createInstance(configuration);
        URL page = this.getClass().getClassLoader().getResource("simple.html");
        driver.get(page.toString());
        Assert.assertEquals("The page title doesn't match.", "Simple Page", driver.getTitle());
        driver.quit();
    }

    private WebDriverConfiguration getMockedConfiguration(DesiredCapabilities capabilities) {
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);

        when(configuration.getCapabilities()).thenReturn(capabilities);
        when(configuration.getBrowser()).thenReturn("chromeheadless");
        when(configuration.getImplementationClass())
            .thenReturn(new BrowserCapabilitiesList.ChromeHeadless().getImplementationClassName());
        return configuration;
    }
}
