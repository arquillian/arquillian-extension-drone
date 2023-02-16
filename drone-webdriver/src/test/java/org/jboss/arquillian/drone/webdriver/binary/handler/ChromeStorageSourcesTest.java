package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.MissingBinaryException;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import static org.jboss.arquillian.drone.webdriver.binary.handler.ChromeDriverBinaryHandler.ChromeStorageSources;

public class ChromeStorageSourcesTest {
    private final ChromeStorageSources sources = new ChromeStorageSources("https:/chrome.example.com/");

    private static final String REAL_OS = System.getProperty("os.name");
    private static final String REAL_ARCH = System.getProperty("os.arch");

    private static final String CHROME_VERSION = "111.0.5563.19";

    @After
    public void resetOsProperties() {
        overrideSystemProperties(REAL_OS, REAL_ARCH);
    }

    @Test
    public void testWindowsDownload() {
        overrideSystemProperties("win", "x86");

        String filename = sources.getFileNameRegexToDownload(CHROME_VERSION, Architecture.BIT32);
        Assert.assertEquals("111.0.5563.19/chromedriver_win32.zip", filename);
    }

    @Test
    public void testLinux64Download() {
        overrideSystemProperties("linux", "x86_64");

        String filename = sources.getFileNameRegexToDownload(CHROME_VERSION, Architecture.BIT64);
        Assert.assertEquals("111.0.5563.19/chromedriver_linux64.zip", filename);
    }

    @Test
    public void testLinux32Download() {
        overrideSystemProperties("linux", "x86");

        String filename = sources.getFileNameRegexToDownload("2.3", Architecture.BIT32);
        Assert.assertEquals("2.3/chromedriver_linux32.zip", filename);
    }

    @Test
    public void testLinux32DownloadInvalidVersion() {
        overrideSystemProperties("linux", "x86");

        MissingBinaryException exception = Assert.assertThrows(MissingBinaryException.class, () ->
            sources.getFileNameRegexToDownload(CHROME_VERSION, Architecture.BIT32));

        Assert.assertTrue("Error message", exception.getMessage().startsWith("32bit Linux is not supported after"));
    }

    @Test
    public void testMacIntel64Download() {
        overrideSystemProperties("Mac OS X", "x86_64");

        String filename = sources.getFileNameRegexToDownload(CHROME_VERSION, Architecture.BIT64);
        Assert.assertEquals("111.0.5563.19/chromedriver_mac64.zip", filename);
    }

    @Test
    public void testMacIntel64DownloadInvalidVersion() {
        overrideSystemProperties("Mac OS X", "x86_64");

        MissingBinaryException exception = Assert.assertThrows(MissingBinaryException.class, () ->
            sources.getFileNameRegexToDownload("2.22", Architecture.BIT64));

        Assert.assertTrue("Error message", exception.getMessage().startsWith("64bit macOS is not supported before"));
    }

    @Test
    public void testMacIntel32Download() {
        overrideSystemProperties("Mac OS X", "x86");

        String filename = sources.getFileNameRegexToDownload("2.17", Architecture.BIT32);
        Assert.assertEquals("2.17/chromedriver_mac32.zip", filename);
    }

    @Test
    public void testMacIntel32DownloadInvalidVersion() {
        overrideSystemProperties("Mac OS X", "x86");

        MissingBinaryException exception = Assert.assertThrows(MissingBinaryException.class, () ->
            sources.getFileNameRegexToDownload(CHROME_VERSION, Architecture.BIT32));

        Assert.assertTrue("Error message", exception.getMessage().startsWith("32bit macOS is not supported after"));
    }

    @Test
    public void testMacAppleSiliconDownload() {
        overrideSystemProperties("Mac OS X", "aarch64");

        String filename = sources.getFileNameRegexToDownload(CHROME_VERSION, Architecture.BIT64);
        Assert.assertEquals("111.0.5563.19/chromedriver_mac_arm64.zip", filename);
    }

    @Test
    public void testMacAppleSiliconM1VersionDownload() {
        overrideSystemProperties("Mac OS X", "aarch64");

        String filename = sources.getFileNameRegexToDownload("106.0.5249.0", Architecture.BIT64);
        Assert.assertEquals("106.0.5249.0/chromedriver_mac64_m1.zip", filename);
    }

    @Test
    public void testMacAppleSiliconIntelFallbackDownload() {
        overrideSystemProperties("Mac OS X", "aarch64");

        String filename = sources.getFileNameRegexToDownload("87.0.4280.20", Architecture.BIT64);
        Assert.assertEquals("87.0.4280.20/chromedriver_mac64.zip", filename);
    }

    private void overrideSystemProperties(String os, String arch) {
        System.setProperty("os.name", os);
        System.setProperty("os.arch", arch);
    }
}
