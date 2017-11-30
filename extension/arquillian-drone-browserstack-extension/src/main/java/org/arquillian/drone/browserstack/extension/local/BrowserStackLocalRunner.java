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
package org.arquillian.drone.browserstack.extension.local;

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
import org.arquillian.drone.browserstack.extension.utils.Utils;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.archive.UnzipTool;
import org.arquillian.spacelift.task.net.DownloadTool;

/**
 * Is responsible for starting a BrowserStackLocal binary
 */
public class BrowserStackLocalRunner {

    private static Logger log = Logger.getLogger(BrowserStackLocalRunner.class.getName());

    private static BrowserStackLocalRunner browserStackLocalRunner = null;

    private final Path browserStackLocalDirectory =
        Paths.get(System.getProperty("user.dir"), "target", "browserstacklocal");
    private final Path browserStackLocalFile =
        browserStackLocalDirectory.resolve("BrowserStackLocal" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
    private final String basicUrl = "https://www.browserstack.com/browserstack-local/";

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private Process browserStackLocalBinary = null;

    private BrowserStackLocalRunner() {
    }

    /**
     * Returns an instance of BrowserStackLocalRunner. If there has been already created, returns this one, otherwise
     * creates and returns a new one - behaves like singleton
     *
     * @return An instance of BrowserStackLocalRunner
     */
    public static BrowserStackLocalRunner getBrowserStackLocalInstance() {
        if (browserStackLocalRunner == null) {
            browserStackLocalRunner = new BrowserStackLocalRunner();
        }
        return browserStackLocalRunner;
    }

    /**
     * Indirectly runs BrowserStackLocal binary. In case that the binary has been already run, then does nothing.
     *
     * @param accessKey
     *     An accessKey the binary should be ran with
     * @param additionalArgs
     *     additional arguments
     * @param localBinary
     *     Path to a local binary of the BrowserStackLocal. If none, then it will be downloaded.
     *
     * @throws BrowserStackLocalException
     *     when something bad happens during running BrowserStackLocal binary
     */
    public void runBrowserStackLocal(String accessKey, String additionalArgs, String localBinary)
        throws BrowserStackLocalException {
        if (browserStackLocalBinary != null) {
            log.fine("One BrowserStackLocal binary has been already started.");
            return;
        }
        if (Utils.isNullOrEmpty(localBinary)) {
            if (!Files.exists(browserStackLocalFile)) {
                prepareBrowserStackLocal();
            }
            runBrowserStackLocal(browserStackLocalFile, accessKey, additionalArgs);
        } else {
            runBrowserStackLocal(Paths.get(localBinary), accessKey, additionalArgs);
        }
    }

    /**
     * Runs BrowserStackLocal binary. In case that the binary has been already run, then does nothing.
     *
     * @param binaryFile
     *     A binary file to be run
     * @param accessKey
     *     An accessKey the binary should be ran with
     * @param additionalArgs
     *     additional arguments
     *
     * @throws BrowserStackLocalException
     *     when something bad happens during running BrowserStackLocal binary
     */
    private void runBrowserStackLocal(Path binaryFile, String accessKey, String additionalArgs)
        throws BrowserStackLocalException {
        List<String> args = new ArrayList<String>();
        args.add(binaryFile.toAbsolutePath().toString());
        args.add(accessKey);
        if (!Utils.isNullOrEmpty(additionalArgs)) {
            args.addAll(Arrays.asList(additionalArgs.split(" ")));
        }
        ProcessBuilder processBuilder = new ProcessBuilder().command(args);

        try {
            browserStackLocalBinary = processBuilder.start();

            final Reader reader = new Reader();
            reader.start();
            Runtime.getRuntime().addShutdownHook(new ChildProcessCloser());
            countDownLatch.await(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new BrowserStackLocalException("Running BrowserStackLocal binary unexpectedly failed: ", e);
        }
    }

    /**
     * Prepares the BrowserStackLocal binary. Creates the directory target/browserstacklocal; downloads a zip file
     * containing the binary; extracts it into the created directory and marks the binary as executable.
     */
    private void prepareBrowserStackLocal() {
        String platformBinaryNameUrl = getPlatformBinaryNameUrl();
        File browserStackLocalZipFile = browserStackLocalDirectory.resolve(platformBinaryNameUrl).toFile();
        String url = basicUrl + platformBinaryNameUrl;

        log.info("Creating directory: " + browserStackLocalDirectory);
        browserStackLocalDirectory.toFile().mkdir();

        log.info("downloading zip file from: " + url + " to " + browserStackLocalZipFile);
        Spacelift.task(DownloadTool.class)
            .from(url)
            .to(browserStackLocalZipFile)
            .execute().await();

        log.info("extracting zip file: " + browserStackLocalZipFile + " to " + browserStackLocalDirectory);
        Spacelift.task(browserStackLocalZipFile, UnzipTool.class)
            .toDir(browserStackLocalDirectory.toFile())
            .execute().await();

        log.info("marking binary file: " + browserStackLocalFile + " as executable");
        try {
            browserStackLocalFile.toFile().setExecutable(true);
        } catch (SecurityException se) {
            log.severe("The downloaded BrowserStackLocal binary: " + browserStackLocalFile
                + " could not be set as executable. This may cause additional problems.");
        }
    }

    /**
     * Returns name of a zip file, that should contain the BrowserStackLocal binary. The name contains corresponding
     * name of the platform the program is running on.
     *
     * @return Formatted name of the BrowserStackLocal zip file
     */
    private String getPlatformBinaryNameUrl() {
        String binary = "BrowserStackLocal-%s.zip";

        if (SystemUtils.IS_OS_WINDOWS) {
            return String.format(binary, "win32");
        } else if (SystemUtils.IS_OS_UNIX) {
            if (Utils.is64()) {
                return String.format(binary, "linux-x64");
            } else {
                return String.format(binary, "linux-ia32");
            }
        } else if (SystemUtils.IS_OS_MAC) {
            return String.format(binary, "darwin-x64");
        } else {
            throw new IllegalStateException("The current platform is not supported."
                + "Supported platforms are windows, linux and macosx."
                + "Your platform has been detected as "
                + SystemUtils.OS_NAME);
        }
    }

    /**
     * This thread reads an output from the BrowserStackLocal binary and prints it on the standard output. At the same
     * time it checks if the output contains one of the strings that indicate that the binary has been successfully
     * started and the connection established or that another BrowserStackLocal binary is already running
     */
    private class Reader extends Thread {
        public void run() {

            BufferedReader in = new BufferedReader(new InputStreamReader(browserStackLocalBinary.getInputStream()));
            String line;
            boolean isAlreadyRunning = false;
            FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new ProcessEndChecker());
            Executors.newSingleThreadExecutor().submit(futureTask);

            while (!isAlreadyRunning) {
                try {
                    synchronized (browserStackLocalBinary) {
                        if (futureTask.isDone()) {
                            break;
                        }
                        while (in.ready() && (line = in.readLine()) != null) {
                            System.out.println("[BrowserStackLocal]$ " + line);

                            if (countDownLatch.getCount() > 0) {
                                if (line.contains(
                                    "You can now access your local server(s) in our remote browser.")) {
                                    countDownLatch.countDown();
                                } else if (line.contains(
                                    "Either another browserstack local client is running on your machine or some server is listening on port")) {
                                    isAlreadyRunning = true;
                                    countDownLatch.countDown();
                                }
                            }
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    throw new BrowserStackLocalException(
                        "Reading BrowserStackLocal binary output unexpectedly failed: ", e);
                }
            }
        }
    }

    /**
     * Waits until the BrowserStackLocal binary ends
     */
    private class ProcessEndChecker implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            browserStackLocalBinary.waitFor();
            return true;
        }
    }

    /**
     * Is responsible for destroying a running BrowserStackLocal binary
     */
    private class ChildProcessCloser extends Thread {
        public void run() {
            browserStackLocalBinary.destroy();
            try {
                browserStackLocalBinary.waitFor();
            } catch (InterruptedException e) {
                throw new BrowserStackLocalException(
                    "Stopping BrowserStackLocal binary unexpectedly failed: ", e);
            }
        }
    }
}
