package org.jboss.arquillian.drone.webdriver.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HttpClient {

    public static final String UTF_8 = "UTF-8";

    private final Logger log = Logger.getLogger(HttpClient.class.getName());

    public Response get(String url) throws IOException {
        return get(url, Collections.emptyMap(), UTF_8);
    }

    public Response get(String url, String charset) throws IOException {
        return get(url, Collections.emptyMap(), charset);
    }

    public Response get(String url, Map<String, String> headers) throws IOException {
        return get(url, headers, UTF_8);
    }

    public Response get(String url, Map<String, String> headers, String charset) throws IOException {
        try (CloseableHttpClient client = createHttpClient()) {
            final HttpGet request = new HttpGet(url);
            addHeaders(headers, request);
            String message = "Sending request: " + request + " with headers: " + Arrays.asList(request.getAllHeaders());
            log.log(PropertySecurityAction.isArquillianDebug() ? Level.INFO : Level.FINE, message);
            final HttpResponse response = client.execute(request);
            return Response.from(response, charset);
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

        private int statusCode;
        private final String payload;
        private final Map<String, String> headers;

        public Response(int statusCode, String payload, Map<String, String> headers) {
            this.statusCode = statusCode;
            this.payload = payload;
            this.headers = headers;
        }

        public static Response from(HttpResponse response) throws IOException {
            return from(response, UTF_8);
        }

        public static Response from(HttpResponse response, String charset) throws IOException {
            final HttpEntity entity = response.getEntity();
            final String payload;
            if (entity == null) {
                payload = null;
            } else {
                payload = EntityUtils.toString(entity, charset);
            }
            final Map<String, String> headers = new HashMap<>();
            for (Header header : response.getAllHeaders()) {
                // Names should be case-insensitive
                headers.put(header.getName().toLowerCase(), header.getValue());
            }
            int statusCode = 0;
            if (response.getStatusLine() != null) {
                statusCode = response.getStatusLine().getStatusCode();
            }
            return new Response(statusCode, payload, headers);
        }

        public boolean hasPayload() {
            return this.payload != null && !this.payload.isEmpty();
        }

        public String getPayload() {
            return payload;
        }

        public String getHeader(String header) {
            return headers.get(header.toLowerCase());
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
