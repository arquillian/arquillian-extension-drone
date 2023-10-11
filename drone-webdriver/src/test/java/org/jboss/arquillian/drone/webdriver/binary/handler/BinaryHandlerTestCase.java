package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.MutableCapabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.ARQUILLIAN_DRONE_CACHE_DIRECTORY;
import static org.jboss.arquillian.drone.webdriver.utils.Constants.DRONE_TARGET_DIRECTORY;

public class BinaryHandlerTestCase {

    private static final Path originalCacheDirectory = ARQUILLIAN_DRONE_CACHE_DIRECTORY;
    private static final Path originalTargetDirectory = DRONE_TARGET_DIRECTORY;

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final SystemOutRule outContent = new SystemOutRule().enableLog();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path testDroneCacheDir;
    private StreamHandler customLogHandler;

    @Before
    public void setTestCacheDirectory() throws NoSuchFieldException, IllegalAccessException, IOException, InvocationTargetException, NoSuchMethodException {
        setTargetDirectory(temporaryFolder.newFolder("drone-test").toPath());
        testDroneCacheDir = temporaryFolder.newFolder("cache").toPath();
        setCacheDirectory(testDroneCacheDir);
    }

    @After
    public void setOriginalCacheDirectory() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        setTargetDirectory(originalTargetDirectory);
        setCacheDirectory(originalCacheDirectory);
        if (customLogHandler != null) {
            Logger.getLogger("").removeHandler(customLogHandler);
        }
    }

    private void setCacheDirectory(Path dirToSet) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        setConstantProperty("ARQUILLIAN_DRONE_CACHE_DIRECTORY", dirToSet);
    }

    private void setTargetDirectory(Path dirToSet) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        setConstantProperty("DRONE_TARGET_DIRECTORY", dirToSet);
    }

    private void setConstantProperty(String propertyVariable, Path value)
        throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field constantField = Constants.class.getField(propertyVariable);
        constantField.setAccessible(true);

        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
        Field modifiersField = Arrays.stream(fields).filter(f -> "modifiers".equals(f.getName())).findFirst().get();

        modifiersField.setAccessible(true);
        modifiersField.setInt(constantField, constantField.getModifiers() & ~Modifier.FINAL);
        constantField.set(null, value);
    }

    @Test
    public void verifyWithoutAnyCapabilitySet() throws Exception {

        // the latest release should be downloaded and prepared
        verifyIsDownloadedExtractedSetExecutableSetInSystemProperty(
            new MutableCapabilities(),
            getDownloadedPath(LocalBinarySource.LATEST_VERSION, LocalBinarySource.LATEST_FILE.getName()),
            getExtractedPath(LocalBinarySource.LATEST_FILE),
            LocalBinarySource.ECHO_LATEST_SCRIPT,
            true);
    }

    @Test
    public void verifyWithVersionCapabilitySet() throws Exception {
        MutableCapabilities capabilities = new MutableCapabilities();
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
        MutableCapabilities capabilities = new MutableCapabilities();
        // set url the file should be downloaded from - without specifying version
        capabilities.setCapability(
            LocalBinaryHandler.LOCAL_SOURCE_BINARY_URL_PROPERTY,
            LocalBinarySource.FIRST_FILE.toURI().toString());

        // the 1.0.0.Final release should be downloaded to target/drone/downloaded directory
        verifyIsDownloadedExtractedSetExecutableSetInSystemProperty(
            capabilities,
            Downloader.DRONE_TARGET_DOWNLOADED_DIRECTORY.resolve(LocalBinarySource.FIRST_FILE.getName()),
            getExtractedPath(LocalBinarySource.FIRST_FILE),
            LocalBinarySource.ECHO_FIRST_SCRIPT,
            false);
    }

    @Test
    public void verifyWithUrlAndVersionCapabilitySet() throws Exception {
        String myCoolVersion = "my-cool-version";
        MutableCapabilities capabilities = new MutableCapabilities();
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
        MutableCapabilities capabilities = new MutableCapabilities();
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
        String latest = new LocalBinaryHandler(new MutableCapabilities()).checkAndSetBinary(true);

        MutableCapabilities capabilities = new MutableCapabilities();
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
        MutableCapabilities capabilities = new MutableCapabilities();
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
        MutableCapabilities capabilities = new MutableCapabilities();
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


        MutableCapabilities capabilities = new MutableCapabilities();
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

    private Thread createThreadWithCheckAndSetBinary(MutableCapabilities capabilities, final CountDownLatch startLatch,
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

    private void verifyIsDownloadedExtractedSetExecutableSetInSystemProperty(MutableCapabilities capabilities,
        Path downloaded, Path extracted, String echo, boolean latest) throws Exception {
        LocalBinaryHandler localBinaryHandler = new LocalBinaryHandler(capabilities);

        File resultingFile = new File(localBinaryHandler.checkAndSetBinary(true));

        // verify downloaded file - should be only one
        File zip = downloaded.toFile();
        assertThat(zip).exists().isFile();
        assertThat(zip.getParentFile().listFiles()).hasSize(1);
        LocalBinarySource.assertThatCorrectFileWasDownloaded(latest, zip);

        assertThat(resultingFile).isEqualTo(extracted.toFile());
        assertThat(resultingFile.getParentFile().listFiles()).hasSize(1);

        Validate.isExecutable(resultingFile.getAbsolutePath(),
            "The file has to be an executable file, " + resultingFile);
        assertThat(System.getProperty(LocalBinaryHandler.LOCAL_SOURCE_SYSTEM_BINARY_PROPERTY)).isEqualTo(
            extracted.toString());

        if (!PlatformUtils.isWindows()) {
            runScriptAndCheck(extracted.toString(), echo);
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

    private Path getDownloadedPath(String version, String fileName) {
        return testDroneCacheDir.resolve(LocalBinaryHandler.LOCAL_SOURCE_CACHE_SUBDIR)
            .resolve(version)
            .resolve(fileName)
            .toAbsolutePath();
    }

    private Path getExtractedPath(File originalFile) {
        return Constants.DRONE_TARGET_DIRECTORY.resolve(BinaryFilesUtils.getMd5hash(originalFile))
            .resolve(LocalBinarySource.FILE_NAME);
    }
}
