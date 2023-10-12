package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.UrlStorageSource;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.Capabilities;

import java.io.File;

/**
 * A class for handling chromeDriver binaries (Chrome for Testing)
 */
public class ChromeForTestingDriverBinaryHandler extends ChromeDriverBinaryHandler {
    public static final String DEFAULT_LATEST_SOURCE = "https://googlechromelabs.github.io/chrome-for-testing/";
    public static final String DEFAULT_SOURCE = "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/";

    public ChromeForTestingDriverBinaryHandler(Capabilities capabilities) {
        super(capabilities);
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new ChromeForTestingStorageSources(DEFAULT_SOURCE);
    }

    @Override
    protected File prepare(File downloaded) throws Exception {
        File extraction = BinaryFilesUtils.extract(downloaded);
        File[] dirs = extraction.listFiles(file -> file.isDirectory() && file.getName().startsWith("chromedriver"));
        final String binaryName = CHROME_DRIVER_BINARY_NAME.get();

        if (dirs == null || dirs.length != 1) {
            throw new IllegalStateException(
                "Missing ChromeDriver directory (chromedriver-*) containing executable (" + binaryName + ") in the directory " + extraction);
        }

        File[] files = dirs[0].listFiles(file -> file.isFile() && file.getName().equals(binaryName));

        if (files == null || files.length != 1) {
            throw new IllegalStateException(
                "Missing ChromeDriver executable (" + binaryName + ") in the directory " + dirs[0]);
        }

        return markAsExecutable(files[0]);
    }

    public static class ChromeForTestingStorageSources extends UrlStorageSource {

        public ChromeForTestingStorageSources(String baseUrl) {
            this(baseUrl, new HttpClient());
        }

        public ChromeForTestingStorageSources(String baseUrl, HttpClient client) {
            this(baseUrl, DEFAULT_LATEST_SOURCE + "LATEST_RELEASE_STABLE", client);
        }

        public ChromeForTestingStorageSources(String baseUrl, String urlToLatestRelease, HttpClient client) {
            super(baseUrl, urlToLatestRelease, client);
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            return getFileNameRegexToDownload(version, Architecture.AUTO_DETECT);
        }

        @Override
        public String getFileNameRegexToDownload(String version, Architecture architecture) {
            // Chrome for Testing URL might be
            // f.e. https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/117.0.5938.149/mac-arm64/chromedriver-mac-arm64.zip
            final StringBuilder fileName = new StringBuilder(version);
            fileName.append("/");

            final StringBuilder platformBuilder = new StringBuilder();
            if (PlatformUtils.isMac()) {
                platformBuilder.append("mac");
                if (PlatformUtils.isMacIntel()) {
                    platformBuilder.append("-x");
                } else {
                    platformBuilder.append("-arm");
                }
            } else if (PlatformUtils.isWindows()) {
                platformBuilder.append("win");
            } else if (PlatformUtils.isUnix()) {
                platformBuilder.append("linux");
            }
            platformBuilder.append(architecture.getValue());

            final String platform = platformBuilder.toString();
            fileName.append(platform);
            fileName.append("/");
            fileName.append("chromedriver-");
            fileName.append(platform);

            return fileName.append(".zip").toString();
        }
    }
}
