package org.jboss.arquillian.drone.webdriver.binary.downloading.source.GitHubSourceTestCase;

import io.specto.hoverfly.junit.dsl.ResponseBuilder;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.http.HttpStatus;
import org.assertj.core.api.JUnitSoftAssertions;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.GeckoDriverGitHubSource;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LatestReleaseTestCase {

    private static final String CACHED_CONTENT = "{\"lastModified\":\"Tue, 31 Jan 2017 17:16:07 GMT\"," +
            "\"asset\":" +
            "{\"version\":\"v0.14.0\"," +
            "\"url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux64.tar.gz\"}" +
            "}";

    private static final String RESPONSE_BODY = "{\"url\":\"https://api.github.com/repos/mozilla/geckodriver/releases/5317999\",\"assets_url\":\"https://api.github.com/repos/mozilla/geckodriver/releases/5317999/assets\",\"upload_url\":\"https://uploads.github.com/repos/mozilla/geckodriver/releases/5317999/assets{?name,label}\",\"html_url\":\"https://github.com/mozilla/geckodriver/releases/tag/v0.14.0\",\"id\":5317999,\"tag_name\":\"v0.14.0\",\"target_commitish\":\"master\",\"name\":\"\",\"draft\":false,\"author\":{\"login\":\"AutomatedTester\",\"id\":128518,\"avatar_url\":\"https://avatars.githubusercontent.com/u/128518?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/AutomatedTester\",\"html_url\":\"https://github.com/AutomatedTester\",\"followers_url\":\"https://api.github.com/users/AutomatedTester/followers\",\"following_url\":\"https://api.github.com/users/AutomatedTester/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/AutomatedTester/subscriptions\",\"organizations_url\":\"https://api.github.com/users/AutomatedTester/orgs\",\"repos_url\":\"https://api.github.com/users/AutomatedTester/repos\",\"events_url\":\"https://api.github.com/users/AutomatedTester/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/AutomatedTester/received_events\",\"type\":\"User\",\"site_admin\":false},\"prerelease\":false,\"created_at\":\"2017-01-31T17:07:43Z\",\"published_at\":\"2017-01-31T17:14:54Z\",\"assets\":[{\"url\":\"https://api.github.com/repos/mozilla/geckodriver/releases/assets/3097112\",\"id\":3097112,\"name\":\"geckodriver-v0.14.0-arm7hf.tar.gz\",\"label\":\"\",\"uploader\":{\"login\":\"AutomatedTester\",\"id\":128518,\"avatar_url\":\"https://avatars.githubusercontent.com/u/128518?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/AutomatedTester\",\"html_url\":\"https://github.com/AutomatedTester\",\"followers_url\":\"https://api.github.com/users/AutomatedTester/followers\",\"following_url\":\"https://api.github.com/users/AutomatedTester/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/AutomatedTester/subscriptions\",\"organizations_url\":\"https://api.github.com/users/AutomatedTester/orgs\",\"repos_url\":\"https://api.github.com/users/AutomatedTester/repos\",\"events_url\":\"https://api.github.com/users/AutomatedTester/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/AutomatedTester/received_events\",\"type\":\"User\",\"site_admin\":false},\"content_type\":\"application/gzip\",\"state\":\"uploaded\",\"size\":2065266,\"download_count\":6774,\"created_at\":\"2017-01-31T17:14:54Z\",\"updated_at\":\"2017-01-31T17:14:54Z\",\"browser_download_url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-arm7hf.tar.gz\"},{\"url\":\"https://api.github.com/repos/mozilla/geckodriver/releases/assets/3097114\",\"id\":3097114,\"name\":\"geckodriver-v0.14.0-linux32.tar.gz\",\"label\":\"\",\"uploader\":{\"login\":\"AutomatedTester\",\"id\":128518,\"avatar_url\":\"https://avatars.githubusercontent.com/u/128518?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/AutomatedTester\",\"html_url\":\"https://github.com/AutomatedTester\",\"followers_url\":\"https://api.github.com/users/AutomatedTester/followers\",\"following_url\":\"https://api.github.com/users/AutomatedTester/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/AutomatedTester/subscriptions\",\"organizations_url\":\"https://api.github.com/users/AutomatedTester/orgs\",\"repos_url\":\"https://api.github.com/users/AutomatedTester/repos\",\"events_url\":\"https://api.github.com/users/AutomatedTester/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/AutomatedTester/received_events\",\"type\":\"User\",\"site_admin\":false},\"content_type\":\"application/gzip\",\"state\":\"uploaded\",\"size\":1901222,\"download_count\":2083,\"created_at\":\"2017-01-31T17:15:00Z\",\"updated_at\":\"2017-01-31T17:15:00Z\",\"browser_download_url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux32.tar.gz\"},{\"url\":\"https://api.github.com/repos/mozilla/geckodriver/releases/assets/3097130\",\"id\":3097130,\"name\":\"geckodriver-v0.14.0-linux64.tar.gz\",\"label\":\"\",\"uploader\":{\"login\":\"AutomatedTester\",\"id\":128518,\"avatar_url\":\"https://avatars.githubusercontent.com/u/128518?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/AutomatedTester\",\"html_url\":\"https://github.com/AutomatedTester\",\"followers_url\":\"https://api.github.com/users/AutomatedTester/followers\",\"following_url\":\"https://api.github.com/users/AutomatedTester/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/AutomatedTester/subscriptions\",\"organizations_url\":\"https://api.github.com/users/AutomatedTester/orgs\",\"repos_url\":\"https://api.github.com/users/AutomatedTester/repos\",\"events_url\":\"https://api.github.com/users/AutomatedTester/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/AutomatedTester/received_events\",\"type\":\"User\",\"site_admin\":false},\"content_type\":\"application/gzip\",\"state\":\"uploaded\",\"size\":1832331,\"download_count\":296463,\"created_at\":\"2017-01-31T17:15:42Z\",\"updated_at\":\"2017-01-31T17:15:43Z\",\"browser_download_url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux64.tar.gz\"},{\"url\":\"https://api.github.com/repos/mozilla/geckodriver/releases/assets/3106292\",\"id\":3106292,\"name\":\"geckodriver-v0.14.0-macos.tar.gz\",\"label\":\"\",\"uploader\":{\"login\":\"AutomatedTester\",\"id\":128518,\"avatar_url\":\"https://avatars.githubusercontent.com/u/128518?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/AutomatedTester\",\"html_url\":\"https://github.com/AutomatedTester\",\"followers_url\":\"https://api.github.com/users/AutomatedTester/followers\",\"following_url\":\"https://api.github.com/users/AutomatedTester/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/AutomatedTester/subscriptions\",\"organizations_url\":\"https://api.github.com/users/AutomatedTester/orgs\",\"repos_url\":\"https://api.github.com/users/AutomatedTester/repos\",\"events_url\":\"https://api.github.com/users/AutomatedTester/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/AutomatedTester/received_events\",\"type\":\"User\",\"site_admin\":false},\"content_type\":\"application/gzip\",\"state\":\"uploaded\",\"size\":1295330,\"download_count\":64869,\"created_at\":\"2017-02-01T22:46:11Z\",\"updated_at\":\"2017-02-01T22:46:12Z\",\"browser_download_url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-macos.tar.gz\"},{\"url\":\"https://api.github.com/repos/mozilla/geckodriver/releases/assets/3097171\",\"id\":3097171,\"name\":\"geckodriver-v0.14.0-win32.zip\",\"label\":\"\",\"uploader\":{\"login\":\"AutomatedTester\",\"id\":128518,\"avatar_url\":\"https://avatars.githubusercontent.com/u/128518?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/AutomatedTester\",\"html_url\":\"https://github.com/AutomatedTester\",\"followers_url\":\"https://api.github.com/users/AutomatedTester/followers\",\"following_url\":\"https://api.github.com/users/AutomatedTester/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/AutomatedTester/subscriptions\",\"organizations_url\":\"https://api.github.com/users/AutomatedTester/orgs\",\"repos_url\":\"https://api.github.com/users/AutomatedTester/repos\",\"events_url\":\"https://api.github.com/users/AutomatedTester/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/AutomatedTester/received_events\",\"type\":\"User\",\"site_admin\":false},\"content_type\":\"application/zip\",\"state\":\"uploaded\",\"size\":2265388,\"download_count\":16373,\"created_at\":\"2017-01-31T17:20:44Z\",\"updated_at\":\"2017-01-31T17:20:44Z\",\"browser_download_url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-win32.zip\"},{\"url\":\"https://api.github.com/repos/mozilla/geckodriver/releases/assets/3097133\",\"id\":3097133,\"name\":\"geckodriver-v0.14.0-win64.zip\",\"label\":\"\",\"uploader\":{\"login\":\"AutomatedTester\",\"id\":128518,\"avatar_url\":\"https://avatars.githubusercontent.com/u/128518?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/AutomatedTester\",\"html_url\":\"https://github.com/AutomatedTester\",\"followers_url\":\"https://api.github.com/users/AutomatedTester/followers\",\"following_url\":\"https://api.github.com/users/AutomatedTester/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/AutomatedTester/subscriptions\",\"organizations_url\":\"https://api.github.com/users/AutomatedTester/orgs\",\"repos_url\":\"https://api.github.com/users/AutomatedTester/repos\",\"events_url\":\"https://api.github.com/users/AutomatedTester/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/AutomatedTester/received_events\",\"type\":\"User\",\"site_admin\":false},\"content_type\":\"application/zip\",\"state\":\"uploaded\",\"size\":2233991,\"download_count\":106756,\"created_at\":\"2017-01-31T17:16:09Z\",\"updated_at\":\"2017-01-31T17:16:09Z\",\"browser_download_url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-win64.zip\"}],\"tarball_url\":\"https://api.github.com/repos/mozilla/geckodriver/tarball/v0.14.0\",\"zipball_url\":\"https://api.github.com/repos/mozilla/geckodriver/zipball/v0.14.0\",\"body\":\"### Changed\\r\\n- Firefox process is now terminated and session ended when the last window is closed\\r\\n- WebDriver library updated to version 0.20.0\\r\\n\\r\\n### Fixed\\r\\n- Stacktraces are now included when the error originates from within the Rust stack\\r\\n- HTTPD now returns correct response headers for `Content-Type` and `Cache-Control` thanks to @jugglinmike\"}";

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode();

    //@ClassRule
    //public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("hoverfly/gh.simulation.mozilla@geckodriver.release.latest.json"));

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private File tmpFolder;
    private GeckoDriverGitHubSource geckoDriverGitHubSource;
    private HttpClient httpClientSpy;
    private GitHubLastUpdateCache cacheSpy;

    @Before
    public void wireComponentsUnderTest() throws IOException {
        this.tmpFolder = folder.newFolder();
        this.httpClientSpy = spy(new HttpClient());
        this.cacheSpy = spy(new GitHubLastUpdateCache(tmpFolder));
        this.geckoDriverGitHubSource = new GeckoDriverGitHubSource(httpClientSpy, cacheSpy);
    }

    @Test
    public void should_load_release_information_from_gh_and_store_in_cache() throws Exception {
        // given
        hoverflyRule.simulate(dsl(service("https://api.github.com")
                .get("/repos/mozilla/geckodriver/releases/latest")
                .queryParam("page", 1)
                .willReturn(
                        ResponseBuilder.response().header("Last-Modified", "Tue, 31 Jan 2017 17:16:07 GMT").body(RESPONSE_BODY)
                )));

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

    @Test
    public void should_load_release_information_from_gh_and_overwrite_cache_when_last_modified_changed() throws Exception {
        // given
        String cached_content = "{\"lastModified\":\"Mon, 30 Jan 2017 17:16:07 GMT\"," +
                "\"asset\":" +
                "{\"version\":\"v0.14.0\"," +
                "\"url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux64.tar.gz\"}" +
                "}";

        hoverflyRule.simulate(dsl(service("https://api.github.com")
                .get("/repos/mozilla/geckodriver/releases/latest")
                .header(HttpHeaders.IF_MODIFIED_SINCE, "Mon, 30 Jan 2017 17:16:07 GMT")
                .queryParam("page", 1)
                .willReturn(
                        ResponseBuilder.response().header("Last-Modified", "Tue, 31 Jan 2017 17:16:07 GMT").body(RESPONSE_BODY)
                )));
        createCacheFile("gh.cache.mozilla@geckodriver.json", cached_content);

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
