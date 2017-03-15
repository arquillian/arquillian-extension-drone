package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.ClassRule;
import org.junit.Test;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;
import static org.assertj.core.api.Assertions.assertThat;

public class GitHubSourceReleaseForVersionTestCase {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("hoverfly/gh.simulation.mozilla@geckodriver.releases.json"));

    GeckoDriverGitHubSource geckoDriverGitHubSource = new GeckoDriverGitHubSource(new HttpClient(), new GitHubLastUpdateCache());

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
