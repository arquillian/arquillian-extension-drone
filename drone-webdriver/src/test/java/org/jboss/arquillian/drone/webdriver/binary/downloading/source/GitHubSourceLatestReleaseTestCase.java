package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.specto.hoverfly.junit.dsl.ResponseBuilder;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.assertj.core.api.JUnitSoftAssertions;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.MutableCapabilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GitHubSourceLatestReleaseTestCase {

    private static final String CACHED_CONTENT = "{\"lastModified\":\"Tue, 31 Jan 2017 17:16:07 GMT\","
        +
        "\"asset\":"
        +
        "{\"version\":\"v0.14.0\","
        +
        "\"url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux64.tar.gz\"}"
        +
        "}";

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private final String RESPONSE_BODY;
    private File tmpFolder;
    private GeckoDriverGitHubSource geckoDriverGitHubSource;
    private HttpClient httpClientSpy;
    private GitHubLastUpdateCache cacheSpy;

    public GitHubSourceLatestReleaseTestCase() throws IOException {
        this.RESPONSE_BODY = loadResponseBody("hoverfly/gh.simulation.mozilla@geckodriver.release.latest.json");
    }

    @Before
    public void wireComponentsUnderTest() throws IOException {
        this.tmpFolder = folder.newFolder();
        this.httpClientSpy = spy(new HttpClient());
        this.cacheSpy = spy(new GitHubLastUpdateCache(tmpFolder));
        this.geckoDriverGitHubSource = new GeckoDriverGitHubSource(httpClientSpy, cacheSpy, new MutableCapabilities());
    }

    @Test
    public void should_load_release_information_from_gh_and_store_in_cache() throws Exception {
        // given
        hoverflyRule.simulate(dsl(service("https://api.github.com")
            .get("/repos/mozilla/geckodriver/releases/latest")
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
    // With this test we make sure that the internal date is converted to RFC 2126 with GMT prior to GH call
    public void should_load_release_information_from_gh_and_overwrite_cache_when_last_modified_changed()
        throws Exception {
        // given
        String cached_content =
            "{\"lastModified\":\"Sun, 01 Jan 2017 18:16:07 +0100\","
                +
                // Here we store the date with the offset
                "\"asset\":"
                +
                "{\"version\":\"v0.14.0\","
                +
                "\"url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux64.tar.gz\"}"
                +
                "}";

        hoverflyRule.simulate(dsl(service("https://api.github.com")
            .get("/repos/mozilla/geckodriver/releases/latest")
            .header(HttpHeaders.IF_MODIFIED_SINCE,
                "Sun, 01 Jan 2017 17:16:07 GMT") // But we expect it to be sent in GMT, as this is how we match the request
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

    private String loadResponseBody(String path) throws IOException {
        String response;
        ClassLoader classLoader = getClass().getClassLoader();
        try (final InputStreamReader inputStreamReader = new InputStreamReader(classLoader.getResourceAsStream(path),
            Charsets.UTF_8)) {
            response = CharStreams.toString(inputStreamReader);
        }
        return response;
    }

    private void createCacheFile(String fileName, String content) throws FileNotFoundException {
        try (final PrintWriter printWriter = new PrintWriter(new File(tmpFolder.getAbsolutePath() + "/" + fileName))) {
            printWriter.print(content);
            printWriter.flush();
        }
    }
}
