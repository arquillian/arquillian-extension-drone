package org.jboss.arquillian.drone.webdriver.factory;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.htmlunit.DroneHtmlUnitDriver;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.drone.webdriver.factory.HtmlUnitDriverFactory.webClientOptions;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlUnitDriverTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldOverrideWebClientOptions() throws IOException {
        final HtmlUnitDriverFactory htmlUnitDriverFactory = new HtmlUnitDriverFactory();
        final WebDriverConfiguration configuration = getMockedConfiguration("ThrowExceptionOnFailingStatusCode=true");
        final WebDriver driver = htmlUnitDriverFactory.createInstance(configuration);
        final DroneHtmlUnitDriver htmlUnitDriver = (DroneHtmlUnitDriver) driver;

        MockWebConnection webConnection = new MockWebConnection();
        webConnection.setDefaultResponse("http://foo.com", 503, "Service Unavailable", "text/plain");

        WebClient webClient = htmlUnitDriver.getWebClient();
        webClient.setWebConnection(webConnection);

        thrown.expect(WebDriverException.class);
        thrown.expectMessage(
            "com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException: 503 Service Unavailable for http://foo.com/");

        driver.get("http://foo.com");
        driver.quit();
    }

    @Test
    public void shouldSetWebClientOptionsForHtmlUnitDriver() throws IOException {
        final HtmlUnitDriverFactory htmlUnitDriverFactory = new HtmlUnitDriverFactory();
        final WebDriverConfiguration configuration = getMockedConfiguration("Timeout=300; JavaScriptEnabled=false");
        final WebDriver driver = htmlUnitDriverFactory.createInstance(configuration);
        final DroneHtmlUnitDriver htmlUnitDriver = (DroneHtmlUnitDriver) driver;
        final WebClient webClient = htmlUnitDriver.getWebClient();
        final WebClientOptions webClientOptions = webClient.getOptions();

        Assertions.assertThat(webClientOptions.isJavaScriptEnabled()).isFalse();
        Assertions.assertThat(webClientOptions.getTimeout()).isEqualTo(300);
    }

    private WebDriverConfiguration getMockedConfiguration(String option) {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        if (Validate.nonEmpty(option)) {
            capabilities.setCapability(webClientOptions, option);
        }
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);

        when(configuration.getCapabilities()).thenReturn(capabilities);
        when(configuration.getImplementationClass())
            .thenReturn(new BrowserCapabilitiesList.HtmlUnit().getImplementationClassName());

        return configuration;
    }
}

