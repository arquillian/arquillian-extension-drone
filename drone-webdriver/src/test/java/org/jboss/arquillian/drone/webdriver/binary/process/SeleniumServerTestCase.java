package org.jboss.arquillian.drone.webdriver.binary.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.drone.webdriver.binary.handler.SeleniumServerBinaryHandler;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.MockBrowserCapabilitiesRegistry;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.assertj.core.api.Assertions.assertThat;

public class SeleniumServerTestCase extends AbstractTestTestBase {

    private static Logger log = Logger.getLogger(SeleniumServerExecutor.class.toString());
    private OutputStream logCapturingStream;
    private StreamHandler customLogHandler;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private String seleniumServerBinary;
    private DesiredCapabilities capabilities;
    private URL url;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(SeleniumServerExecutor.class);
    }

    @Before
    public void initialise() throws Exception {
        capabilities = new DesiredCapabilities();
        // use selenium server version defined in arquillian.xml
        String selSerVersion = getSeleniumServerVersion(MockBrowserCapabilitiesRegistry.getArquillianDescriptor());
        if (!Validate.empty(selSerVersion)) {
            capabilities
                .setCapability(SeleniumServerBinaryHandler.SELENIUM_SERVER_VERSION_PROPERTY, selSerVersion);
        }

        seleniumServerBinary = new SeleniumServerBinaryHandler(capabilities).downloadAndPrepare().toString();

        url = new URL("http://localhost:5555/wd/hub/");

        attachLogCapture();
        setUpStreams();
    }

    @Test
    public void should_start_selenium_server_with_serverArgs_debug() throws Exception {

        final String browser = "chrome";
        final String seleniumServerArgs = "-debug -role node";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, seleniumServerArgs));

        final String command = parseLogger();

        assertThat(command).contains(seleniumServerArgs);
        assertThat(outContent.toString()).contains("DEBUG");
    }

    @Test
    public void should_start_selenium_server_with_no_serverArgs() throws Exception {

        final String browser = "chrome";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, null));

        final String command = parseLogger();

        assertThat(command).endsWith(String.valueOf(url.getPort()));
        assertThat(outContent.toString()).contains("Selenium Server is up and running");
    }

    @Test
    public void should_start_selenium_server_as_hub() throws Exception {

        final String browser = "chrome";

        String seleniumServerArgs = "-role hub -browserTimeout 1000";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, seleniumServerArgs));

        String hubCommand = parseLogger();

        assertThat(hubCommand).contains(seleniumServerArgs);
        assertThat(outContent.toString()).contains("Selenium Grid hub is up and running");
    }

    @After
    public void stopSeleniumServer() {
        cleanUpStreams();
        fire(new AfterSuite());
    }

    private String getSeleniumServerVersion(ArquillianDescriptor arquillian) {
        ExtensionDef webdriver = arquillian.extension("webdriver");
        Map<String, String> props = webdriver.getExtensionProperties();
        return props.get("seleniumServerVersion");
    }

    private String parseLogger() throws IOException {

        final String capturedLog = getTestCapturedLog();
        final String expectedLogPart = "Running Selenium server process: java .*$";

        final Pattern pattern = Pattern.compile(expectedLogPart);
        final Matcher matcher = pattern.matcher(capturedLog);
        String command;

        if (matcher.find()) ;
        {
            command = matcher.group();
        }
        return command;
    }

    private void attachLogCapture() {
        logCapturingStream = new ByteArrayOutputStream();
        Handler[] handlers = log.getParent().getHandlers();
        customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
        log.addHandler(customLogHandler);
    }

    private String getTestCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

    private void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    private void cleanUpStreams() {
        System.setOut(System.out);
    }
}
