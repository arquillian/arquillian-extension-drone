package org.jboss.arquillian.drone.webdriver.factory;

import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.htmlunit.WebClientOptions;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

public class WebClientOptionsMapperTest {

    @Test
    public void shouldSetWebClientOptions() {
        //given
        final String browserName = new BrowserCapabilitiesList.HtmlUnit().getReadableName();
        Map<String, String> map = new LinkedHashMap<>();
        map.put(browserName + "Timeout", "10");
        map.put(browserName + "JavaScriptEnabled", "false");
        map.put(browserName + "ThrowExceptionOnScriptError", "false");

        Capabilities capabilities = new MutableCapabilities(map);
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
