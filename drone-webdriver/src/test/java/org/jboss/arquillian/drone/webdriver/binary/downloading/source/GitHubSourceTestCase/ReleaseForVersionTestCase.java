package org.jboss.arquillian.drone.webdriver.binary.downloading.source.GitHubSourceTestCase;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.GeckoDriverGitHubSource;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class ReleaseForVersionTestCase {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("hoverfly/gh.simulation.mozilla@geckodriver.releases.json"));

    private GeckoDriverGitHubSource geckoDriverGitHubSource;
    private HttpClient httpClientSpy;
    private GitHubLastUpdateCache cacheSpy;


    @Before
    public void wireComponentsUnderTest() throws IOException {
        this.httpClientSpy = spy(new HttpClient());
        this.geckoDriverGitHubSource = new GeckoDriverGitHubSource(httpClientSpy, cacheSpy);
    }

    @Test
    public void should_load_release_information_from_gh_for_given_version() throws Exception {
        // given
        final String expectedVersion = "v0.13.0";

        // when
        final ExternalBinary release = geckoDriverGitHubSource.getReleaseForVersion(expectedVersion);

        // then
        assertThat(release.getVersion()).isEqualTo(expectedVersion);
    }

    @Test
    public void should_return_null_when_release_for_version_not_found() throws Exception {
        // given
        final String expectedVersion = "v0.24.0";

        // when
        final ExternalBinary release = geckoDriverGitHubSource.getReleaseForVersion(expectedVersion);

        // then
        assertThat(release).isNull();
    }

}
