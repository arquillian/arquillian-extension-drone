package org.jboss.arquillian.drone.webdriver.binary.process;

import org.jboss.arquillian.drone.webdriver.binary.handler.SeleniumServerBinaryHandler;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class SeleniumServerTestCase extends AbstractTestTestBase {

    private static Logger log = Logger.getLogger(SeleniumServerExecutor.class.toString());
    private OutputStream logCapturingStream;
    private StreamHandler customLogHandler;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    private String seleniumServerBinary;
    private DesiredCapabilities capabilities;
    private URL url;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(SeleniumServerExecutor.class);
    }

    @Before
    public void initialise() throws Exception {
        seleniumServerBinary =
                new SeleniumServerBinaryHandler(new DesiredCapabilities()).downloadAndPrepare().toString();
        capabilities = new DesiredCapabilities();
        url = new URL("http://localhost:4444/wd/hub/");

        attachLogCapture();
        setUpStreams();
    }

    @Test
    @InSequence(1)
    public void should_start_selenium_server_with_serverArgs_debug() throws Exception {

        final String browser = "chrome";
        final String seleniumServerArgs = "-debug -role node";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, seleniumServerArgs));

        final String command = parseLogger();

        assertThat(command).contains(seleniumServerArgs);
        assertThat(outContent.toString()).contains("DEBUG");
    }

    @Test
    @InSequence(2)
    public void should_start_selenium_server_with_no_serverArgs() throws Exception {

        final String browser = "chrome";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, null));

        final String command = parseLogger();

        assertThat(command).endsWith(String.valueOf(url.getPort()));
        assertThat(outContent.toString()).contains("Selenium Server is up and running");
    }

    @Test
    @InSequence(3)
    public void should_start_selenium_server_with_hub_and_node() throws Exception {

        final String browser = "chrome";

        // start selenium Grid Hub at port 4444
        String seleniumServerArgs = "-role hub -browserTimeout 1000";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, seleniumServerArgs));

        String hubCommand = parseLogger();

        assertThat(hubCommand).contains(seleniumServerArgs);
        assertThat(outContent.toString()).contains("Selenium Grid hub is up and running");

        // start selenium grid node at port 5555 and register it with the Hub.
        String seleniumServerArgs2 = "-role node";
        URL nodeUrl = new URL("http://localhost:5555");

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, nodeUrl, seleniumServerArgs2));

        String nodeCommand = parseLogger();

        assertThat(nodeCommand).contains(seleniumServerArgs2);
        assertThat(outContent.toString()).contains("The node is registered to the hub and ready to use");
    }

    @After
    public void stopSeleniumServer() {
        cleanUpStreams();
        fire(new AfterSuite());
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
        System.setErr(new PrintStream(errContent));
    }

    private void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }
}
