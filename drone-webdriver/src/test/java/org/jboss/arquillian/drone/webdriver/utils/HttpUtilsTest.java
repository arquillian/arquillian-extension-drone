package org.jboss.arquillian.drone.webdriver.utils;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;


//@RunWith(PowerMockRunner.class)
//@PowerMockIgnore({"org.apache.http.ssl.*", "javax.net.ssl.*"})
//@PrepareForTest(HttpUtils.class)
public class HttpUtilsTest {

    private final String url = "http://localhost:8089/MatousJobanek/my-test-repository/releases/latest";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void exampleTest() throws IOException {
        stubFor(get(urlEqualTo("/MatousJobanek/my-test-repository/releases/latest")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withHeader("Last_Modified", "Tue, 20 Dec 2016 13:27:15 GMT")
                        .withBody("Hello")));

        HttpClient httpClient = new HttpClient();
        String response = httpClient.get(url).getPayload();
        System.out.println(httpClient.get(url).getHeader("Last_Modified"));
        System.out.println(response);

    }

/*
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
*/

}
