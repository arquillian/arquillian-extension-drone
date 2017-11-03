package org.jboss.arquillian.drone.webdriver.factory;

import com.google.gson.Gson;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.mockito.Mockito.when;

public class HeadlessBrowserChromeOptionsTest {

    @Test
    public void shouldSetChromeOptionsForChromeHeadlessDriver() throws IOException {
        ChromeDriverFactory chromeDriverFactory = new ChromeDriverFactory();

        DesiredCapabilities chromeCaps =
            new DesiredCapabilities(new BrowserCapabilitiesList.ChromeHeadless().getRawCapabilities());
        WebDriverConfiguration configuration = getMockedConfiguration(chromeCaps);

        Capabilities capabilities = chromeDriverFactory.getCapabilities(configuration, true);
        Object chromeOptions = capabilities.getCapability(ChromeOptions.CAPABILITY);

        Assertions.assertThat(new Gson().toJson(chromeOptions)).contains("headless");
    }

    private WebDriverConfiguration getMockedConfiguration(DesiredCapabilities capabilities) {
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);

        when(configuration.getCapabilities()).thenReturn(capabilities);
        when(configuration.getBrowser()).thenReturn("chromeheadless");
        return configuration;
    }
}
