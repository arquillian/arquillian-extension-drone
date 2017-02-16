package org.jboss.arquillian.drone.webdriver.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.GitHubSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.apache.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.apache.http.HttpHeaders.LAST_MODIFIED;
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class HttpUtils {

    private static String LastModified = null;
    private static Properties properties = new Properties();

    public static String sentGetRequest(String url) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        Boolean is_gitHubSource = url.contains(GitHubSource.latestUrl);

        if (is_gitHubSource) {
            LastModified = loadConfig("LastModified");
            if (LastModified != null) {
                request.addHeader(IF_MODIFIED_SINCE, LastModified);
            }
        }

        HttpResponse response = client.execute(request);

        if (is_gitHubSource) {
            if (response.getStatusLine().getStatusCode() == SC_NOT_MODIFIED) {
                return null;
            }
            LastModified = response.getFirstHeader(LAST_MODIFIED).getValue();
            storeConfig("LastModified", LastModified);
        }

        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    public static String loadConfig(String key) throws IOException {
        String value = null;
        File configFile = new File("config.properties");

        if (configFile.exists()) {
            properties.load(new FileInputStream(configFile));
            value = properties.getProperty(key);
        }
        return value;
    }

    public static void storeConfig(String key, String value) throws IOException {
        properties.setProperty(key, value);
        properties.store(new FileOutputStream("config.properties"), null);
    }

}
