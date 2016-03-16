package org.arquillian.drone.saucelabs.extension.webdriver.connect;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.archive.UntarTool;
import org.arquillian.spacelift.task.archive.UnzipTool;
import org.arquillian.spacelift.task.net.DownloadTool;

/**
 * Is responsible for starting a SauceConnect binary
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class SauceConnectRunner {

    private static Logger log = Logger.getLogger(SauceConnectRunner.class.getName());

    private static SauceConnectRunner sauceConnectRunner = null;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final File sauceConnectDirectory = new File("target" + File.separator + "sauceconnect");
    private final File sauceConnectFile = new File(
        sauceConnectDirectory.getPath() + File.separator + "sc/bin/sc" + (PlatformUtils.isWindows() ? ".exe" : ""));
    private Process process = null;

    private SauceConnectRunner() {
    }

    /**
     * Returns an instance of SauceConnectRunner. If there has been already created, returns this one, otherwise
     * creates and returns a new one - behaves like singleton
     *
     * @return An instance of SauceConnectRunner
     */
    public static SauceConnectRunner createSauceConnectRunnerInstance() {
        if (sauceConnectRunner == null) {
            sauceConnectRunner = new SauceConnectRunner();
        }
        return sauceConnectRunner;
    }

    /**
     * Indirectly runs SauceConnect binary. In case that the binary has been already run, then does nothing.
     *
     * @param username       A username the binary should be ran with
     * @param accessKey      An accessKey the binary should be ran with
     * @param additionalArgs additional arguments
     * @param localBinary    Path to a local binary of the SauceConnect. If none, then it will be downloaded.
     */
    public void runSauceConnect(String username, String accessKey, String additionalArgs, String localBinary) {
        if (process != null) {
            return;
        }
        if (Utils.isEmpty(localBinary)) {
            if (!sauceConnectFile.exists()) {
                prepareSauceConnect();
            }
            runTheBinary(sauceConnectFile, username, accessKey, additionalArgs);

        } else {
            runTheBinary(new File(localBinary), username, accessKey, additionalArgs);
        }
    }

    /**
     * Runs SauceConnect binary. In case that the binary has been already run, then does nothing.
     *
     * @param username       A username the binary should be ran with
     * @param binaryFile     A binary file to be run
     * @param accessKey      An accessKey the binary should be ran with
     * @param additionalArgs additional arguments
     */
    private void runTheBinary(File binaryFile, String username, String accessKey, String additionalArgs) {
        List<String> args = new ArrayList<String>();
        args.add(binaryFile.getAbsolutePath());
        args.add("-u");
        args.add(username);
        args.add("-k");
        args.add(accessKey);
        if (!Utils.isEmpty(additionalArgs)) {
            args.addAll(Arrays.asList(additionalArgs.split(" ")));
        }
        ProcessBuilder processBuilder = new ProcessBuilder().command(args);

        try {
            process = processBuilder.start();

            final Reader reader = new Reader();
            reader.start();
            Runtime.getRuntime().addShutdownHook(new CloseChildProcess());
            countDownLatch.await(30, TimeUnit.SECONDS);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares the SauceConnect binary. Creates the directory target/sauceconnect; downloads a zip file
     * containing the binary; extracts zip file into the created directory and marks the binary as executable.
     */
    private void prepareSauceConnect() {
        String url = BinaryUtils.getPlatformBinaryNameUrl();
        String archiveName = url.substring(url.lastIndexOf("/") + 1);
        File sauceConnectArchiveFile = new File(sauceConnectDirectory.getPath() + File.separator + archiveName);

        log.info("Creating directory: " + sauceConnectDirectory);
        sauceConnectDirectory.mkdir();

        log.info("downloading zip file from: " + url + " into " + sauceConnectArchiveFile.getPath());
        Spacelift.task(DownloadTool.class).from(url).to(sauceConnectArchiveFile.getPath()).execute().await();

        if (archiveName.endsWith(".tar.gz")) {
            log.info("extracting tar file: " + sauceConnectArchiveFile + " into " + sauceConnectDirectory.getPath());
            Spacelift.task(sauceConnectArchiveFile, UntarTool.class).toDir(sauceConnectDirectory.getPath()).execute()
                .await();
        } else {
            log.info("extracting zip file: " + sauceConnectArchiveFile + " into " + sauceConnectDirectory.getPath());
            Spacelift.task(sauceConnectArchiveFile, UnzipTool.class).toDir(sauceConnectDirectory.getPath()).execute()
                .await();
        }

        String fromDirectory =
            sauceConnectDirectory + File.separator + archiveName.replace(".zip", "").replace(".tar.gz", "");
        String toDirectory = sauceConnectDirectory + File.separator + "sc";
        log.info("renaming extracted directory: " + fromDirectory + " to: " + toDirectory);
        new File(fromDirectory).renameTo(new File(toDirectory));

        log.info("marking binary file: " + sauceConnectFile.getPath() + " as executable");
        sauceConnectFile.setExecutable(true);
    }

    /**
     * This thread reads an output from the SauceConnect binary and prints it on the standard output. At the same
     * time it checks if the output contains one of the strings that indicate that the binary has been successfully
     * started and the connection established or that another SauceConnect binary is already running
     */
    private class Reader extends Thread {
        public void run() {

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean isAlreadyRunning = false;

            while (!isAlreadyRunning) {
                try {
                    synchronized (process) {
                        if (!process.isAlive()) {
                            break;
                        }
                        while (in.ready() && (line = in.readLine()) != null) {
                            System.out.println("[SauceConnect]$ " + line);

                            if (countDownLatch.getCount() > 0) {
                                if (line.contains(
                                    "Sauce Connect is up, you may start your tests")) {
                                    countDownLatch.countDown();
                                } else if (line.contains(
                                    "check if Sauce Connect is already running")) {
                                    isAlreadyRunning = true;
                                    countDownLatch.countDown();
                                }
                            }
                        }
                        Thread.sleep(10);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Is responsible for destroying a running SauceConnect binary process
     */
    private class CloseChildProcess extends Thread {
        public void run() {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
