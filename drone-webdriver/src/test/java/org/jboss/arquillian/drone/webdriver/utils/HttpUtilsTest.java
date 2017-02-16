package org.jboss.arquillian.drone.webdriver.utils;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.apache.http.ssl.*", "javax.net.ssl.*"})
@PrepareForTest(HttpUtils.class)
public class HttpUtilsTest {

    private final String url = "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/latest";

    @Before
    public void setPartialMock() throws IOException {
        PowerMockito.spy(HttpUtils.class);
    }

    @Test
    public void test_normal_http_request_returns_non_empty_response() throws IOException {
        Mockito.when(HttpUtils.loadConfig("LastModified")).thenReturn(null);
        String json = HttpUtils.sentGetRequest(url);
        assertThat(json).isNotEmpty();
    }

    @Test
    public void test_normal_http_request_stores_last_modified_header_value() throws IOException {
        Mockito.when(HttpUtils.loadConfig("LastModified")).thenReturn(null);
        HttpUtils.sentGetRequest(url);

        File configFile = new File("config.properties");
        assertThat(configFile).exists();

        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));
        assertThat(properties.getProperty("LastModified")).isEqualTo("Tue, 20 Dec 2016 13:27:15 GMT");
    }

    @Test
    public void test_conditional_http_request_returns_null() throws IOException {
        Mockito.when(HttpUtils.loadConfig("LastModified")).thenReturn("Tue, 20 Dec 2016 13:27:15 GMT");
        String json = HttpUtils.sentGetRequest(url);
        assertThat(json).isNull();
    }

}
