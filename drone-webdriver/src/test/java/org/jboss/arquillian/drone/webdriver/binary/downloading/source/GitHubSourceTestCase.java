package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class GitHubSourceTestCase {

    private static final String BASIC_PATH =
        "src/test/resources/files/downloading/github/".replace("/", File.separator);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    private HttpClient httpClient;
    private GitHubLastUpdateCache gitHubLastUpdateCache;
    private File tmpFolder;

    @Before
    public void createGithubUpdateCache() throws IOException {
        tmpFolder = folder.newFolder();
        gitHubLastUpdateCache = new GitHubLastUpdateCache(tmpFolder);
        httpClient = new HttpClient();
        mockGitHubAPIcalls();
    }

    public void mockGitHubAPIcalls() {
        stubFor(get(urlMatching("/mozilla/geckodriver/releases/latest"))
                .willReturn(aResponse()
                        .proxiedFrom("https://api.github.com/repos/")
                        .withStatus(200)
                        .withHeader("Last-Modified", "Tue, 31 Jan 2017 17:16:07 GMT")
                        .withBody("{\n" +
                                "  \"url\": \"https://api.github.com/repos/mozilla/geckodriver/releases/5317999\",\n" +
                                "  \"assets_url\": \"https://api.github.com/repos/mozilla/geckodriver/releases/5317999/assets\",\n" +
                                "  \"upload_url\": \"https://uploads.github.com/repos/mozilla/geckodriver/releases/5317999/assets{?name,label}\",\n" +
                                "  \"html_url\": \"https://github.com/mozilla/geckodriver/releases/tag/v0.14.0\",\n" +
                                "  \"id\": 5317999,\n" +
                                "  \"tag_name\": \"v0.14.0\",\n" +
                                "  \"target_commitish\": \"master\",\n" +
                                "  \"name\": \"\",\n" +
                                "  \"draft\": false,\n" +
                                "  \"author\": {\n" +
                                "    \"login\": \"AutomatedTester\",\n" +
                                "    \"id\": 128518,\n" +
                                "    \"avatar_url\": \"https://avatars.githubusercontent.com/u/128518?v=3\",\n" +
                                "    \"gravatar_id\": \"\",\n" +
                                "    \"url\": \"https://api.github.com/users/AutomatedTester\",\n" +
                                "    \"html_url\": \"https://github.com/AutomatedTester\",\n" +
                                "    \"followers_url\": \"https://api.github.com/users/AutomatedTester/followers\",\n" +
                                "    \"following_url\": \"https://api.github.com/users/AutomatedTester/following{/other_user}\",\n" +
                                "    \"gists_url\": \"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\n" +
                                "    \"starred_url\": \"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\n" +
                                "    \"subscriptions_url\": \"https://api.github.com/users/AutomatedTester/subscriptions\",\n" +
                                "    \"organizations_url\": \"https://api.github.com/users/AutomatedTester/orgs\",\n" +
                                "    \"repos_url\": \"https://api.github.com/users/AutomatedTester/repos\",\n" +
                                "    \"events_url\": \"https://api.github.com/users/AutomatedTester/events{/privacy}\",\n" +
                                "    \"received_events_url\": \"https://api.github.com/users/AutomatedTester/received_events\",\n" +
                                "    \"type\": \"User\",\n" +
                                "    \"site_admin\": false\n" +
                                "  },\n" +
                                "  \"prerelease\": false,\n" +
                                "  \"created_at\": \"2017-01-31T17:07:43Z\",\n" +
                                "  \"published_at\": \"2017-01-31T17:14:54Z\",\n" +
                                "  \"assets\": [\n" +
                                "    {\n" +
                                "      \"url\": \"https://api.github.com/repos/mozilla/geckodriver/releases/assets/3097112\",\n" +
                                "      \"id\": 3097112,\n" +
                                "      \"name\": \"geckodriver-v0.14.0-arm7hf.tar.gz\",\n" +
                                "      \"label\": \"\",\n" +
                                "      \"uploader\": {\n" +
                                "        \"login\": \"AutomatedTester\",\n" +
                                "        \"id\": 128518,\n" +
                                "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/128518?v=3\",\n" +
                                "        \"gravatar_id\": \"\",\n" +
                                "        \"url\": \"https://api.github.com/users/AutomatedTester\",\n" +
                                "        \"html_url\": \"https://github.com/AutomatedTester\",\n" +
                                "        \"followers_url\": \"https://api.github.com/users/AutomatedTester/followers\",\n" +
                                "        \"following_url\": \"https://api.github.com/users/AutomatedTester/following{/other_user}\",\n" +
                                "        \"gists_url\": \"https://api.github.com/users/AutomatedTester/gists{/gist_id}\",\n" +
                                "        \"starred_url\": \"https://api.github.com/users/AutomatedTester/starred{/owner}{/repo}\",\n" +
                                "        \"subscriptions_url\": \"https://api.github.com/users/AutomatedTester/subscriptions\",\n" +
                                "        \"organizations_url\": \"https://api.github.com/users/AutomatedTester/orgs\",\n" +
                                "        \"repos_url\": \"https://api.github.com/users/AutomatedTester/repos\",\n" +
                                "        \"events_url\": \"https://api.github.com/users/AutomatedTester/events{/privacy}\",\n" +
                                "        \"received_events_url\": \"https://api.github.com/users/AutomatedTester/received_events\",\n" +
                                "        \"type\": \"User\",\n" +
                                "        \"site_admin\": false\n" +
                                "      },\n" +
                                "      \"content_type\": \"application/gzip\",\n" +
                                "      \"state\": \"uploaded\",\n" +
                                "      \"size\": 2065266,\n" +
                                "      \"download_count\": 5185,\n" +
                                "      \"created_at\": \"2017-01-31T17:14:54Z\",\n" +
                                "      \"updated_at\": \"2017-01-31T17:14:54Z\",\n" +
                                "      \"browser_download_url\": \"https://github.com/mozilla/geckodriver/releases/download/v0.14.0/geckodriver-v0.14.0-arm7hf.tar.gz\"\n" +
                                "    }\n" )
                ));
    }

    @Test
    public void should_return_latest_release() throws Exception {

        // Given
        GeckoDriverGitHubSource geckoDriverGitHubSource = new GeckoDriverGitHubSource(httpClient, gitHubLastUpdateCache);

        // When
        ExternalBinary latestRelease = geckoDriverGitHubSource.getLatestRelease();


        // Then
        assertThat(latestRelease.getVersion()).isEqualTo("v0.14.0");
        assertThat(tmpFolder.listFiles()).containsOnly(new File(tmpFolder.getAbsolutePath() + "/gh.cache.mozilla@geckodriver.json"));
    }

    @Test
    public void should_return_latest_release_for_version() throws Exception {

        // Given
        GeckoDriverGitHubSource geckoDriverGitHubSource = new GeckoDriverGitHubSource(httpClient, gitHubLastUpdateCache);

        // When
        ExternalBinary latestRelease = geckoDriverGitHubSource.getReleaseForVersion("v0.14.0");

        // Then
        assertThat(latestRelease.getVersion()).isEqualTo("v0.14.0");
    }

    @Test
    public void should_return_latest_release_from_cache() throws Exception {

        //Given

        //When

        //Then

    }

    @Test
    public void exampleTest() throws IOException {
        final String url = "http://api.github.com/repos/MatousJobanek/my-test-repository/releases/latest";

        stubFor(get(urlEqualTo("/MatousJobanek/my-test-repository/releases/latest")).willReturn(
                aResponse()
                        .proxiedFrom("https://api.github.com/repos/")
                        .withStatus(200)
                        .withHeader("Last-Modified", "Tue, 20 Dec 2016 13:27:15 GMT")
                        .withBody("Hello")));

        HttpClient httpClient = new HttpClient();
        String response = httpClient.get(url).getPayload();
        System.out.println(httpClient.get(url).getHeader("Last_Modified"));
        System.out.println(response);
    }
}
