package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.openqa.selenium.remote.DesiredCapabilities;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GitHubSourceLatestReleaseFromTagsTestCase {

    private static final String CACHED_CONTENT = "{\"lastModified\":\"Tue, 28 Mar 2017 05:23:15 GMT\"," +
        "\"asset\":" +
        "{\"version\":\"2.1.1\"," +
        "\"url\":\"https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2\"}" +
        "}";

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private final String RESPONSE_BODY;
    private File tmpFolder;
    private PhantomJSSourceForLatestRelease phantomJSSource;
    private HttpClient httpClientSpy;
    private GitHubLastUpdateCache cacheSpy;

    public GitHubSourceLatestReleaseFromTagsTestCase() throws IOException {
        this.RESPONSE_BODY = loadResponseBody("hoverfly/gh.simulation.ariya@phantomjs.tags.json");
    }

    @Before
    public void wireComponentsUnderTest() throws IOException {
        this.tmpFolder = folder.newFolder();
        this.httpClientSpy = spy(new HttpClient());
        this.cacheSpy = spy(new GitHubLastUpdateCache(tmpFolder));
        this.phantomJSSource = new PhantomJSSourceForLatestRelease(httpClientSpy, cacheSpy);
    }

    @Test
    public void should_load_release_information_from_gh_and_store_in_cache() throws Exception {
        // given
        hoverflyRule.simulate(dsl(service("https://api.github.com")
            .get("/repos/ariya/phantomjs/tags")
            .willReturn(
                ResponseBuilder.response()
                    .header("Last-Modified", "Tue, 28 Mar 2017 05:23:15 GMT")
                    .body(RESPONSE_BODY)
            )));

        final String expectedVersion = "2.1.1";

        // when
        final ExternalBinary latestRelease = phantomJSSource.getLatestRelease();

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
            .get("/repos/ariya/phantomjs/tags")
            .header(HttpHeaders.IF_MODIFIED_SINCE, "Tue, 28 Mar 2017 05:23:15 GMT")
            .willReturn(
                ResponseBuilder.response().status(HttpStatus.SC_NOT_MODIFIED)
            )));
        createCacheFile("gh.cache.ariya@phantomjs.json", CACHED_CONTENT);

        final String expectedVersion = "2.1.1";

        // when
        final ExternalBinary latestRelease = phantomJSSource.getLatestRelease();

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
            "{\"lastModified\":\"Sun, 01 Jan 2017 18:16:07 +0100\"," + // Here we store the date with the offset
                "\"asset\":" +
                "{\"version\":\"2.1.0\"," +
                "\"url\":\"https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.0-linux-x86_64.tar.bz2\"}"
                +
                "}";

        hoverflyRule.simulate(dsl(service("https://api.github.com")
            .get("/repos/ariya/phantomjs/tags")
            .header(HttpHeaders.IF_MODIFIED_SINCE,
                "Sun, 01 Jan 2017 17:16:07 GMT") // But we expect it to be sent in GMT, as this is how we match the request
            .willReturn(
                ResponseBuilder.response()
                    .header("Last-Modified", "Tue, 28 Mar 2017 05:23:15 GMT")
                    .body(RESPONSE_BODY)
            )));
        createCacheFile("gh.cache.ariya@phantomjs.json", cached_content);

        final String expectedVersion = "2.1.1";

        // when
        final ExternalBinary latestRelease = phantomJSSource.getLatestRelease();

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
        try (
            final InputStreamReader inputStreamReader = new InputStreamReader(classLoader.getResourceAsStream(path),
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

    class PhantomJSSourceForLatestRelease extends PhantomJSGitHubBitbucketSource {
        private String TAGS_URL = "/tags";
        private String TAG_NAME = "name";

        PhantomJSSourceForLatestRelease(HttpClient httpClient,
            GitHubLastUpdateCache gitHubLastUpdateCache) {
            super(httpClient, gitHubLastUpdateCache, new DesiredCapabilities());
        }

        public ExternalBinary getLatestRelease() throws Exception {
            final HttpClient.Response response =
                sentGetRequestWithPagination(getProjectUrl() + TAGS_URL, 1, lastModificationHeader());
            final ExternalBinary latestPhantomJSBinary;

            if (response.hasPayload()) {
                JsonArray releaseTags = getGson().fromJson(response.getPayload(), JsonElement.class).getAsJsonArray();
                if (releaseTags.size() == 0) {
                    return null;
                }
                String version = releaseTags.get(0).getAsJsonObject().get(TAG_NAME).getAsString();
                latestPhantomJSBinary = new ExternalBinary(version);

                latestPhantomJSBinary.setUrl(getUrlForVersion(version));
                getCache().store(latestPhantomJSBinary, getUniqueKey(), extractModificationDate(response));
            } else {
                latestPhantomJSBinary = getCache().load(getUniqueKey(), ExternalBinary.class);
            }
            return latestPhantomJSBinary;
        }
    }
}
