package org.jboss.arquillian.drone.webdriver.binary.process;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.awaitility.Awaitility;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.openqa.selenium.MutableCapabilities;

public class SeleniumServerTestCase extends AbstractTestTestBase {
    @Rule
    public final SystemOutRule outContent = new SystemOutRule().enableLog();

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(SeleniumServerExecutor.class);
    }

    @Test
    public void should_start_selenium_server_with_serverArgs_node() throws Exception {

        final String browser = "chrome";
        final String seleniumServerArgs = "node";

        fire(new StartSeleniumServer(browser, new MutableCapabilities(), getUrl(5566), seleniumServerArgs));

        verifyLogContainsRegex(".*\\[NodeServer.execute.*Started Selenium node.*");
    }

    @Test
    public void should_start_selenium_server_with_no_serverArgs() throws Exception {

        final String browser = "chrome";

        fire(new StartSeleniumServer(browser, new MutableCapabilities(), getUrl(5577), null));

        verifyLogContainsRegex(".*\\[Standalone.execute.*Started Selenium Standalone.*");
    }

    @Test
    public void should_start_selenium_server_as_hub() throws Exception {

        final String browser = "chrome";

        String seleniumServerArgs = "hub";

        fire(new StartSeleniumServer(browser, new MutableCapabilities(), getUrl(5588), seleniumServerArgs));

        verifyLogContainsRegex(".*\\[Hub.execute.*Started Selenium Hub.*");
    }

    private URL getUrl(int port) throws MalformedURLException {
        return new URL(String.format("http://localhost:%d/wd/hub/", port));
    }

    private void verifyLogContainsRegex(String regex) throws IOException {

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        Awaitility.await("The log should contains part that matches regex: " + regex)
            .atMost(2, TimeUnit.SECONDS)
            .until(() -> pattern.matcher(outContent.getLog()).find());
    }
}
