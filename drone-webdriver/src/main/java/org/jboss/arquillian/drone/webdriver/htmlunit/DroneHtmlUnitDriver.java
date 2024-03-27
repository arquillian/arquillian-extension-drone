package org.jboss.arquillian.drone.webdriver.htmlunit;

import org.htmlunit.WebClient;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class DroneHtmlUnitDriver extends HtmlUnitDriver {

    public DroneHtmlUnitDriver(Capabilities capabilities) {
        super(capabilities);
    }

    @Override
    public WebClient modifyWebClient(WebClient client) {
        // set multiple options for webclient here as per requirement in future.

        return client;
    }

    @Override
    public WebClient getWebClient() {
        return super.getWebClient();
    }
}
