package org.jboss.arquillian.drone.webdriver.htmlunit;

import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.Capabilities;

public class HtmlUnitDriver extends org.openqa.selenium.htmlunit.HtmlUnitDriver {

    public HtmlUnitDriver(Capabilities capabilities) {
        super(capabilities);
    }

    @Override
    public WebClient modifyWebClient(WebClient client) {
        // set multiple options for webclient here as per requirement. Nothing here now.

        return client;
    }

    @Override
    public WebClient getWebClient() {
        return super.getWebClient();
    }
}
