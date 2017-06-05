package org.jboss.arquillian.drone.webdriver.binary.downloading;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.arquillian.spacelift.execution.ExecutionException;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.DummyRepositoryGitHubSource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.LocalBinarySource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class DownloaderTestCase {

    private static String TEST_DRONE_TARGET_DIRECTORY = "target" + File.separator + "drone-test" + File.separator;
    public static final String TEST_DOWNLOAD_DIRECTORY =
        TEST_DRONE_TARGET_DIRECTORY + "test-downloads" + File.separator;
    private File downloadDir = new File(TEST_DOWNLOAD_DIRECTORY);

    @Rule
    public final SystemOutRule outContent = new SystemOutRule().enableLog();

    @Before
    public void deleteDirectory() throws IOException {
        FileUtils.deleteDirectory(downloadDir);
    }

    @Test
    public void testDownloadFromGitHub() throws Exception {

        URL url = new URL(DummyRepositoryGitHubSource.URL_TO_LATEST_RELEASE);
        File download = Downloader.download(downloadDir, url);

        assertThat(download).isNotNull().isFile();
        DummyRepositoryGitHubSource
            .assertThatCorrectReleaseWasDownloaded(DummyRepositoryGitHubSource.LATEST_RELEASE, download);
        long lastModified = download.lastModified();

        // now it should use already downloaded file
        File secondDownload = Downloader.download(downloadDir, url);
        assertThat(secondDownload.lastModified()).isEqualTo(lastModified)
            .as("Timestamp of the new downloaded file should be same as the previous one");
    }

    @Test(expected = ExecutionException.class)
    public void testWrongUrlDownload() throws MalformedURLException {
        Downloader.download(downloadDir, new URL("https://abc/123"));
    }

    @Test
    public void testDoubleDownloadFromLocalSource() throws MalformedURLException {
        File downloaded = Downloader.download(downloadDir, LocalBinarySource.LATEST_FILE.toURI().toURL());
        LocalBinarySource.assertThatCorrectFileWasDownloaded(true, downloaded);

        Downloader.download(downloadDir, LocalBinarySource.LATEST_FILE.toURI().toURL());
        assertThat(outContent.getLog()).containsOnlyOnce("Drone: downloading");
    }

    @After
    public void cleanUp() throws IOException {
        FileUtils.deleteDirectory(downloadDir);
    }
}
