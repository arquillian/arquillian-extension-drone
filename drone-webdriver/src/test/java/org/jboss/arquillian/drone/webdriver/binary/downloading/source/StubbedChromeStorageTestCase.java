package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.handler.ChromeDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.emptyMap;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StubbedChromeStorageTestCase {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    private static final String FILE_PATH =
        "src/test/resources/files/downloading/chrome-driver/chrome_drivers.xml".replace("/", File.separator);

    public static final String ALL_OS_RELEASE = "96.0.4664.45";

    @Mock
    private HttpClient httpClient;

    private ChromeDriverBinaryHandler.ChromeStorageSources chromeDrivers;

    @Before
    public void setMock() throws IOException {
        when(httpClient.get(startsWith("https://chromedriver.storage.googleapis.com/"))).thenReturn(new HttpClient.Response(
            readFileToString(new File(FILE_PATH), "utf-8").replaceAll("(?:>)(\\s*)<", "><"), emptyMap()));

        when(httpClient.get(endsWith("/LATEST_RELEASE"), anyString())).thenReturn(new HttpClient.Response(ALL_OS_RELEASE, emptyMap()));

        chromeDrivers = new ChromeDriverBinaryHandler.ChromeStorageSources("https://chromedriver.storage.googleapis.com/", httpClient);
    }

    @Test
    public void should_find_latest_stable_release_when_exists() throws Exception {
        // when
        final ExternalBinary latestRelease = chromeDrivers.getLatestRelease();

        // then
        assertThat(latestRelease.getVersion(), is(ALL_OS_RELEASE));
    }

    @Test
    public void should_find_latest_stable_release_with_32_bit_version() throws Exception {
        System.setProperty("os.name", "win");

        assertThat(PlatformUtils.isWindows(), is(true));

        final ExternalBinary latestRelease = chromeDrivers.getLatestRelease();
        assertThat(latestRelease.getVersion(), is(ALL_OS_RELEASE));
        assertThat(latestRelease.getUrl(), CoreMatchers.endsWith("win32.zip"));
    }
}
