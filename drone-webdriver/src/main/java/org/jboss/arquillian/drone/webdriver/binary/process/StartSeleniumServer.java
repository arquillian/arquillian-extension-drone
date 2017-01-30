package org.jboss.arquillian.drone.webdriver.binary.process;

import java.net.URL;

import org.jboss.arquillian.core.spi.event.Event;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class StartSeleniumServer implements Event {

    private String pathToSeleniumServerBinary;
    private String browser;
    private DesiredCapabilities capabilities;
    private URL url;
    private String seleniumServerArgs;

    public StartSeleniumServer(String pathToSeleniumServerBinary, String browser,
        DesiredCapabilities capabilities, URL url, String seleniumServerArgs) {
        this.pathToSeleniumServerBinary = pathToSeleniumServerBinary;
        this.browser = browser;
        this.capabilities = capabilities;
        this.url = url;
        this.seleniumServerArgs = seleniumServerArgs;
    }

    public String getPathToSeleniumServerBinary() {
        return pathToSeleniumServerBinary;
    }

    public void setPathToSeleniumServerBinary(String pathToSeleniumServerBinary) {
        this.pathToSeleniumServerBinary = pathToSeleniumServerBinary;
    }

    public String getSeleniumServerArgs() {
        return seleniumServerArgs;
    }

    public void setSeleniumServerArgs(String seleniumServerArgs) {
        this.seleniumServerArgs = seleniumServerArgs;
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

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
