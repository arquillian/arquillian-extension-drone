package org.jboss.arquillian.drone.webdriver.utils;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHubLastUpdateCacheTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private GitHubLastUpdateCache gitHubLastUpdateCache;
    private File tmpFolder;

    @Before
    public void createGithubUpdateCache() throws IOException {
        tmpFolder = folder.newFolder();
        gitHubLastUpdateCache = new GitHubLastUpdateCache(tmpFolder);
    }

    @Test
    public void should_store_external_binary() throws Exception {
        // given
        final ExternalBinary externalBinary = new ExternalBinary("1.0.0.Final", "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/assets/2857399");
        final String releasesId = "4968399";

        // when
        gitHubLastUpdateCache.store(externalBinary, releasesId, LocalDateTime.now());

        // then
        assertThat(tmpFolder.listFiles()).containsOnly(new File(tmpFolder.getAbsolutePath() + "/gh.cache.4968399.json"));
    }

    @Test
    public void should_load_external_binary() throws Exception {
        // given
        final ExternalBinary storedExternalBinary = new ExternalBinary("1.0.0.Final", "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/assets/2857399");
        final String releasesId = "4968399";
        gitHubLastUpdateCache.store(storedExternalBinary, releasesId, LocalDateTime.now());

        // when
        final ExternalBinary loadedExternalLibrary = gitHubLastUpdateCache.load(releasesId, ExternalBinary.class);

        // then
        assertThat(loadedExternalLibrary).isEqualTo(storedExternalBinary);
    }

    @Test
    public void should_retrieve_modification_date() throws Exception {
        // given
        final ExternalBinary storedExternalBinary = new ExternalBinary("1.0.0.Final", "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/assets/2857399");
        final String releasesId = "4968399";
        gitHubLastUpdateCache.store(storedExternalBinary, releasesId, LocalDateTime.now().minusDays(2));

        // when
        LocalDateTime modificationDate = gitHubLastUpdateCache.lastModificationOf(releasesId);

        // then
        assertThat(modificationDate).isBefore(LocalDateTime.now());
    }


}