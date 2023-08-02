package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.MutableCapabilities;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static org.jboss.arquillian.drone.webdriver.binary.downloading.source.GitHubSource.AUTHORIZATION_HEADER_KEY;
import static org.jboss.arquillian.drone.webdriver.binary.downloading.source.GitHubSource.BASIC_AUTHORIZATION_HEADER_VALUE_PREFIX;
import static org.jboss.arquillian.drone.webdriver.binary.downloading.source.GitHubSource.GITHUB_TOKEN_PROPERTY;
import static org.jboss.arquillian.drone.webdriver.binary.downloading.source.GitHubSource.GITHUB_USERNAME_PROPERTY;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GitHubSourceAuthenticatedRequestTestCase {

    private static final Logger log = Logger.getLogger(GitHubSourceAuthenticatedRequestTestCase.class.getName());

    @ClassRule
    public static HoverflyRule hoverflyRule =
        HoverflyRule.inSimulationMode(classpath("hoverfly/gh.simulation.mozilla@geckodriver.releases.json"));

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_add_authentication_header_to_request() throws Exception {
        // given
        HttpClient httpClientSpy = spy(new HttpClient());
        GitHubLastUpdateCache gitHubLastUpdateCache = new GitHubLastUpdateCache(folder.newFolder());

        String username = "my-username";
        String token = "my-token";

        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability(GITHUB_USERNAME_PROPERTY, username);
        capabilities.setCapability(GITHUB_TOKEN_PROPERTY, token);

        GeckoDriverGitHubSource geckoDriverGitHubSource =
            new GeckoDriverGitHubSource(httpClientSpy, gitHubLastUpdateCache, capabilities);

        // when
        geckoDriverGitHubSource.getReleaseForVersion("v0.14.0");

        // then
        verify(httpClientSpy, times(1)).get(anyString(),
                                            (Map<String, String>) argThat(headers -> containsAuthHeaderParam((Map<String, String>) headers, username + ":" + token)));
    }

    private boolean containsAuthHeaderParam(Map<String, String> headers, String authPair) {
        if (headers.size() > 0) {
            String authorization = headers.get(AUTHORIZATION_HEADER_KEY);
            if (authorization != null && authorization.startsWith(BASIC_AUTHORIZATION_HEADER_VALUE_PREFIX)) {
                String tokens =
                    new String(Base64.getDecoder().decode(authorization.substring(6, authorization.length() - 1)));
                if (authPair.equals(tokens)) {
                    return true;
                } else {
                    log.severe(String.format(
                        "The auth param value should contain: [%s] but contains: [%s]", authPair, tokens));
                }
            } else {
                log.severe(String.format("There is no header pair with key: [%s] and value starting with [%s]",
                    AUTHORIZATION_HEADER_KEY, BASIC_AUTHORIZATION_HEADER_VALUE_PREFIX));
            }
        } else {
            log.severe("The map doesn't contain any header param");
        }
        return false;
    }
}
