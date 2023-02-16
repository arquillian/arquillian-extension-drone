package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.MissingBinaryException;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.UrlStorageSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * A class for handling chromeDriver binaries
 */
public class ChromeDriverBinaryHandler extends AbstractBinaryHandler {

    private static final Logger log = Logger.getLogger(ChromeDriverBinaryHandler.class.getName());

    public static final String CHROME_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.chrome.driver";
    public static final String CHROME_DRIVER_BINARY_PROPERTY = "chromeDriverBinary";
    private static final String CHROME_DRIVER_VERSION_PROPERTY = "chromeDriverVersion";
    private static final String CHROME_DRIVER_URL_PROPERTY = "chromeDriverUrl";

    public static final String CHROME_DRIVER_BINARY_NAME = "chromedriver" + (PlatformUtils.isWindows() ? ".exe" : "");

    private final DesiredCapabilities capabilities;

    public ChromeDriverBinaryHandler(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.Chrome().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return CHROME_DRIVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return CHROME_DRIVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new ChromeStorageSources("https://chromedriver.storage.googleapis.com/");
    }

    @Override
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return CHROME_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return CHROME_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    @Override
    protected File prepare(File downloaded) throws Exception {
        File extraction = BinaryFilesUtils.extract(downloaded);
        File[] files = extraction.listFiles(file -> file.isFile() && file.getName().equals(CHROME_DRIVER_BINARY_NAME));

        if (files == null || files.length != 1) {
            throw new IllegalStateException(
                "Missing ChromeDriver executable (" + CHROME_DRIVER_BINARY_NAME + ") in the directory " + extraction);
        }

        return markAsExecutable(files[0]);
    }

    public static class ChromeStorageSources extends UrlStorageSource {

        public ChromeStorageSources(String baseUrl) {
            this(baseUrl, new HttpClient());
        }

        public ChromeStorageSources(String baseUrl, HttpClient client) {
            super(baseUrl, baseUrl + "LATEST_RELEASE", client);
        }

        @Override
        public ExternalBinary getReleaseForVersion(String requiredVersion, Architecture architecture) throws Exception {
            ExternalBinary release;
            try {
                release = super.getReleaseForVersion(requiredVersion, architecture);
            } catch (MissingBinaryException mbe) {
                if (PlatformUtils.isWindows() && Objects.equals(architecture.getValue(), Architecture.BIT64.getValue())) {
                    log.log(Level.WARNING, "Failed downloading 64-bit version of Chrome Driver. Reason: ", mbe);
                    log.log(Level.WARNING, "This special case was reported as https://github.com/arquillian/arquillian-extension-drone/issues/300");
                    log.log(Level.WARNING, "Downloading 32-bit version of Chrome Driver instead. ({0})", requiredVersion);

                    release = getReleaseForVersion(requiredVersion, Architecture.BIT32);
                } else {
                    throw mbe;
                }
            }
            return release;
        }

        @Override
        protected ExternalBinary getLatestRelease(String charset) throws Exception {
            ExternalBinary latestRelease;
            try {
                latestRelease = super.getLatestRelease(charset);
            } catch (MissingBinaryException e) {
                if (PlatformUtils.isWindows()) {
                    final String latestVersion = getVersion(urlToLatestRelease, charset);
                    log.log(Level.WARNING, "Failed downloading 64-bit version of Chrome Driver. Reason: ", e);
                    log.log(Level.WARNING, "Downloading 32-bit version of Chrome Driver instead. ({0})", latestVersion);

                    latestRelease = getReleaseForVersion(latestVersion, Architecture.BIT32);
                } else {
                    throw new MissingBinaryException(e.getMessage());
                }
            }
            return latestRelease;
        }

        @Override
        public String getFileNameRegexToDownload(String version) {
            return getFileNameRegexToDownload(version, Architecture.AUTO_DETECT);
        }

        private static final ChromeVersion LAST_LINUX_32BIT = new ChromeVersion(2, 33, 0, 0);
        private static final ChromeVersion LAST_MAC_32BIT = new ChromeVersion(2, 22, 0, 0);
        private static final ChromeVersion FIRST_MAC_64BIT = new ChromeVersion(2, 23, 0, 0);
        private static final ChromeVersion FIRST_MAC_M1 = new ChromeVersion(88, 0, 0, 0);
        private static final ChromeVersion LAST_MAC_M1 = new ChromeVersion(106, 0, 5249, 0);

        @Override
        public String getFileNameRegexToDownload(String version, Architecture architecture) {
            // known formats:
            // chromedriver_linux32.zip    v <= 2.33
            // chromedriver_linux64.zip
            // chromedriver_win32.zip
            // chromedriver_mac32.zip      v <= 2.22
            // chromedriver_mac64.zip      v >= 2.23
            // chromedriver_mac64_m1.zip   88.0 <= v >= 106.0.5249
            // chromedriver_mac_arm64.zip  v > 106.0.5249

            ChromeVersion parsedVersion = new ChromeVersion(version);

            final StringBuilder fileName = new StringBuilder(version);
            fileName.append("/");
            fileName.append("chromedriver_");

            if (PlatformUtils.isMac()) {
                fileName.append("mac");
                fileName.append(getMacSuffix(parsedVersion, architecture));
            } else if (PlatformUtils.isWindows()) {
                // windows is currently only 32 bit
                fileName.append("win32");
            } else if (PlatformUtils.isUnix()) {
                if (architecture == Architecture.BIT32 && parsedVersion.isAfter(LAST_LINUX_32BIT)) {
                    throw new MissingBinaryException("32bit Linux is not supported after version " + LAST_LINUX_32BIT);
                }

                fileName.append("linux");
                fileName.append(architecture.getValue());
            } else {
                throw new MissingBinaryException("Unsupported OS: " + PlatformUtils.getOS());
            }

            return fileName.append(".zip").toString();
        }

        private String getMacSuffix(ChromeVersion version, Architecture architecture) {
            if (architecture == Architecture.BIT32 && version.isAfter(LAST_MAC_32BIT)) {
                throw new MissingBinaryException("32bit macOS is not supported after version " + LAST_MAC_32BIT);
            }

            if (architecture == Architecture.BIT64 && version.isBefore(FIRST_MAC_64BIT)) {
                throw new MissingBinaryException("64bit macOS is not supported before version " + FIRST_MAC_64BIT);
            }

            if (PlatformUtils.isMacAppleSilicon()) {
                if (version.isBefore(FIRST_MAC_M1)) {
                    // before ARM builds for macOS - fallback to Intel: chromedriver_mac64.zip
                    return "64";
                }

                if (version.isAfter(LAST_MAC_M1)) {
                    // after switch to arm64: chromedriver_mac_arm64.zip
                    return "_arm64";
                }

                // before switch to arm64 but after initial ARM builds: chromedriver_mac64_m1.zip
                return "64_m1";
            } else {
                // Intel: chromedriver_mac64.zip or chromedriver_mac32.zip
                return architecture.getValue();
            }
        }
    }

    static class ChromeVersion {
        final int major;
        final int minor;
        final int patch;
        final int build;

        ChromeVersion(int major, int minor, int patch, int build) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.build = build;
        }

        ChromeVersion(String raw) {
            String[] versionParts = raw.split("\\.");

            if (versionParts.length < 2) {
                throw new IllegalArgumentException("Invalid Chrome version");
            }

            major = Integer.parseInt(versionParts[0]);
            minor = Integer.parseInt(versionParts[1]);

            if (versionParts.length >= 4) {
                patch = Integer.parseInt(versionParts[2]);
                build = Integer.parseInt(versionParts[3]);
            } else {
                patch = 0;
                build = 0;
            }
        }

        boolean isAfter(ChromeVersion version) {
            if (major > version.major) {
                return true;
            }

            if (major < version.major) {
                return false;
            }

            if (minor > version.minor) {
                return true;
            }

            if (minor < version.minor) {
                return false;
            }

            if (patch > version.patch) {
                return true;
            }

            if (patch < version.patch) {
                return false;
            }

            return build > version.build;
        }

        boolean isBefore(ChromeVersion version) {
            if (major > version.major) {
                return false;
            }

            if (major < version.major) {
                return true;
            }

            if (minor > version.minor) {
                return false;
            }

            if (minor < version.minor) {
                return true;
            }

            if (patch > version.patch) {
                return false;
            }

            if (patch < version.patch) {
                return true;
            }

            return build < version.build;
        }


        @Override
        public String toString() {
            return major + "." + minor + "." + patch + "." + build;
        }
    }
}
