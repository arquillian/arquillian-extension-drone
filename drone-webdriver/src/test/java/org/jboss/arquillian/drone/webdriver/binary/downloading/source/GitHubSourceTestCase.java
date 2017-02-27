package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import io.specto.hoverfly.junit.dsl.ResponseBuilder;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.http.HttpStatus;
import org.assertj.core.api.JUnitSoftAssertions;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GitHubSourceTestCase {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("hoverfly/gh.simulation.mozilla@geckodriver.json"));

    private static final String CACHED_CONTENT = "{\"lastModified\":\"Tue, 31 Jan 2017 17:16:07 GMT\"," +
            "\"asset\":" +
                    "{\"version\":\"v0.14.0\"," +
                    "\"url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux64.tar.gz\"}" +
            "}";

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private File tmpFolder;
    private GeckoDriverGitHubSource geckoDriverGitHubSource;
    private HttpClient httpClientSpy;
    private GitHubLastUpdateCache cacheSpy;

    @Before
    public void createGithubUpdateCache() throws IOException {
        this.tmpFolder = folder.newFolder();
        this.httpClientSpy = spy(new HttpClient());
        this.cacheSpy = spy(new GitHubLastUpdateCache(tmpFolder));
        this.geckoDriverGitHubSource = new GeckoDriverGitHubSource(httpClientSpy, cacheSpy);
    }

    @Test
    public void should_load_release_information_from_gh_and_store_in_cache() throws Exception {
        // given
        final String expectedVersion = "v0.14.0";

        // when
        final ExternalBinary latestRelease = geckoDriverGitHubSource.getLatestRelease();

        // then
        softly.assertThat(latestRelease.getVersion()).isEqualTo(expectedVersion);
        softly.assertThat(tmpFolder.listFiles()).hasSize(1);
        softly.assertThat(tmpFolder.listFiles()[0]).isFile().hasContent(CACHED_CONTENT);
        verify(httpClientSpy).get(anyString(), anyMap());
        verify(cacheSpy, times(0)).load(anyString(), any());
        verify(cacheSpy, times(1)).store(any(), anyString(), any());
    }

    @Test
    public void should_load_release_information_from_cache_when_not_changed() throws Exception {
        // given
        hoverflyRule.simulate(dsl(service("https://api.github.com")
                .get("/repos/mozilla/geckodriver/releases/latest")
                .header(HttpHeaders.IF_MODIFIED_SINCE, "Tue, 31 Jan 2017 17:16:07 GMT")
                .queryParam("page", 1)
                .willReturn(
                        ResponseBuilder.response().status(HttpStatus.SC_NOT_MODIFIED)
                )));
        createCacheFile("gh.cache.mozilla@geckodriver.json", CACHED_CONTENT);

        final String expectedVersion = "v0.14.0";

        // when
        final ExternalBinary latestRelease = geckoDriverGitHubSource.getLatestRelease();

        // then
        softly.assertThat(latestRelease.getVersion()).isEqualTo(expectedVersion);
        softly.assertThat(tmpFolder.listFiles()).hasSize(1);
        softly.assertThat(tmpFolder.listFiles()[0]).isFile().hasContent(CACHED_CONTENT);
        verify(httpClientSpy).get(anyString(), anyMap());
        verify(cacheSpy, times(1)).load(anyString(), any());
        verify(cacheSpy, times(0)).store(any(), anyString(), any());
    }

    private void createCacheFile(String fileName, String content) throws FileNotFoundException {
        try (final PrintWriter printWriter = new PrintWriter(new File(tmpFolder.getAbsolutePath() + "/" + fileName))) {
            printWriter.print(content);
            printWriter.flush();
        }
    }

    // Cleaning up system properties for proxies
    // We need this until hoverfly cleans up properly
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";

    private static String ORG_HTTP_PROXY_HOST = "";
    private static String ORG_HTTPS_PROXY_HOST = "";
    private static String ORG_HTTP_NON_PROXY_HOSTS = "";
    private static String ORG_HTTP_PROXY_PORT = "";
    private static String ORG_HTTPS_PROXY_PORT = "";

    static { // @BeforeClass won't work due to order of junit execution - hoverfly would start first
        ORG_HTTP_PROXY_HOST = System.getProperty(HTTP_PROXY_HOST, "");
        ORG_HTTPS_PROXY_HOST = System.getProperty(HTTPS_PROXY_HOST, "");
        ORG_HTTP_NON_PROXY_HOSTS = System.getProperty(HTTP_NON_PROXY_HOSTS, "");
        ORG_HTTP_PROXY_PORT = System.getProperty(HTTP_PROXY_PORT, "");
        ORG_HTTPS_PROXY_PORT = System.getProperty(HTTPS_PROXY_PORT, "");
    }

    @AfterClass
    public static void clean() {
        System.setProperty(HTTP_PROXY_HOST, ORG_HTTP_PROXY_HOST);
        System.setProperty(HTTPS_PROXY_HOST, ORG_HTTPS_PROXY_HOST);
        System.setProperty(HTTP_NON_PROXY_HOSTS, ORG_HTTP_NON_PROXY_HOSTS);
        System.setProperty(HTTP_PROXY_PORT, ORG_HTTP_PROXY_PORT);
        System.setProperty(HTTPS_PROXY_PORT, ORG_HTTPS_PROXY_PORT);
    }

}
