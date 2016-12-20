package org.jboss.arquillian.drone.webdriver.binary.downloading;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.arquillian.spacelift.execution.ExecutionException;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.DummyRepositoryGitHubSource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.LocalBinarySource;
import org.jboss.arquillian.drone.webdriver.utils.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class DownloaderTestCase {

    public static final String TEST_DOWNLOAD_DIRECTORY =
        Constants.DRONE_TARGET_DIRECTORY + "test-downloads" + File.separator;
    private File downloadDir = new File(TEST_DOWNLOAD_DIRECTORY);

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
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream stdOut = System.out;
        System.setOut(new PrintStream(outContent));

        File downloaded = Downloader.download(downloadDir, LocalBinarySource.LATEST_FILE.toURI().toURL());
        LocalBinarySource.assertThatCorrectFileWasDownloaded(true, downloaded);

        Downloader.download(downloadDir, LocalBinarySource.LATEST_FILE.toURI().toURL());
        System.setOut(stdOut);
        assertThat(outContent.toString()).containsOnlyOnce("Drone: downloading");
    }

    @After
    public void cleanUp() throws IOException {
        FileUtils.deleteDirectory(downloadDir);
    }
}
