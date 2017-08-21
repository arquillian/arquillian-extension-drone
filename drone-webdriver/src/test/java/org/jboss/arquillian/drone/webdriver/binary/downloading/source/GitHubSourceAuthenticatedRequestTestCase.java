package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import java.util.Base64;
import java.util.logging.Logger;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.remote.DesiredCapabilities;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
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

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(GitHubSource.GITHUB_USERNAME_PROPERTY, "my-username");
        capabilities.setCapability(GitHubSource.GITHUB_TOKEN_PROPERTY, "my-token");

        GeckoDriverGitHubSource geckoDriverGitHubSource = new GeckoDriverGitHubSource(httpClientSpy, gitHubLastUpdateCache, capabilities);

        // when
        geckoDriverGitHubSource.getReleaseForVersion("v0.14.0");

        // then
        verify(httpClientSpy, times(1)).get(anyString(), argThat(headers -> {
            if (headers.size() > 0) {
                String authorization = headers.get("Authorization");
                if (authorization != null && authorization.startsWith("Basic ")) {
                    String tokens =
                        new String(Base64.getDecoder().decode(authorization.substring(6, authorization.length() - 1)));
                    if ("my-username:my-token".equals(tokens)) {
                        return true;
                    } else {
                        log.severe(String.format(
                            "The auth param value should contain: [my-username:my-token] but contains: [%s]",
                            tokens));
                    }
                } else {
                    log.severe("There is no header pair with key: 'Authorization' and value starting with 'Basic'");
                }
            } else {
                log.severe("The map doesn't contain any header param");
            }
            return false;
        }));
    }
}
