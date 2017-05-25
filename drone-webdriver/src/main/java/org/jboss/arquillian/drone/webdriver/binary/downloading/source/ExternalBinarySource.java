package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;

/**
 * A representation of an external source of some binarie.
 */
public interface ExternalBinarySource {

    /**
     * Retrieves information about the latest release of binary and returns an instance of {@link ExternalBinary} that
     * represents the latest release.
     *
     * @return An instance of {@link ExternalBinary} that contains information about the latest release of the binary.
     *
     * @throws Exception
     *     If anything bad happens
     */
    ExternalBinary getLatestRelease() throws Exception;

    /**
     * Retrieves information about a binary release with the desired version and returns an instance of
     * {@link ExternalBinary} that represents the found release.
     *
     * @param version
     *     A version of a binary release that should be retrieved.
     *
     * @return An instance of {@link ExternalBinary} that contains information about a binary release with the desired
     * version.
     *
     * @throws Exception
     */
    ExternalBinary getReleaseForVersion(String version) throws Exception;

    /**
     * This method should returns a regex that represents a file name of the release asset that should be downloaded.
     * If this method returns null, the AbstractBinaryHandler won't be looking for any file in the cache directory
     * and will jump to downloading phase.
     *
     * @return A regex that represents a file name of an asset to be downloaded.
     */
    String getFileNameRegexToDownload(String version);
}
