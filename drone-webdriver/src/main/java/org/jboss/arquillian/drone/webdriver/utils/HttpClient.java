package org.jboss.arquillian.drone.webdriver.utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class HttpClient {

    public static class Response {
        private final String payload;
        private final Map<String, String> headers;

        public Response(String payload, Map<String, String> headers) {
            this.payload = payload;
            this.headers = headers;
        }

        public boolean hasPayload() {
            return this.payload != null && !this.payload.isEmpty();
        }

        public String getPayload() {
            return payload;
        }

        public String getHeader(String header) {
            return headers.get(header);
        }

        public static Response from(HttpResponse response) throws IOException {
            final String payload = EntityUtils.toString(response.getEntity(), "UTF-8");
            final Map<String, String> headers = new HashMap<>();
            for (Header header : response.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
            return new Response(payload, headers);
        }
    }

    public Response get(String url) throws IOException {
        return get(url, Collections.emptyMap());
    }

    public Response get(String url, Map<String, String> headers) throws IOException {
        final CloseableHttpClient client = HttpClientBuilder.create().build(); // TODO close http client
        final HttpGet request = new HttpGet(url);
        addHeaders(headers, request);

        final HttpResponse response = client.execute(request);
        return Response.from(response);
    }

    private void addHeaders(Map<String, String> headers, HttpGet request) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.addHeader(header.getKey(), header.getValue());
        }
    }

}
