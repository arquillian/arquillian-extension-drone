package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.drone.webdriver.binary.handler.ChromeForTestingDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.arquillian.drone.webdriver.binary.handler.ChromeForTestingDriverBinaryHandler.DEFAULT_SOURCE;

public class ChromeForTestingStorageSourceTest {
    private static final String TEST_VERSION = "117.0.5938.149";

    @Test
    public void linux64() throws IOException {
        try (Closeable ignore = new OsUpdater("linux", "64")) {
            ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources source = new ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources(DEFAULT_SOURCE);
            final String finalUrl = source.getFileNameRegexToDownload(TEST_VERSION);

            assertThat(PlatformUtils.isLinux(), CoreMatchers.is(true));
            assertThat(PlatformUtils.is64(), CoreMatchers.is(true));

            assertThat(finalUrl, CoreMatchers.is(TEST_VERSION + "/linux64/chromedriver-linux64.zip"));
        }
    }

    @Test
    public void macArm64() throws IOException {
        try (Closeable ignore = new OsUpdater("mac", "aarch64")) {
            ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources source = new ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources(DEFAULT_SOURCE);
            final String finalUrl = source.getFileNameRegexToDownload(TEST_VERSION);

            assertThat(PlatformUtils.isMacAppleSilicon(), CoreMatchers.is(true));
            assertThat(PlatformUtils.is64(), CoreMatchers.is(true));

            assertThat(finalUrl, CoreMatchers.is(TEST_VERSION + "/mac-arm64/chromedriver-mac-arm64.zip"));
        }
    }

    @Test
    public void macIntel64() throws IOException {
        try (Closeable ignore = new OsUpdater("mac", "x86_64")) {
            ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources source = new ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources(DEFAULT_SOURCE);
            final String finalUrl = source.getFileNameRegexToDownload(TEST_VERSION);

            assertThat(PlatformUtils.isMacIntel(), CoreMatchers.is(true));
            assertThat(PlatformUtils.is64(), CoreMatchers.is(true));

            assertThat(finalUrl, CoreMatchers.is(TEST_VERSION + "/mac-x64/chromedriver-mac-x64.zip"));
        }
    }

    @Test
    public void win32() throws IOException {
        try (Closeable ignore = new OsUpdater("win", "32")) {
            ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources source = new ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources(DEFAULT_SOURCE);

            final String finalUrl = source.getFileNameRegexToDownload(TEST_VERSION, Architecture.BIT32);

            assertThat(PlatformUtils.isWindows(), CoreMatchers.is(true));
            assertThat(PlatformUtils.is32(), CoreMatchers.is(true));

            assertThat(finalUrl, CoreMatchers.is(TEST_VERSION + "/win32/chromedriver-win32.zip"));
        }
    }

    @Test
    public void win64() throws IOException {
        try (Closeable ignore = new OsUpdater("win", "64")) {
            ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources source = new ChromeForTestingDriverBinaryHandler.ChromeForTestingStorageSources(DEFAULT_SOURCE);
            final String finalUrl = source.getFileNameRegexToDownload(TEST_VERSION);

            assertThat(PlatformUtils.isWindows(), CoreMatchers.is(true));
            assertThat(PlatformUtils.is64(), CoreMatchers.is(true));

            assertThat(finalUrl, CoreMatchers.is(TEST_VERSION + "/win64/chromedriver-win64.zip"));
        }
    }


    private static class OsUpdater implements Closeable {
        private final String oldOs;
        private final String oldArch;

        public OsUpdater(String os, String arch) {
            this.oldOs = System.getProperty("os.name", "").toLowerCase();
            this.oldArch = System.getProperty("os.arch", "").toLowerCase();

            System.setProperty("os.name", os);
            System.setProperty("os.arch", arch);
        }

        @Override
        public void close() {
            System.setProperty("os.name", oldOs);
            System.setProperty("os.arch", oldArch);
        }
    }

}
