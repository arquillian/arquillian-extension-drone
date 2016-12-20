package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.handler.GoogleSeleniumStorageProvider;
import org.jboss.arquillian.drone.webdriver.utils.HttpUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.drone.webdriver.binary.downloading.source.SeleniumGoogleStorageSource.SELENIUM_BASE_STORAGE_URL;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpUtils.class)
public class LocalGoogleSeleniumStorageTestCase {

    public static final String FILE_PATH =
        "src/test/resources/files/downloading/local-selenium-storage-2016-12-21".replace("/", File.separator);

    @Before
    public void setMock() throws IOException {
        PowerMockito.mockStatic(HttpUtils.class);
        BDDMockito.when(HttpUtils.sentGetRequest(SELENIUM_BASE_STORAGE_URL)).thenReturn(
            FileUtils.readFileToString(new File(FILE_PATH), "utf-8"));
    }

    @Test
    public void testGetLatestIERelease() throws Exception {
        ExternalBinary latestRelease = GoogleSeleniumStorageProvider.getIeStorageSource(null).getLatestRelease();
        assertThat(latestRelease.getVersion()).isEqualTo("3.0");
        assertThat(latestRelease.getUrl())
            .startsWith("http://selenium-release.storage.googleapis.com/3.0/IEDriverServer_")
            .endsWith("_3.0.0.zip");
    }

    @Test
    public void testGetLatestSeleniumServerRelease() throws Exception {
        ExternalBinary latestRelease =
            GoogleSeleniumStorageProvider.getSeleniumServerStorageSource(null).getLatestRelease();
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

    private void testIEVersion(String dir, String version) throws Exception {
        ExternalBinary release = GoogleSeleniumStorageProvider.getIeStorageSource(version).getLatestRelease();
        assertThat(release.getVersion()).isEqualTo(dir);
        assertThat(release.getUrl())
            .startsWith("http://selenium-release.storage.googleapis.com/" + dir + "/IEDriverServer_")
            .endsWith("_" + version + ".zip");
    }

    private void testSeleniumServerVersion(String dir, String version) throws Exception {
        ExternalBinary release =
            GoogleSeleniumStorageProvider.getSeleniumServerStorageSource(version).getLatestRelease();
        assertThat(release.getVersion()).isEqualTo(dir);
        assertThat(release.getUrl()).startsWith(
            "http://selenium-release.storage.googleapis.com/" + dir + "/selenium-server-standalone-" + version
                + ".jar");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetNonExistingIERelease() throws Exception {
        GoogleSeleniumStorageProvider.getIeStorageSource("1.2.3").getLatestRelease();
    }
}
