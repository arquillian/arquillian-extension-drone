package org.jboss.arquillian.drone.webdriver.utils;


import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.apache.http.ssl.*", "javax.net.ssl.*"})
@PrepareForTest(HttpClient.class)
public class HttpClientTest {

    private final String url = "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/latest";

    /*@Before
    public void setPartialMock() throws IOException {
        PowerMockito.spy(HttpClient.class);
    }

    @Test
    public void test_normal_http_request_returns_non_empty_response() throws IOException {
        Mockito.when(HttpClient.loadConfig("LastModified")).thenReturn(null);
        String json = HttpClient.sentGetRequest(url);
        assertThat(json).isNotEmpty();
    }

    @Test
    public void test_normal_http_request_stores_last_modified_header_value() throws IOException {
        Mockito.when(HttpClient.loadConfig("LastModified")).thenReturn(null);
        HttpClient.sentGetRequest(url);

        File configFile = new File("config.properties");
        assertThat(configFile).exists();

        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));
        assertThat(properties.getProperty("LastModified")).isEqualTo("Tue, 20 Dec 2016 13:27:15 GMT");
    }

    @Test
    public void test_conditional_http_request_returns_null() throws IOException {
        Mockito.when(HttpClient.loadConfig("LastModified")).thenReturn("Tue, 20 Dec 2016 13:27:15 GMT");
        String json = HttpClient.sentGetRequest(url);
        assertThat(json).isNull();
    }*/

}
