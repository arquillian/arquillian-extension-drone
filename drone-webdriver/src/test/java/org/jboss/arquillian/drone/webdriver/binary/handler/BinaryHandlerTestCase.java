package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.task.os.CommandTool;
import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.Downloader;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.LocalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.process.BinaryInteraction;
import org.jboss.arquillian.drone.webdriver.utils.Constants;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.jboss.arquillian.phantom.resolver.maven.PlatformUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.ARQUILLIAN_DRONE_CACHE_DIRECTORY;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.DRONE_TARGET_DIRECTORY;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BinaryHandlerTestCase {

    private static String TEST_DRONE_TARGET_DIRECTORY = "target" + File.separator + "drone-test" + File.separator;
    private static String TEST_DRONE_CACHE_DIRECTORY = TEST_DRONE_TARGET_DIRECTORY + "cache" + File.separator;

    private static final String originalCacheDirectory = ARQUILLIAN_DRONE_CACHE_DIRECTORY;
    private static final String originalTargetDirectory = DRONE_TARGET_DIRECTORY;

    @BeforeClass
    public static void setTestCacheDirectory() throws NoSuchFieldException, IllegalAccessException {
        setTargetDirectory(TEST_DRONE_TARGET_DIRECTORY);
        setCacheDirectory(TEST_DRONE_CACHE_DIRECTORY);
    }

    @Before
    public void cleanupBefore() throws IOException {
        cleanUp();
    }

    @After
    public void cleanupAfter() throws IOException {
        cleanUp();
    }

    private void cleanUp() throws IOException {
        File targetDroneDir = new File(TEST_DRONE_TARGET_DIRECTORY);
        if (targetDroneDir.exists()) {
            FileUtils.deleteDirectory(targetDroneDir);
        }
        System.setProperty(LocalBinaryHandler.LOCAL_SOURCE_SYSTEM_BINARY_PROPERTY, "");
        System.setProperty(LocalBinaryHandler.LOCAL_SOURCE_BINARY_PROPERTY, "");
    }

    @AfterClass
    public static void setOriginalCacheDirectory() throws NoSuchFieldException, IllegalAccessException {
        setTargetDirectory(originalTargetDirectory);
        setCacheDirectory(originalCacheDirectory);
    }

    private static void setCacheDirectory(String dirToSet) throws NoSuchFieldException, IllegalAccessException {
        setConstantProperty("ARQUILLIAN_DRONE_CACHE_DIRECTORY", dirToSet);
    }

    private static void setTargetDirectory(String dirToSet) throws NoSuchFieldException, IllegalAccessException {
        setConstantProperty("DRONE_TARGET_DIRECTORY", dirToSet);
    }

    private static void setConstantProperty(String propertyVariable, String value)
        throws NoSuchFieldException, IllegalAccessException {
        Field constantField = Constants.class.getField(propertyVariable);
        constantField.setAccessible(true);

        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(constantField, constantField.getModifiers() & ~Modifier.FINAL);

        constantField.set(null, value);
    }

    @Test
    public void verifyWithoutAnyCapabilitySet() throws Exception {

        // the latest release should be downloaded and prepared
        verifyIsDownloadedExtractedSetExecutableSetInSystemProperty(
            new DesiredCapabilities(),
            getDownloadedPath(LocalBinarySource.LATEST_VERSION, LocalBinarySource.LATEST_FILE.getName()),
            getExtractedPath(LocalBinarySource.LATEST_FILE),
            LocalBinarySource.ECHO_LATEST_SCRIPT,
            true);
    }

    @Test
    public void verifyWithVersionCapabilitySet() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        // set version property to 1.0.0.Final
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_VERSION_PROPERTY,
            LocalBinarySource.FIRST_VERSION);

        // the 1.0.0.Final release should be downloaded and prepared
        verifyIsDownloadedExtractedSetExecutableSetInSystemProperty(
            capabilities,
            getDownloadedPath(LocalBinarySource.FIRST_VERSION, LocalBinarySource.FIRST_FILE.getName()),
            getExtractedPath(LocalBinarySource.FIRST_FILE),
            LocalBinarySource.ECHO_FIRST_SCRIPT,
            false);
    }

    @Test
    public void verifyWithUrlCapabilitySet() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        // set url the file should be downloaded from - without specifying version
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_URL_PROPERTY,
            LocalBinarySource.FIRST_FILE.toURI().toString());

        // the 1.0.0.Final release should be downloaded to target/drone/downloaded directory
        verifyIsDownloadedExtractedSetExecutableSetInSystemProperty(
            capabilities,
            Downloader.DRONE_TARGET_DOWNLOADED_DIRECTORY + LocalBinarySource.FIRST_FILE.getName(),
            getExtractedPath(LocalBinarySource.FIRST_FILE),
            LocalBinarySource.ECHO_FIRST_SCRIPT,
            false);
    }

    @Test
    public void verifyWithUrlAndVersionCapabilitySet() throws Exception {
        String myCoolVersion = "my-cool-version";
        DesiredCapabilities capabilities = new DesiredCapabilities();
        // set url the file should be downloaded from and also my own version
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_VERSION_PROPERTY,
            myCoolVersion);
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_URL_PROPERTY,
            LocalBinarySource.FIRST_FILE.toURI().toString());

        // the 1.0.0.Final release should be downloaded to target/drone/test/my-cool-version
        verifyIsDownloadedExtractedSetExecutableSetInSystemProperty(
            capabilities,
            getDownloadedPath(myCoolVersion, LocalBinarySource.FIRST_FILE.getName()),
            getExtractedPath(LocalBinarySource.FIRST_FILE),
            LocalBinarySource.ECHO_FIRST_SCRIPT,
            false);
    }

    @Test
    public void verifyWithDownloadBinariesSetFalse() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        // set download feature to off
        capabilities.setCapability(
            AbstractBinaryHandler.DOWNLOAD_BINARIES_PROPERTY,
            "false");

        // nothing should be downloaded
        LocalBinaryHandler dummyGitHubHandler = new LocalBinaryHandler(capabilities);
        String resultingFile = dummyGitHubHandler.checkAndSetBinary(true);
        assertThat(resultingFile).isNull();
    }

    @Test
    public void verifyWithSystemPropertySetToLatest() throws Exception {
        String latest = new LocalBinaryHandler(new DesiredCapabilities()).checkAndSetBinary(true);

        DesiredCapabilities capabilities = new DesiredCapabilities();
        // these settings shouldn't have any impact
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_PROPERTY,
            LocalBinarySource.FIRST_FILE.getAbsolutePath());
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_VERSION_PROPERTY,
            LocalBinarySource.FIRST_VERSION);

        // verify that the original one is used
        String binary = new LocalBinaryHandler(capabilities).checkAndSetBinary(true);
        assertThat(binary).isEqualTo(latest);
    }

    @Test
    public void verifyWithPropertySetInSystemToFirst() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        // the 1.0.0.Final should be downloaded
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_VERSION_PROPERTY,
            LocalBinarySource.FIRST_VERSION);

        // only download extract and set as executable - don't set into system property
        File first = new LocalBinaryHandler(capabilities).downloadAndPrepare();
        System.setProperty(LocalBinaryHandler.LOCAL_SOURCE_BINARY_PROPERTY, first.getAbsolutePath());

        // this shouldn't have any impact
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_VERSION_PROPERTY,
            LocalBinarySource.LATEST_VERSION);

        // verify that the original one is used
        String binary = new LocalBinaryHandler(capabilities).checkAndSetBinary(true);
        assertThat(binary).isEqualTo(first.getAbsolutePath());
    }

    @Test
    public void verifyWithPropertySetToZip() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        // set binary to zip - test should throw an exception
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_PROPERTY,
            LocalBinarySource.FIRST_FILE.getAbsolutePath());
        // this shouldn't have any impact
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_VERSION_PROPERTY,
            LocalBinarySource.FIRST_VERSION);

        try {
            new LocalBinaryHandler(capabilities).checkAndSetBinary(true);
            if (!PlatformUtils.isWindows()) {
                Assert.fail("This test should have failed on all platforms but Windows");
            }
        } catch (IllegalArgumentException iae) {
            if (PlatformUtils.isWindows()) {
                Assert.fail("This test should have not failed on Windows");
            }
        }
    }

    private void verifyIsDownloadedExtractedSetExecutableSetInSystemProperty(DesiredCapabilities capabilities,
        String downloaded, String extracted, String echo, boolean latest) throws Exception {
        LocalBinaryHandler localBinaryHandler = new LocalBinaryHandler(capabilities);

        File resultingFile = new File(localBinaryHandler.checkAndSetBinary(true));

        // verify downloaded file - should be only one
        File zip = new File(downloaded);
        assertThat(zip).exists().isFile();
        assertThat(zip.getParentFile().listFiles()).hasSize(1);
        LocalBinarySource.assertThatCorrectFileWasDownloaded(latest, zip);

        assertThat(resultingFile).isEqualTo(new File(extracted));
        assertThat(resultingFile.getParentFile().listFiles()).hasSize(1);

        Validate.isExecutable(resultingFile.getAbsolutePath(),
                              "The file has to be an executable file, " + resultingFile);
        assertThat(System.getProperty(LocalBinaryHandler.LOCAL_SOURCE_SYSTEM_BINARY_PROPERTY)).isEqualTo(extracted);

        if (!PlatformUtils.isWindows()) {
            runScriptAndCheck(extracted, echo);
        }
    }

    private void runScriptAndCheck(String script, String expected) {
        PrintStream stdOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Spacelift
            .task(CommandTool.class)
            .command(new CommandBuilder(script))
            .runAsDaemon()
            .interaction(new BinaryInteraction()
                             .outputPrefix("[Local Source] ")
                             .printToOut(".*")
                             .build())
            .execute().awaitAtMost(5, TimeUnit.SECONDS);
        System.setOut(stdOut);
        assertThat(outContent.toString().trim()).isEqualTo("[Local Source] " + expected);
    }

    private String getDownloadedPath(String version, String fileName) {
        return TEST_DRONE_CACHE_DIRECTORY + File.separator + LocalBinaryHandler.LOCAL_SOURCE_CACHE_SUBDIR
            + File.separator + version + File.separator
            + fileName;
    }

    private String getExtractedPath(File originalFile) {
        return Constants.DRONE_TARGET_DIRECTORY + BinaryFilesUtils.getMd5hash(originalFile) + File.separator
            + LocalBinarySource.FILE_NAME;
    }
}
