package org.jboss.arquillian.drone.webdriver.binary.process;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import org.jboss.arquillian.drone.webdriver.binary.handler.SeleniumServerBinaryHandler;
import org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.assertj.core.api.Assertions.assertThat;

public class SeleniumServerTestCase extends AbstractTestTestBase {

    private DesiredCapabilities capabilities;
    private URL url;
    @Rule
    public final SystemOutRule outContent = new SystemOutRule().enableLog();
    private String seleniumServerBinary;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(SeleniumServerExecutor.class);
    }

    @Before
    public void initialise() throws Exception {
        capabilities = new DesiredCapabilities();
        // use selenium server version defined in arquillian.xml
        String selSerVersion = ArqDescPropertyUtil.getSeleniumServerVersionProperty();
        if (!Validate.empty(selSerVersion)) {
            capabilities
                .setCapability(SeleniumServerBinaryHandler.SELENIUM_SERVER_VERSION_PROPERTY, selSerVersion);
        }

        seleniumServerBinary = new SeleniumServerBinaryHandler(capabilities).downloadAndPrepare().toString();

        url = new URL("http://localhost:5555/wd/hub/");

    }

    @Test
    public void should_start_selenium_server_with_serverArgs_debug() throws Exception {

        final String browser = "chrome";
        final String seleniumServerArgs = "-debug true -role node";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, seleniumServerArgs));

        verifyLogContainsRegex("^\\[Selenium server\\].+DEBUG - .+4444$");
    }

    @Test
    public void should_start_selenium_server_with_no_serverArgs() throws Exception {

        final String browser = "chrome";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, null));

        verifyLogContainsRegex("^\\[Selenium server\\].+ServerConnector.+5555.+$");
        assertThat(outContent.getLog()).contains("Selenium Server is up and running");
    }

    @Test
    public void should_start_selenium_server_as_hub() throws Exception {

        final String browser = "chrome";

        String seleniumServerArgs = "-role hub -browserTimeout 1000";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, seleniumServerArgs));

        verifyLogContainsRegex("^\\[Selenium server\\].+Nodes should register to .+5555.+$");
        assertThat(outContent.getLog()).contains("Selenium Grid hub is up and running");
    }

    @After
    public void stopSeleniumServer() {
        fire(new AfterSuite());
    }

    private void verifyLogContainsRegex(String regex) throws IOException {

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        assertThat(pattern.matcher(outContent.getLog()).find())
            .as("The log should contains part that matches regex: " + regex)
            .isTrue();
    }
}
