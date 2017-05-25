package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class DummyRepositoryGitHubSource extends GitHubSource {

    public static final String LATEST_RELEASE = "8.0.0.Final";
    public static final String BASE_API_URL = "https://api.github.com/repos/MatousJobanek/my-test-repository/";
    public static final String BASE_URL_TO_RELEASE =
        "https://github.com/MatousJobanek/my-test-repository/releases/download/%s/release.zip";
    public static final String URL_TO_LATEST_RELEASE = String.format(BASE_URL_TO_RELEASE, LATEST_RELEASE);

    public DummyRepositoryGitHubSource(HttpClient httpClient, GitHubLastUpdateCache gitHubLastUpdateCache) {
        super("MatousJobanek", "my-test-repository", httpClient, gitHubLastUpdateCache);
    }

    public static void assertThatCorrectReleaseWasDownloaded(String version, File releaseZip) throws Exception {
        assertThat(releaseZip).isFile();

        File extraction = BinaryFilesUtils.extract(releaseZip);
        assertThat(extraction).isDirectory();

        File[] versionFiles = extraction.listFiles();
        assertThat(versionFiles).as("The extracted dir should contain version file").hasSize(1);
        assertThat(versionFiles[0]).isFile().hasName("version");

        List<String> content = Files.readAllLines(versionFiles[0].toPath());
        assertThat(content).hasSize(1).contains(version);
    }

    @Override
    public String getFileNameRegexToDownload(String version) {
        return "release.zip";
    }
}
