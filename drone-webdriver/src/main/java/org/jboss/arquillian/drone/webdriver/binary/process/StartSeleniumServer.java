package org.jboss.arquillian.drone.webdriver.binary.process;

import org.jboss.arquillian.core.spi.event.Event;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class StartSeleniumServer implements Event {

    private String pathToSeleniumServerBinary;
    private String browser;
    private DesiredCapabilities capabilities;

    public StartSeleniumServer(String pathToSeleniumServerBinary, String browser,
        DesiredCapabilities capabilities) {
        this.pathToSeleniumServerBinary = pathToSeleniumServerBinary;
        this.browser = browser;
        this.capabilities = capabilities;
    }

    public String getPathToSeleniumServerBinary() {
        return pathToSeleniumServerBinary;
    }

    public void setPathToSeleniumServerBinary(String pathToSeleniumServerBinary) {
        this.pathToSeleniumServerBinary = pathToSeleniumServerBinary;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
    }
}
