package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.handler.ChromeDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.BDDMockito.when;

/**
 * Please be aware the test focus on issue reported as
 * https://github.com/arquillian/arquillian-extension-drone/issues/300
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class StubbedChromeStorageTestCase {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    public static final String ALL_OS_BUT_WIN64_RELEASE = "96.0.4664.45";
    public static final String NON_EXISTING_RELEASE = "1.1.1.1";

    @Mock
    private HttpClient httpClient;

    private ChromeDriverBinaryHandler.ChromeStorageSources chromeDrivers;

    @Before
    public void setMock() throws IOException {
        chromeDrivers = new ChromeDriverBinaryHandler.ChromeStorageSources("https://chromedriver.storage.googleapis.com/", httpClient);

        when(httpClient.get(endsWith("chromedriver_win64.zip"))).thenReturn(new HttpClient.Response(404,
                                                                                                    "chromedriver_win64.zip is not available",
                                                                                                    emptyMap()));

        when(httpClient.get(endsWith("chromedriver_win32.zip"))).thenReturn(new HttpClient.Response(200,
                                                                                                    "chromedriver_win32.zip is available",
                                                                                                    emptyMap()));
    }

    @Test
    public void should_find_latest_stable_release_when_exists() throws Exception {
        when(httpClient.get(endsWith("/LATEST_RELEASE"), anyString())).thenReturn(new HttpClient.Response(200,
                                                                                                          ALL_OS_BUT_WIN64_RELEASE,
                                                                                                          emptyMap()));

        when(httpClient.get(endsWith("64.zip"))).thenReturn(new HttpClient.Response(200,
                                                                                    "chromedriver 64 bit version is available",
                                                                                    emptyMap()));

        // when
        final ExternalBinary latestRelease = chromeDrivers.getLatestRelease();

        // then
        assertThat(latestRelease.getVersion(), is(ALL_OS_BUT_WIN64_RELEASE));
    }

    @Test
    public void should_find_latest_stable_release_with_32_bit_version() throws Exception {
        System.setProperty("os.name", "win");
        when(httpClient.get(endsWith("/LATEST_RELEASE"), anyString())).thenReturn(new HttpClient.Response(200,
                                                                                                          ALL_OS_BUT_WIN64_RELEASE,
                                                                                                          emptyMap()));

        assertThat(PlatformUtils.isWindows(), is(true));

        final ExternalBinary latestRelease = chromeDrivers.getLatestRelease();
        assertThat(latestRelease.getVersion(), is(ALL_OS_BUT_WIN64_RELEASE));
        assertThat(latestRelease.getUrl(), CoreMatchers.endsWith("win32.zip"));
    }

    @Test
    public void should_find_32_bit_version_on_windows_with_detected_architecture() throws Exception {
        System.setProperty("os.name", "win");

        assertThat(PlatformUtils.isWindows(), is(true));

        final ExternalBinary releaseWithArchitectureDetected = chromeDrivers.getReleaseForVersion(ALL_OS_BUT_WIN64_RELEASE);
        assertThat(releaseWithArchitectureDetected.getVersion(), is(ALL_OS_BUT_WIN64_RELEASE));
        assertThat(releaseWithArchitectureDetected.getUrl(), CoreMatchers.endsWith("win32.zip"));
    }

    @Test
    public void should_find_32_bit_version_on_windows_with_32_bit_architecture() throws Exception {
        System.setProperty("os.name", "win");

        assertThat(PlatformUtils.isWindows(), is(true));

        final ExternalBinary release = chromeDrivers.getReleaseForVersion(ALL_OS_BUT_WIN64_RELEASE, Architecture.BIT32);
        assertThat(release.getVersion(), is(ALL_OS_BUT_WIN64_RELEASE));
        assertThat(release.getUrl(), CoreMatchers.endsWith("win32.zip"));
    }

    @Test
    public void should_find_32_bit_version_on_windows_with_64_bit_architecture() throws Exception {
        System.setProperty("os.name", "win");

        assertThat(PlatformUtils.isWindows(), is(true));

        final ExternalBinary release = chromeDrivers.getReleaseForVersion(ALL_OS_BUT_WIN64_RELEASE, Architecture.BIT64);
        assertThat(release.getVersion(), is(ALL_OS_BUT_WIN64_RELEASE));
        assertThat(release.getUrl(), CoreMatchers.endsWith("win32.zip"));
    }

    @Test(expected = MissingBinaryException.class)
    public void should_throw_missing_binary_exception() throws Exception {
        System.setProperty("os.name", "win");

        assertThat(PlatformUtils.isWindows(), is(true));

        when(httpClient.get(contains(NON_EXISTING_RELEASE))).thenReturn(new HttpClient.Response(404,
                                                                                                "this version is not available",
                                                                                                emptyMap()));

        chromeDrivers.getReleaseForVersion(NON_EXISTING_RELEASE);
    }
}
