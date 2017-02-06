package org.jboss.arquillian.drone.webdriver.binary.process;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.drone.webdriver.binary.handler.SeleniumServerBinaryHandler;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeleniumServerTestCase extends AbstractTestTestBase {

    private static Logger log = Logger.getLogger(SeleniumServerExecutor.class.toString());
    private OutputStream logCapturingStream;
    private StreamHandler customLogHandler;

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
    }

    @Test
    public void test_command_contains_serverArgs() throws Exception {

        final String browser = "chrome";
        final String seleniumServerArgs = "-debug";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, seleniumServerArgs));

        final String capturedLog = getTestCapturedLog();
        final String output = parseLogger(capturedLog);

        Assertions.assertThat(output.contains(seleniumServerArgs));
    }

    @Test
    public void test_command_does_not_contain_serverArgs() throws Exception {

        final String browser = "chrome";

        fire(new StartSeleniumServer(seleniumServerBinary, browser, capabilities, url, null));

        final String capturedLog = getTestCapturedLog();
        final String output = parseLogger(capturedLog);

        Assertions.assertThat(output.endsWith(String.valueOf(url.getPort())));
    }

    @After
    public void stopSeleniumServer(){
        fire(new AfterSuite());
    }

    private String parseLogger(String capturedLog) {

        final String expectedLogPart = "Running Selenium server process: java .*$";
        final Pattern pattern = Pattern.compile(expectedLogPart);
        final Matcher matcher = pattern.matcher(capturedLog);
        String line;

        if (matcher.find()) ;
        {
            line = matcher.group();
        }
        return line;
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
}
