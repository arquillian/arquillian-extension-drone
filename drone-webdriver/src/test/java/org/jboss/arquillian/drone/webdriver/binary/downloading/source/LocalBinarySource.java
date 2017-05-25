package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class LocalBinarySource implements ExternalBinarySource {

    public static final String LATEST_VERSION = "latest";
    public static final String FIRST_VERSION = "1.0.0.Final";
    public static final String FILE_NAME = "echo-script";
    public static final String ECHO_LATEST_SCRIPT = "latest script";
    public static final String ECHO_FIRST_SCRIPT = "1.0.0.Final script";
    private static String LOCAL_SOURCE_DIRECTORY =
        "src/test/resources/files/downloading/LocalSource/".replace("/", File.separator);
    public static final File LATEST_FILE = new File(LOCAL_SOURCE_DIRECTORY + "latest-echo-script.zip");
    public static final File FIRST_FILE = new File(LOCAL_SOURCE_DIRECTORY + "1.0.0.Final-echo-script.zip");

    public static void assertThatCorrectFileWasDownloaded(boolean latest, File file) {
        assertThat(BinaryFilesUtils.getMd5hash(file))
            .as("MD5 hash should be same")
            .isEqualTo(BinaryFilesUtils.getMd5hash(latest ? LATEST_FILE : FIRST_FILE));
    }

    @Override
    public ExternalBinary getLatestRelease() throws Exception {
        return new ExternalBinary(LATEST_VERSION, LATEST_FILE.toURI().toString());
    }

    @Override
    public ExternalBinary getReleaseForVersion(String version) throws Exception {
        if (version.equals(FIRST_VERSION)) {
            return new ExternalBinary(FIRST_VERSION, FIRST_FILE.toURI().toString());
        } else {
            return getLatestRelease();
        }
    }

    @Override
    public String getFileNameRegexToDownload(String version) {
        if (version.equals(FIRST_VERSION)) {
            return FIRST_FILE.getName();
        } else {
            return LATEST_FILE.getName();
        }
    }
}
