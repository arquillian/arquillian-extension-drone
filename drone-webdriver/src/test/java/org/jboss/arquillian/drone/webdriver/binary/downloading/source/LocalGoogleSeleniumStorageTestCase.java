package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.handler.GoogleSeleniumStorageProvider;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.drone.webdriver.binary.downloading.source.SeleniumGoogleStorageSource.SELENIUM_BASE_STORAGE_URL;
import static org.mockito.ArgumentMatchers.startsWith;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalGoogleSeleniumStorageTestCase {

    public static final String FILE_PATH =
        "src/test/resources/files/downloading/local-selenium-storage-2016-12-21".replace("/", File.separator);

    @Mock
    private HttpClient httpClient;

    @Before
    public void setMock() throws IOException {
        BDDMockito.when(httpClient.get(startsWith(SELENIUM_BASE_STORAGE_URL))).thenReturn(new HttpClient.Response(
            FileUtils.readFileToString(new File(FILE_PATH), "utf-8"), Collections.emptyMap()));
    }

    @Test
    public void testGetLatestIERelease() throws Exception {
        ExternalBinary latestRelease =
            GoogleSeleniumStorageProvider.getIeStorageSource(null, httpClient).getLatestRelease();
        assertThat(latestRelease.getVersion()).isEqualTo("3.0");
        assertThat(latestRelease.getUrl())
            .startsWith("http://selenium-release.storage.googleapis.com/3.0/IEDriverServer_")
            .endsWith("_3.0.0.zip");
    }

    @Test
    public void testGetLatestSeleniumServerRelease() throws Exception {
        ExternalBinary latestRelease =
            GoogleSeleniumStorageProvider.getSeleniumServerStorageSource(null, httpClient).getLatestRelease();
        assertThat(latestRelease.getVersion()).isEqualTo("3.0");
        assertThat(latestRelease.getUrl())
            .startsWith("http://selenium-release.storage.googleapis.com/3.0/selenium-server-standalone-3.0.1.jar");
    }

    @Test
    public void testAllZeroReleases() throws Exception {
        for (int i = 39; i <= 53; i++) {
            String dir = "2." + i;
            testIEVersion(dir, dir + ".0");
            testSeleniumServerVersion(dir, dir + ".0");
        }
        for (int i = 1; i <= 4; i++) {
            // for beta versions, there were no IE bits released
            testSeleniumServerVersion("3.0-beta" + i, "3.0.0-beta" + i);
        }
        testIEVersion("3.0", "3.0.0");
        testSeleniumServerVersion("3.0", "3.0.0");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetNonExistingIERelease() throws Exception {
        GoogleSeleniumStorageProvider.getIeStorageSource("1.2.3", httpClient).getLatestRelease();
    }

    private void testIEVersion(String dir, String version) throws Exception {
        ExternalBinary release = GoogleSeleniumStorageProvider.getIeStorageSource(version, httpClient).getLatestRelease();
        assertThat(release.getVersion()).isEqualTo(dir);
        assertThat(release.getUrl())
            .startsWith("http://selenium-release.storage.googleapis.com/" + dir + "/IEDriverServer_")
            .endsWith("_" + version + ".zip");
    }

    private void testSeleniumServerVersion(String dir, String version) throws Exception {
        ExternalBinary release =
            GoogleSeleniumStorageProvider.getSeleniumServerStorageSource(version, httpClient).getLatestRelease();
        assertThat(release.getVersion()).isEqualTo(dir);
        assertThat(release.getUrl()).startsWith(
            "http://selenium-release.storage.googleapis.com/" + dir + "/selenium-server-standalone-" + version
                + ".jar");
    }
}
