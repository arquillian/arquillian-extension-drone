package org.jboss.arquillian.drone.webdriver.factory;

import com.google.gson.Gson;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;

import static org.mockito.Mockito.when;

public class HeadlessBrowserChromeOptionsTest {

    @Test
    public void shouldSetChromeOptionsForChromeHeadlessDriver() throws IOException {
        ChromeDriverFactory chromeDriverFactory = new ChromeDriverFactory();

        Capabilities chromeCaps =
            new ImmutableCapabilities(new BrowserCapabilitiesList.ChromeHeadless().getRawCapabilities());
        WebDriverConfiguration configuration = getMockedConfiguration(chromeCaps);

        Object chromeOptions = chromeDriverFactory.getChromeOptions(configuration, true);

        Assertions.assertThat(new Gson().toJson(chromeOptions)).contains("headless");
    }

    private WebDriverConfiguration getMockedConfiguration(Capabilities capabilities) {
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);

        when(configuration.getCapabilities()).thenReturn(capabilities);
        when(configuration.getBrowserName()).thenReturn("chromeheadless");
        return configuration;
    }
}
