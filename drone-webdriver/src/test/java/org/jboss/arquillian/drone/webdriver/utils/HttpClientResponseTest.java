package org.jboss.arquillian.drone.webdriver.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

public class HttpClientResponseTest {

    @Test
    public void testHeaders() throws IOException {
        String now = ZonedDateTime.now().toString();
        Header[] headers = new Header[] { new BasicHeader("last-modified", now), new BasicHeader("Foo-Modified", now) };
        HttpClient.Response response = HttpClient.Response
                .from(new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "")) {
                    @Override
                    public Header[] getAllHeaders() {
                        return headers;
                    }
                });

        String header = response.getHeader(org.apache.http.HttpHeaders.LAST_MODIFIED);
        assertNotNull(header);
        assertEquals(now, header);
        header = response.getHeader("foo-modified");
        assertNotNull(header);
        assertEquals(now, header);
    }

}
