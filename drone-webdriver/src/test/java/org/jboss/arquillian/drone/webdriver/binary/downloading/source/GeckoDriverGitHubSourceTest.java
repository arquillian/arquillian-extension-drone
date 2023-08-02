package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.Capabilities;

import java.nio.charset.StandardCharsets;

@RunWith(MockitoJUnitRunner.class)
public class GeckoDriverGitHubSourceTest {

    @Rule
    public final RestoreSystemProperties rsp = new RestoreSystemProperties();

    @Mock
    HttpClient httpClient;

    @Mock
    GitHubLastUpdateCache gitHubLastUpdateCache;

    @Mock
    Capabilities capabilities;

    @InjectMocks
    GeckoDriverGitHubSource geckoDriverGitHubSource;

    private static JsonObject releaseObject;
    private static final String VERSION_TAG = "v0.29.1";

    @BeforeClass
    public static void init() throws Exception {
        String releaseJson = IOUtils.resourceToString(
            String.format("files/downloading/github/geckodriver/geckodriver_release_%s.json", VERSION_TAG),
            StandardCharsets.UTF_8,
            GeckoDriverGitHubSourceTest.class.getClassLoader()
        );
        releaseObject = new Gson().fromJson(releaseJson, JsonObject.class);
    }

    @Test
    public void testDownloadFileForLinux32() throws Exception {
        probeOsArchCombination("Linux", "i386", "geckodriver-%s-linux32.tar.gz");
    }

    @Test
    public void testDownloadFileForLinux64() throws Exception {
        probeOsArchCombination("Linux", "amd64", "geckodriver-%s-linux64.tar.gz");
    }

    @Test
    public void testDownloadFileForWindows32() throws Exception {
        probeOsArchCombination("Windows XP", "x86", "geckodriver-%s-win32.zip");
    }

    @Test
    public void testDownloadFileForWindows64() throws Exception {
        probeOsArchCombination("Windows 10", "amd64", "geckodriver-%s-win64.zip");
    }

    @Test
    public void testDownloadFileForMacIntel() throws Exception {
        probeOsArchCombination("Mac OS X", "x86_64", "geckodriver-%s-macos.tar.gz");
    }

    @Test
    public void testDownloadFileForMacAppleSilicon() throws Exception {
        probeOsArchCombination("Mac OS X", "aarch64", "geckodriver-%s-macos-aarch64.tar.gz");
    }

    @Test
    public void testUnsupportedArchitecture() {
        Exception e = Assert.assertThrows(
            RuntimeException.class,
            () -> probeOsArchCombination("TR-DOS", "Z80", null)
        );
        Assert.assertTrue(e.getMessage().contains("binary download is not available"));
    }

    private void probeOsArchCombination(String osName, String osArch, String expectedFileToDownload) throws Exception {
        System.setProperty("os.name", osName);
        System.setProperty("os.arch", osArch);
        String downloadUrl = geckoDriverGitHubSource.findReleaseBinaryUrl(releaseObject, VERSION_TAG);
        String fileToDownload = FilenameUtils.getName(downloadUrl);
        Assert.assertEquals(String.format(expectedFileToDownload, VERSION_TAG), fileToDownload);
    }

}
