package org.jboss.arquillian.drone.webdriver.utils;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.assertj.core.api.Condition;
import org.assertj.core.api.SoftAssertions;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
    public void should_create_nested_cache_folder() throws Exception {
        // given
        final File customCacheFolder = new File(new File(tmpFolder, "nested"), "custom-cache-folder");
        gitHubLastUpdateCache = new GitHubLastUpdateCache(customCacheFolder);

        // then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(customCacheFolder).exists().isDirectory();
        softly.assertAll();
    }

    @Test
    public void should_create_release_cache_folder() throws Exception {
        // given
        final File customCacheFolder = new File(tmpFolder, "custom-cache-folder");
        final ExternalBinary externalBinary = new ExternalBinary("1.0.0.Final",
            "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/assets/2857399");
        final String releasesId = "4968399";
        gitHubLastUpdateCache = new GitHubLastUpdateCache(customCacheFolder);

        // when
        gitHubLastUpdateCache.store(externalBinary, releasesId, ZonedDateTime.now());

        // then
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(customCacheFolder).exists().isDirectory();
        softly.assertThat(new File(customCacheFolder, "gh.cache.4968399.json")).isFile().has(new Condition<File>() {
            @Override
            public boolean matches(File value) {
                return value.length() > 0;
            }
        });
        softly.assertAll();
    }

    @Test
    public void should_store_external_binary() throws Exception {
        // given
        final ExternalBinary externalBinary = new ExternalBinary("1.0.0.Final",
            "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/assets/2857399");
        final String releasesId = "4968399";

        // when
        gitHubLastUpdateCache.store(externalBinary, releasesId, ZonedDateTime.now());

        // then
        assertThat(tmpFolder.listFiles()).containsOnly(new File(tmpFolder.getAbsolutePath() + "/gh.cache.4968399.json"));
    }

    @Test
    public void should_load_external_binary() throws Exception {
        // given
        final ExternalBinary storedExternalBinary = new ExternalBinary("1.0.0.Final",
            "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/assets/2857399");
        final String releasesId = "4968399";
        gitHubLastUpdateCache.store(storedExternalBinary, releasesId, ZonedDateTime.now());

        // when
        final ExternalBinary loadedExternalLibrary = gitHubLastUpdateCache.load(releasesId, ExternalBinary.class);

        // then
        assertThat(loadedExternalLibrary).isEqualTo(storedExternalBinary);
    }

    @Test
    public void should_retrieve_modification_date() throws Exception {
        // given
        final ExternalBinary storedExternalBinary = new ExternalBinary("1.0.0.Final",
            "https://api.github.com/repos/MatousJobanek/my-test-repository/releases/assets/2857399");
        final String releasesId = "4968399";
        gitHubLastUpdateCache.store(storedExternalBinary, releasesId, ZonedDateTime.now(ZoneId.of("GMT")).minusDays(2));

        // when
        ZonedDateTime modificationDate = gitHubLastUpdateCache.lastModificationOf(releasesId);

        // then
        assertThat(modificationDate).isBefore(ZonedDateTime.now());
    }

    @Test
    public void should_return_github_launch_date_when_no_entry_in_cache() throws Exception {
        // given
        final String releasesId = "4968399";

        // when
        ZonedDateTime modificationDate = gitHubLastUpdateCache.lastModificationOf(releasesId);

        // then
        assertThat(modificationDate).isEqualToIgnoringHours(ZonedDateTime.of(2008, 4, 10, 0, 0, 0, 0, ZoneId.of("GMT")));
    }
}
