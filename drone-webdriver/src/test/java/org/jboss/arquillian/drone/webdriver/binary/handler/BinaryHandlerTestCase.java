package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.task.os.CommandTool;
import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.Downloader;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.LocalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.process.BinaryInteraction;
import org.jboss.arquillian.drone.webdriver.utils.Constants;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.ARQUILLIAN_DRONE_CACHE_DIRECTORY;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.DRONE_TARGET_DIRECTORY;

/**
 *
 */
public class BinaryHandlerTestCase {

    private static final String originalCacheDirectory = ARQUILLIAN_DRONE_CACHE_DIRECTORY;
    private static final String originalTargetDirectory = DRONE_TARGET_DIRECTORY;
    private static String TEST_DRONE_TARGET_DIRECTORY = "target" + File.separator + "drone-test" + File.separator;
    private static String TEST_DRONE_CACHE_DIRECTORY = TEST_DRONE_TARGET_DIRECTORY + "cache" + File.separator;

    private StreamHandler customLogHandler;

    @Rule
    public final SystemOutRule outContent = new SystemOutRule().enableLog();

    @BeforeClass
    public static void setTestCacheDirectory() throws NoSuchFieldException, IllegalAccessException {
        setTargetDirectory(TEST_DRONE_TARGET_DIRECTORY);
        setCacheDirectory(TEST_DRONE_CACHE_DIRECTORY);
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

    @Before
    public void cleanupBefore() throws IOException {
        cleanUp();
    }

    @After
    public void cleanupAfter() throws IOException {
        cleanUp();
        if (customLogHandler != null){
            Logger.getLogger("").removeHandler(customLogHandler);
        }
    }

    private void cleanUp() throws IOException {
        File targetDroneDir = new File(TEST_DRONE_TARGET_DIRECTORY);
        if (targetDroneDir.exists()) {
            FileUtils.deleteDirectory(targetDroneDir);
        }
        System.setProperty(LocalBinaryHandler.LOCAL_SOURCE_SYSTEM_BINARY_PROPERTY, "");
        System.setProperty(LocalBinaryHandler.LOCAL_SOURCE_BINARY_PROPERTY, "");
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

    @Test
    public void testBinaryHandlerInMultipleThreads() throws Exception {

        ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
        Logger parentLog = Logger.getLogger("");
        Handler handler = parentLog.getHandlers()[0];
        customLogHandler = new StreamHandler(logOutputStream, handler.getFormatter());
        parentLog.addHandler(customLogHandler);


        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_VERSION_PROPERTY,
            LocalBinarySource.FIRST_VERSION);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(3);

        createThreadWithCheckAndSetBinary(capabilities, startLatch, stopLatch).start();
        createThreadWithCheckAndSetBinary(capabilities, startLatch, stopLatch).start();
        createThreadWithCheckAndSetBinary(capabilities, startLatch, stopLatch).start();

        startLatch.countDown();
        stopLatch.await(10, TimeUnit.SECONDS);

        // verify
        customLogHandler.flush();
        verifyLogContainsOneOccurrence("Drone: downloading", outContent.getLog());
        verifyLogContainsOneOccurrence("Extracting zip file", logOutputStream.toString());
        verifyLogContainsOneOccurrence("marking binary file.+as executable", logOutputStream.toString());
    }

    private void verifyLogContainsOneOccurrence(String message, String log) {
        Matcher matcher = Pattern.compile(message).matcher(log);
        assertThat(matcher.find()).as(String.format(
            "The log should contain one occurrence of message \"%s\" but none was found. For more information see the log",
            message))
            .isTrue();
        assertThat(matcher.find()).as(String.format(
            "The log should contain only one occurrence of message \"%s\" but more than one was found. For more information see the log",
            message)).isFalse();
    }

    private Thread createThreadWithCheckAndSetBinary(DesiredCapabilities capabilities, final CountDownLatch startLatch,
        final CountDownLatch stopLatch) {
        return new Thread(() -> {
            try {
                startLatch.await();
                new LocalBinaryHandler(capabilities).checkAndSetBinary(true);
                stopLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
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
        Spacelift
            .task(CommandTool.class)
            .command(new CommandBuilder(script))
            .runAsDaemon()
            .interaction(new BinaryInteraction()
                .outputPrefix("[Local Source] ")
                .printToOut(".*")
                .build())
            .execute().awaitAtMost(5, TimeUnit.SECONDS);
        assertThat(outContent.getLog().trim()).endsWith("[Local Source] " + expected);
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
