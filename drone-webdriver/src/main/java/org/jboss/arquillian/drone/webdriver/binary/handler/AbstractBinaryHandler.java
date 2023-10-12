package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.Downloader;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.utils.Constants;
import org.jboss.arquillian.drone.webdriver.utils.PropertySecurityAction;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Class that handles system properties, properties stored in capabilities, downloading, extracting and setting binaries
 * as executable.
 */
public abstract class AbstractBinaryHandler implements BinaryHandler {

    /**
     * Capability property that sets downloading on or off. By default it is on
     */
    public static final String DOWNLOAD_BINARIES_PROPERTY = "downloadBinaries";
    private static final Logger log = Logger.getLogger(AbstractBinaryHandler.class.toString());

    /**
     * Checks system properties and capabilities, whether a path to binary is already set there
     * (see {@link AbstractBinaryHandler#getSystemBinaryProperty()} and {@link AbstractBinaryHandler#getBinaryProperty()}
     * ).
     * If not and the downloading is not set off ({@link AbstractBinaryHandler#DOWNLOAD_BINARIES_PROPERTY}), then the
     * binary is downloaded. Resulting binary is then set as system property that is returned by the method
     * {@link AbstractBinaryHandler#getSystemBinaryProperty()}
     *
     * @param performExecutableValidations
     *     If it should be checked whether the binary points to an executable file.
     *
     * @return Path to the binary
     */
    @Override
    public String checkAndSetBinary(boolean performExecutableValidations) {
        String binary = PropertySecurityAction.getProperty(getSystemBinaryProperty());

        if (Validate.empty(binary)) {
            binary = PropertySecurityAction.getProperty(getBinaryProperty());
        }

        if (Validate.empty(binary) && !Validate.empty(getBinaryProperty())) {
            binary = (String) getCapabilities().getCapability(getBinaryProperty());
        }

        if (Validate.empty(binary)) {
            String downloadBinaries = (String) getCapabilities().getCapability(DOWNLOAD_BINARIES_PROPERTY);
            if (Validate.empty(downloadBinaries)
                || (!downloadBinaries.toLowerCase().trim().equals("false")
                && !downloadBinaries.toLowerCase().trim().equals("no"))) {

                try {
                    binary = downloadAndPrepare().toString();
                } catch (Exception e) {
                    throw new IllegalStateException(
                        "Something bad happened when Drone was trying to download and prepare a binary. "
                            + "For more information see the cause.", e);
                }
            }
        }
        setBinaryAsSystemProperty(performExecutableValidations, binary);
        return binary;
    }

    /**
     * Sets binary as a system property that is returned by method
     * {@link AbstractBinaryHandler#getSystemBinaryProperty()}
     *
     * @param performExecutableValidations
     *     If it should be checked whether the binary points to an executable file.
     * @param binary
     *     Path to the binary
     */
    protected void setBinaryAsSystemProperty(boolean performExecutableValidations, String binary) {
        if (Validate.nonEmpty(binary) && Validate.nonEmpty(getSystemBinaryProperty())) {
            if (performExecutableValidations) {
                Validate.isExecutable(binary,
                    "The binary must point to an executable file, " + binary);
            }
            PropertySecurityAction.setProperty(getSystemBinaryProperty(), binary);
        }
    }

    /**
     * This method consist of four steps:
     * <br/>
     * <h3>1. Checking properties</h3>
     * In the first step it checks capabilities if there is set either url a binary should be downloaded from or
     * a desired binary version. For more information see methods {@link AbstractBinaryHandler#getUrlToDownloadProperty()}
     * and
     * {@link AbstractBinaryHandler#getDesiredVersionProperty()}.
     * <p>
     * <h3>2. Downloading</h3>
     * If the url is set then the binary is downloaded from the given url.
     * <p>
     * If there is set only the desired version, then a binary with the specified version is downloaded using an external
     * binary source ({@link AbstractBinaryHandler#getExternalBinarySource()})
     * </p>
     * <p>
     * If there is set neither a url nor a desired version, then a binary with the latest version is downloaded using
     * the external binary source.
     * </p>
     * <p>
     * <p>
     * Directory where the downloaded file is stored depends on set properties. If there is set the desired version,
     * or if the latest version is downloaded then the file is stored in:
     * <br/>
     * <code>$HOME/.arquillian/drone/ + </code>{@link AbstractBinaryHandler#getArquillianCacheSubdirectory()}<code>
     * + / + version
     * </code>
     * <br/>
     * If the version is not set, then the file is stored in: <code>target/drone/downloaded</code>
     * </p>
     * <p>
     * <h3>3. Extraction/copy</h3>
     * When the file is downloaded then it is expected that in most cases it is an archive (zip, tar) file. If the file
     * is
     * an archive then it is extracted; if the file is not an archive file, then it is copied. The targeted directory
     * for this operation is:
     * <br/>
     * <code>target/drone/md5hash(downloaded_file)/</code>
     * <p>
     * <h3>4. Setting as executable</h3>
     * In the last step the extracted/copied file is set as an executalbe file.
     *
     * @return An executable binary that was extracted/copied from the downloaded file
     *
     * @throws Exception
     *     If anything bad happens
     */
    public File downloadAndPrepare() throws Exception {
        String url = null;
        if (!Validate.empty(getUrlToDownloadProperty())) {
            url = (String) getCapabilities().getCapability(getUrlToDownloadProperty());
        }

        String desiredVersion = null;
        if (!Validate.empty(getDesiredVersionProperty())) {
            desiredVersion = (String) getCapabilities().getCapability(getDesiredVersionProperty());
            if (StringUtils.isBlank(desiredVersion)) {
                desiredVersion = System.getProperty(getDesiredVersionProperty());
            }
        }

        if (Validate.nonEmpty(url)) {
            if (Validate.empty(desiredVersion)) {
                return downloadAndPrepare(null, url);
            } else {
                return downloadAndPrepare(createAndGetCacheDirectory(desiredVersion), url);
            }
        }

        if (getExternalBinarySource() == null) {
            return null;
        }
        ExternalBinary release = null;
        if (Validate.nonEmpty(desiredVersion)) {
            File versionDirectory = createAndGetCacheDirectory(desiredVersion);

            File alreadyDownloaded = checkAndGetIfDownloaded(desiredVersion, versionDirectory);
            if (alreadyDownloaded != null) {
                return prepare(alreadyDownloaded);

            } else {
                release = getExternalBinarySource().getReleaseForVersion(desiredVersion);
                return downloadAndPrepare(versionDirectory, release.getUrl());
            }

        } else {
            release = getExternalBinarySource().getLatestRelease();
            return downloadAndPrepare(createAndGetCacheDirectory(release.getVersion()), release.getUrl());
        }
    }

    private File checkAndGetIfDownloaded(String desiredVersion, File versionDirectory) {
        String fileNameRegexToDownload = getExternalBinarySource().getFileNameRegexToDownload(desiredVersion);
        if (fileNameRegexToDownload == null) {
            return null;
        }

        File[] files = versionDirectory.listFiles(
            file -> file.isFile() && file.getName().matches(fileNameRegexToDownload));

        if (files != null && files.length == 1) {
            return files[0];
        }
        return null;
    }

    /**
     * Takes care of all steps but the first one of the method {@link AbstractBinaryHandler#downloadAndPrepare()}
     *
     * @param targetDir
     *     A directory where a downloaded binary should be stored
     * @param from
     *     A url a binary should be downloaded from
     *
     * @return An executable binary that was extracted/copied from the downloaded file
     *
     * @throws Exception
     *     If anything bad happens
     */
    protected File downloadAndPrepare(File targetDir, String from) throws Exception {
        return downloadAndPrepare(targetDir, new URL(from));
    }

    /**
     * Takes care of all steps but the first one of the method {@link AbstractBinaryHandler#downloadAndPrepare()}
     *
     * @param targetDir
     *     A directory where a downloaded binary should be stored
     * @param from
     *     A url a binary should be downloaded from
     *
     * @return An executable binary that was extracted/copied from the downloaded file
     *
     * @throws Exception
     *     If anything bad happens
     */
    protected File downloadAndPrepare(File targetDir, URL from) throws Exception {
        File downloaded = Downloader.download(targetDir, from);
        return prepare(downloaded);
    }

    /**
     * Takes care of the preparation - extraction/move of downloaded file & marking as executable
     *
     * @param downloaded The downloaded file to prepare
     * @return An executable binary that was extracted/copied from the downloaded file
     * @throws Exception If anything bad happens
     */
    protected File prepare(File downloaded) throws Exception {
        if (!downloaded.exists()) {
            throw new IllegalStateException("Downloaded file '" + downloaded.getPath() + "' doesn't exist, the download probably failed");
        }
        File extraction = BinaryFilesUtils.extract(downloaded);
        File[] files = extraction.listFiles(file -> file.isFile());
        if (files == null || files.length == 0) {
            throw new IllegalStateException(
                "The number of extracted files in the directory " + extraction + " is 0. There is no file to use");
        }
        return markAsExecutable(files[0]);
    }

    /**
     * Sets the given binary to be executable (if it is not already set)
     *
     * @param binaryFile
     *     A binary file that should be set to be executable
     *
     * @return the given binary file set to be executable
     */
    protected File markAsExecutable(File binaryFile) {
        synchronized (log) {
            if (!Validate.executable(binaryFile.getAbsolutePath())) {
                log.info("marking binary file: " + binaryFile.getPath() + " as executable");
                try {
                    binaryFile.setExecutable(true);
                } catch (SecurityException se) {
                    log.severe("The downloaded binary: " + binaryFile
                        + " could not be set as executable. This may cause additional problems.");
                }
            }
        }
        return binaryFile;
    }

    protected File createAndGetCacheDirectory(String subdirectory) {
        Path dirPath = Constants.ARQUILLIAN_DRONE_CACHE_DIRECTORY.resolve(getArquillianCacheSubdirectory())
            .resolve(subdirectory == null ? "" : subdirectory);
        File dir = dirPath.toFile();
        dir.mkdirs();
        return dir;
    }

    /**
     * This method should return a capability property name which a path to an executable binary could be stored under
     *
     * @return A capability property name which a path to an executable binary could be stored under
     */
    protected abstract String getBinaryProperty();

    /**
     * This method should return a system property name which a path to an executable binary should be stored under
     *
     * @return A system property name which a path to an executable binary should be stored under
     */
    public abstract String getSystemBinaryProperty();

    /**
     * Name of the subdirectory that should be used for this binary handler in the Drone cache directory
     * <code>($HOME/.arquillian/drone)</code>
     *
     * @return Name of the subdirectory that should be used for this binary handler in the Drone cache directory
     */
    protected abstract String getArquillianCacheSubdirectory();

    /**
     * This method should return a capability property name which a desired version of a binary could be stored under
     *
     * @return A capability property name under a desired version of a binary could be stored under
     */
    protected abstract String getDesiredVersionProperty();

    /**
     * This method should return a capability property name which a url pointing to a desired binary could be stored under
     *
     * @return A capability property name which a url pointing to a desired binary could be stored under
     */
    protected abstract String getUrlToDownloadProperty();

    /**
     * This method should return an instance of an {@link ExternalBinary} that should be used for retrieving available
     * releases of a binary
     *
     * @return An instance of an {@link ExternalBinary} that should be used for retrieving available releases of a binary
     */
    protected abstract ExternalBinarySource getExternalBinarySource();

    /**
     * This method should return a desired capabilities with stored properties
     *
     * @return A desired capabilities with stored properties
     */
    protected abstract Capabilities getCapabilities();
}
