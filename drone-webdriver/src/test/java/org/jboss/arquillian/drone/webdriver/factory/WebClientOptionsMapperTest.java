package org.jboss.arquillian.drone.webdriver.factory;

import com.gargoylesoftware.htmlunit.WebClientOptions;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

public class WebClientOptionsMapperTest {

    @Test
    public void shouldSetWebClientOptions() {
        //given
        final String browserName = new BrowserCapabilitiesList.HtmlUnit().getReadableName();
        Map<String, String> map = new LinkedHashMap<>();
        map.put(browserName + "Timeout", "10");
        map.put(browserName + "JavaScriptEnabled", "false");
        map.put(browserName + "ThrowExceptionOnScriptError", "false");

        DesiredCapabilities capabilities = new DesiredCapabilities(map);
        WebClientOptions webClientOptions = new WebClientOptions();

        //when
        CapabilitiesOptionsMapper.mapCapabilities(webClientOptions, capabilities,
            browserName);

        //then
        Assertions.assertThat(webClientOptions.getTimeout()).isEqualTo(10);
        Assertions.assertThat(webClientOptions.isJavaScriptEnabled()).isFalse();
        Assertions.assertThat(webClientOptions.isThrowExceptionOnScriptError()).isFalse();
    }
}
