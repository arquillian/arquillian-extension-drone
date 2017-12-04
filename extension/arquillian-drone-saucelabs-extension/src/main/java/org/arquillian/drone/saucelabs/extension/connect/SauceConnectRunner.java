/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.drone.saucelabs.extension.connect;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.commons.lang3.SystemUtils;
import org.arquillian.drone.saucelabs.extension.utils.BinaryUrlUtils;
import org.arquillian.drone.saucelabs.extension.utils.Utils;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.archive.UntarTool;
import org.arquillian.spacelift.task.archive.UnzipTool;
import org.arquillian.spacelift.task.net.DownloadTool;

/**
 * Is responsible for starting a SauceConnect binary
 */
public class SauceConnectRunner {

    private static Logger log = Logger.getLogger(SauceConnectRunner.class.getName());

    private static SauceConnectRunner sauceConnectRunner = null;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final Path sauceConnectDirectory = Paths.get(System.getProperty("user.dir"), "target", "sauceconnect");
    private final Path sauceConnectFile =
        sauceConnectDirectory.resolve("sc").resolve("bin").resolve("sc" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
    private Process sauceConnectBinary = null;

    private SauceConnectRunner() {
    }

    /**
     * Returns an instance of SauceConnectRunner. If there has been already created, returns this one, otherwise
     * creates and returns a new one - behaves like singleton
     *
     * @return An instance of SauceConnectRunner
     */
    public static SauceConnectRunner getSauceConnectRunnerInstance() {
        if (sauceConnectRunner == null) {
            sauceConnectRunner = new SauceConnectRunner();
        }
        return sauceConnectRunner;
    }

    /**
     * Indirectly runs SauceConnect binary. In case that the binary has been already run, then does nothing.
     *
     * @param username
     *     A username the binary should be ran with
     * @param accessKey
     *     An accessKey the binary should be ran with
     * @param additionalArgs
     *     additional arguments
     * @param localBinary
     *     Path to a local binary of the SauceConnect. If none, then it will be downloaded.
     *
     * @throws SauceConnectException
     *     when something bad happens during running BrowserStackLocal binary
     */
    public void runSauceConnect(String username, String accessKey, String additionalArgs, String localBinary)
        throws SauceConnectException {
        if (sauceConnectBinary != null) {
            return;
        }
        if (Utils.isNullOrEmpty(localBinary)) {
            if (!Files.exists(sauceConnectFile)) {
                prepareSauceConnect();
            }
            runSauceConnect(sauceConnectFile, username, accessKey, additionalArgs);
        } else {
            runSauceConnect(Paths.get(localBinary), username, accessKey, additionalArgs);
        }
    }

    /**
     * Runs SauceConnect binary. In case that the binary has been already run, then does nothing.
     *
     * @param username
     *     A username the binary should be ran with
     * @param binaryFile
     *     A binary file to be run
     * @param accessKey
     *     An accessKey the binary should be ran with
     * @param additionalArgs
     *     additional arguments
     *
     * @throws SauceConnectException
     *     when something bad happens during running BrowserStackLocal binary
     */
    private void runSauceConnect(Path binaryFile, String username, String accessKey, String additionalArgs)
        throws SauceConnectException {
        List<String> args = new ArrayList<String>();
        args.add(binaryFile.toAbsolutePath().toString());
        args.add("-u");
        args.add(username);
        args.add("-k");
        args.add(accessKey);
        if (!Utils.isNullOrEmpty(additionalArgs)) {
            args.addAll(Arrays.asList(additionalArgs.split(" ")));
        }
        ProcessBuilder processBuilder = new ProcessBuilder().command(args);

        try {
            sauceConnectBinary = processBuilder.start();

            final Reader reader = new Reader();
            reader.start();
            Runtime.getRuntime().addShutdownHook(new ChildProcessCloser());
            countDownLatch.await(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new SauceConnectException("Running SauceConnect binary unexpectedly failed: ", e);
        }
    }

    /**
     * Prepares the SauceConnect binary. Creates the directory target/sauceconnect; downloads a zip file
     * containing the binary; extracts zip file into the created directory and marks the binary as executable.
     */
    private void prepareSauceConnect() {
        String url = BinaryUrlUtils.getPlatformBinaryNameUrl();
        String archiveName = url.substring(url.lastIndexOf("/") + 1);
        File sauceConnectArchiveFile = sauceConnectDirectory.resolve(archiveName).toFile();

        log.info("Creating directory: " + sauceConnectDirectory);
        sauceConnectDirectory.toFile().mkdir();

        log.info("downloading zip file from: " + url + " into " + sauceConnectArchiveFile.getPath());
        Spacelift.task(DownloadTool.class)
            .from(url)
            .to(sauceConnectArchiveFile.getPath())
            .execute().await();

        if (archiveName.endsWith(".tar.gz")) {
            log.info("extracting tar file: " + sauceConnectArchiveFile + " into " + sauceConnectDirectory);
            Spacelift.task(sauceConnectArchiveFile, UntarTool.class)
                .toDir(sauceConnectDirectory.toString())
                .execute().await();
        } else {
            log.info("extracting zip file: " + sauceConnectArchiveFile + " into " + sauceConnectDirectory);
            Spacelift.task(sauceConnectArchiveFile, UnzipTool.class)
                .toDir(sauceConnectDirectory.toString())
                .execute().await();
        }

        File fromDirectory =
            sauceConnectDirectory.resolve(archiveName.replace(".zip", "").replace(".tar.gz", "")).toFile();
        File toDirectory = sauceConnectDirectory.resolve("sc").toFile();

        log.info("renaming extracted directory: " + fromDirectory + " to: " + toDirectory);
        fromDirectory.renameTo(toDirectory);

        log.info("marking binary file: " + sauceConnectFile + " as executable");
        try {
            sauceConnectFile.toFile().setExecutable(true);
        } catch (SecurityException se) {
            log.severe("The downloaded SauceConnect binary: " + sauceConnectFile
                + " could not be set as executable. This may cause additional problems.");
        }
    }

    /**
     * This thread reads an output from the SauceConnect binary and prints it on the standard output. At the same
     * time it checks if the output contains one of the strings that indicate that the binary has been successfully
     * started and the connection established or that another SauceConnect binary is already running
     */
    private class Reader extends Thread {
        public void run() {

            BufferedReader in = new BufferedReader(new InputStreamReader(sauceConnectBinary.getInputStream()));
            String line;
            boolean isAlreadyRunning = false;
            FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new ProcessEndChecker());
            Executors.newSingleThreadExecutor().submit(futureTask);
            while (!isAlreadyRunning) {
                try {
                    synchronized (sauceConnectBinary) {
                        if (futureTask.isDone()) {
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
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    throw new SauceConnectException("Reading SauceConnect binary output unexpectedly failed: ", e);
                }
            }
        }
    }

    /**
     * Waits until the SauceConnect binary ends
     */
    private class ProcessEndChecker implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            sauceConnectBinary.waitFor();
            return true;
        }
    }

    /**
     * Is responsible for destroying a running SauceConnect binary
     */
    private class ChildProcessCloser extends Thread {
        public void run() {
            sauceConnectBinary.destroy();
            try {
                sauceConnectBinary.waitFor();
            } catch (InterruptedException e) {
                throw new SauceConnectException("Stopping SauceConnect binary unexpectedly failed: ", e);
            }
        }
    }
}
