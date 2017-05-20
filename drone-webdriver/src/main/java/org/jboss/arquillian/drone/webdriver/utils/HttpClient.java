package org.jboss.arquillian.drone.webdriver.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClient {

    private final Logger log = Logger.getLogger(HttpClient.class.getName());

    public Response get(String url) throws IOException {
        return get(url, Collections.emptyMap());
    }

    public Response get(String url, Map<String, String> headers) throws IOException {
        try (CloseableHttpClient client = createHttpClient()) {
            final HttpGet request = new HttpGet(url);
            addHeaders(headers, request);
            String message = "Sending request: " + request + " with headers: " + Arrays.asList(request.getAllHeaders());
            log.log(PropertySecurityAction.isArquillianDebug() ? Level.INFO : Level.FINE, message);
            final HttpResponse response = client.execute(request);
            return Response.from(response);
        }
    }

    private CloseableHttpClient createHttpClient() {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.useSystemProperties();
        return httpClientBuilder.build();
    }

    private void addHeaders(Map<String, String> headers, HttpGet request) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.addHeader(header.getKey(), header.getValue());
        }
    }

    public static class Response {
        private final String payload;
        private final Map<String, String> headers;

        public Response(String payload, Map<String, String> headers) {
            this.payload = payload;
            this.headers = headers;
        }

        public static Response from(HttpResponse response) throws IOException {
            final HttpEntity entity = response.getEntity();
            final String payload;
            if (entity == null) {
                payload = null;
            } else {
                payload = EntityUtils.toString(entity, "UTF-8");
            }
            final Map<String, String> headers = new HashMap<>();
            for (Header header : response.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
            return new Response(payload, headers);
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
    }
}
