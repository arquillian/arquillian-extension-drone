package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import io.specto.hoverfly.junit.dsl.ResponseBuilder;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import org.assertj.core.api.JUnitSoftAssertions;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.MutableCapabilities;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GitHubSourceRateLimitAndErrorTestCase {

    private static final String CACHED_CONTENT = "{\"lastModified\":\"Tue, 31 Jan 2017 17:16:07 GMT\","
        + "\"asset\":"
        + "{\"version\":\"v0.14.0\","
        + "\"url\":\"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-linux64.tar.gz\"}"
        + "}";

    private static final String SERVER_PROBLEM_RESPONSE_BODY =
        "{\"message\": \"There is an error on the GitHub server\"}";

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private final String RATE_LIMIT_RESPONSE_BODY = "{\n"
        + "  \"message\": \"API rate limit exceeded for 1.2.3.4. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)\",\n"
        + "  \"documentation_url\": \"https://developer.github.com/v3/#rate-limiting\"\n"
        + "}";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private File tmpFolder;
    private GeckoDriverGitHubSource geckoDriverGitHubSource;
    private HttpClient httpClientSpy;
    private GitHubLastUpdateCache cacheSpy;

    @Before
    public void wireComponentsUnderTest() throws IOException {
        this.tmpFolder = folder.newFolder();
        this.httpClientSpy = spy(new HttpClient());
        this.cacheSpy = spy(new GitHubLastUpdateCache(tmpFolder));
        this.geckoDriverGitHubSource = new GeckoDriverGitHubSource(httpClientSpy, cacheSpy, new MutableCapabilities());
    }

    private void simulate(String requestPath, boolean rateLimit) {
        hoverflyRule.simulate(dsl(service("https://api.github.com")
            .get(requestPath)
            .willReturn(
                rateLimit ?
                    ResponseBuilder.response()
                        .header("X-RateLimit-Remaining", "0")
                        .header("X-RateLimit-Reset", "1496393220")
                        .header("Status", "403 Forbidden")
                        .body(RATE_LIMIT_RESPONSE_BODY)
                    :
                        ResponseBuilder.response()
                            .header("Status", "500 Internal Error")
                            .body(SERVER_PROBLEM_RESPONSE_BODY)
            )));
    }

    @Test
    public void should_fail_with_rate_limit_when_load_latest_release_information_as_nothing_cached() throws Exception {
        // given
        simulate("/repos/mozilla/geckodriver/releases/latest", true);

        // expected
        exception.expect(IllegalStateException.class);
        exception.expectMessage(getRateLimitExpectedMsg(true));

        // when
        geckoDriverGitHubSource.getLatestRelease();
    }

    @Test
    public void should_fail_with_server_error_when_load_latest_release_information_as_nothing_cached() throws Exception {
        // given
        simulate("/repos/mozilla/geckodriver/releases/latest", false);

        // expected
        exception.expect(IllegalStateException.class);
        exception.expectMessage(SERVER_PROBLEM_RESPONSE_BODY);

        // when
        geckoDriverGitHubSource.getLatestRelease();
    }

    @Test
    public void should_fail_when_load_releases_information() throws Exception {
        // given
        simulate("/repos/mozilla/geckodriver/releases", true);

        // expected
        exception.expect(IllegalStateException.class);
        exception.expectMessage(getRateLimitExpectedMsg(false));

        // when
        geckoDriverGitHubSource.getReleaseForVersion("v0.14.0");
    }

    @Test
    public void should_fail_with_server_error_when_load_releases_information() throws Exception {
        // given
        simulate("/repos/mozilla/geckodriver/releases", false);

        // expected
        exception.expect(IllegalStateException.class);
        exception.expectMessage(SERVER_PROBLEM_RESPONSE_BODY);

        // when
        geckoDriverGitHubSource.getReleaseForVersion("v0.14.0");
    }

    @Test
    public void should_load_release_information_from_cache_and_log_warning() throws Exception {
        // given
        simulate("/repos/mozilla/geckodriver/releases/latest", true);
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

    private String getRateLimitExpectedMsg(boolean latest) {
        return String.format(
            "GitHub API rate limit exceeded. To get the information about the %srelease you need to wait "
                + "till the rate limit is reset which will be: %s", latest ? "latest " : "", new Date(1496393220000L));
    }

    private void createCacheFile(String fileName, String content) throws FileNotFoundException {
        try (final PrintWriter printWriter = new PrintWriter(new File(tmpFolder.getAbsolutePath() + "/" + fileName))) {
            printWriter.print(content);
            printWriter.flush();
        }
    }
}
