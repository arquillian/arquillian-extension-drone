package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.io.IOException;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.handler.EdgeDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StubbedEdgeStorageTestCase {

    public static final String NON_EXISTING_VERSION = "96.0.1028.123";

    public static final String ALL_OS_RELEASE = "96.0.1054.26";

    @Mock
    private HttpClient httpClient;

    private EdgeDriverBinaryHandler.EdgeStorageSources edgeDrivers;

    @Before
    public void setMock() throws IOException {
        edgeDrivers = new EdgeDriverBinaryHandler.EdgeStorageSources("https://msedgedriver.azureedge.net/", httpClient);
    }

    @Test
    public void should_find_latest_stable_release_when_exists() throws Exception {
        // given
        when(httpClient.get(endsWith("/LATEST_STABLE"), anyString())).thenReturn(new HttpClient.Response(200,
                                                                                                         ALL_OS_RELEASE,
                                                                                                         emptyMap()));

        when(httpClient.get(endsWith("64.zip"))).thenReturn(new HttpClient.Response(200,
                                                                                    "edgedriver 64 bit version is available",
                                                                                    emptyMap()));

        // when
        final ExternalBinary latestRelease = edgeDrivers.getLatestRelease();

        // then
        assertThat(latestRelease.getVersion()).isEqualTo(ALL_OS_RELEASE);
    }

    @Test
    public void should_find_latest_release_for_given_os_when_latest_stable_is_not_released_for_it() throws Exception {
        // given
        // latest stable is not released for the OS (prepared test file does not have such a version at all)
        when(httpClient.get(endsWith("/LATEST_STABLE"), anyString())).thenReturn(new HttpClient.Response(200,
                                                                                                         NON_EXISTING_VERSION,
                                                                                                         emptyMap()));

        when(httpClient.get(contains(NON_EXISTING_VERSION + "/edgedriver_"))).thenReturn(new HttpClient.Response(404,
                                                                                                                 "this version is not available",
                                                                                                                 emptyMap()));

        // roll back to existing version (test file has it for all version)
        when(httpClient.get(contains("/LATEST_RELEASE_96_"), anyString())).thenReturn(new HttpClient.Response(200,
                                                                                                              ALL_OS_RELEASE,
                                                                                                              emptyMap()));

        when(httpClient.get(contains(ALL_OS_RELEASE + "/edgedriver_"))).thenReturn(new HttpClient.Response(200,
                                                                                                           "this version is available",
                                                                                                           emptyMap()));

        // when
        final ExternalBinary latestRelease = edgeDrivers.getLatestRelease();

        // then
        assertThat(latestRelease.getVersion()).isEqualTo(ALL_OS_RELEASE);
    }
}
