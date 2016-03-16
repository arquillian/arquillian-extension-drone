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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.archive.UnzipTool;
import org.arquillian.spacelift.task.net.DownloadTool;

/**
 * Is responsible for starting a BrowserStackLocal binary
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BrowserStackLocalRunner {

    private static Logger log = Logger.getLogger(BrowserStackLocalRunner.class.getName());

    private static BrowserStackLocalRunner browserStackLocalRunner = null;

    private final File browserStackLocalDirectory = new File("target" + File.separator + "browserstacklocal");
    private final File browserStackLocalFile =
        new File(browserStackLocalDirectory.getPath() + File.separator + "BrowserStackLocal" + (PlatformUtils
            .isWindows() ? ".exe" : ""));
    private final String basicUrl = "https://www.browserstack.com/browserstack-local/";

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private Process process = null;

    private BrowserStackLocalRunner() {
    }

    /**
     * Returns an instance of BrowserStackLocalRunner. If there has been already created, returns this one, otherwise
     * creates and returns a new one - behaves like singleton
     *
     * @return An instance of BrowserStackLocalRunner
     */
    public static BrowserStackLocalRunner createBrowserStackLocalInstance() {
        if (browserStackLocalRunner == null) {
            browserStackLocalRunner = new BrowserStackLocalRunner();
        }
        return browserStackLocalRunner;
    }

    /**
     * Indirectly runs BrowserStackLocal binary. In case that the binary has been already run, then does nothing.
     *
     * @param accessKey      An accessKey the binary should be ran with
     * @param additionalArgs additional arguments
     * @param localBinary    Path to a local binary of the BrowserStackLocal. If none, then it will be downloaded.
     */
    public void runBrowserStackLocal(String accessKey, String additionalArgs, String localBinary) {
        if (process != null) {
            return;
        }
        if (isEmpty(localBinary)) {
            if (!browserStackLocalFile.exists()) {
                prepareBrowserStackLocal();
            }
            runTheBinary(browserStackLocalFile, accessKey, additionalArgs);

        } else {
            runTheBinary(new File(localBinary), accessKey, additionalArgs);
        }
    }

    /**
     * Runs BrowserStackLocal binary. In case that the binary has been already run, then does nothing.
     *
     * @param binaryFile     A binary file to be run
     * @param accessKey      An accessKey the binary should be ran with
     * @param additionalArgs additional arguments
     */
    private void runTheBinary(File binaryFile, String accessKey, String additionalArgs) {
        List<String> args = new ArrayList<String>();
        args.add(binaryFile.getAbsolutePath());
        args.add(accessKey);
        if (!isEmpty(additionalArgs)) {
            args.addAll(Arrays.asList(additionalArgs.split(" ")));
        }
        ProcessBuilder processBuilder = new ProcessBuilder().command(args);

        try {
            process = processBuilder.start();

            final Reader reader = new Reader();
            reader.start();
            Runtime.getRuntime().addShutdownHook(new CloseChildProcess());
            countDownLatch.await(20, TimeUnit.SECONDS);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares the BrowserStackLocal binary. Creates the directory target/browserstacklocal; downloads a zip file
     * containing the binary; extracts it into the created directory and marks the binary as executable.
     */
    private void prepareBrowserStackLocal() {
        String platformBinaryNameUrl = getPlatformBinaryNameUrl();
        File browserStackLocalZipFile =
            new File(browserStackLocalDirectory.getPath() + File.separator + platformBinaryNameUrl);
        String url = basicUrl + platformBinaryNameUrl;

        log.info("Creating directory: " + browserStackLocalDirectory);
        browserStackLocalDirectory.mkdir();

        log.info("downloading zip file from: " + url + " to " + browserStackLocalZipFile.getPath());
        Spacelift.task(DownloadTool.class).from(url).to(browserStackLocalZipFile.getPath()).execute().await();

        log.info("extracting zip file: " + browserStackLocalZipFile + " to " + browserStackLocalDirectory.getPath());
        Spacelift.task(browserStackLocalZipFile, UnzipTool.class).toDir(browserStackLocalDirectory.getPath()).execute()
            .await();

        log.info("marking binary file: " + browserStackLocalFile.getPath() + " as executable");
        browserStackLocalFile.setExecutable(true);
    }

    /**
     * Returns name of a zip file, that should contain the BrowserStackLocal binary. The name contains corresponding
     * name of the platform the program is running on.
     *
     * @return Formatted name of the BrowserStackLocal zip file
     */
    private String getPlatformBinaryNameUrl() {
        String binary = "BrowserStackLocal-%s.zip";
        switch (PlatformUtils.platform().os()) {
            case WINDOWS:
                return String.format(binary, "win32");
            case UNIX:
                if (PlatformUtils.is64()) {
                    return String.format(binary, "linux-x64");
                } else {
                    return String.format(binary, "linux-ia32");
                }
            case MACOSX:
                return String.format(binary, "darwin-x64");
            default:
                throw new IllegalStateException("The current platform is not supported."
                                                    + "Supported platforms are windows, linux and macosx."
                                                    + "Your platform has been detected as "
                                                    + PlatformUtils.platform().os().toString().toLowerCase() + ""
                                                    + "from the the system property 'os.name' => '" + PlatformUtils.OS
                                                    + "'.");

        }
    }

    private boolean isEmpty(String object) {
        return object == null || object.isEmpty();
    }

    /**
     * This thread reads an output from the BrowserStackLocal binary and prints it on the standard output. At the same
     * time it checks if the output contains one of the strings that indicate that the binary has been successfully
     * started and the connection established or that another BrowserStackLocal binary is already running
     */
    private class Reader extends Thread {
        public void run() {

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean isAlreadyRunning = false;
            FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new WaitForProcess());
            Executors.newSingleThreadExecutor().submit(futureTask);

            while (!isAlreadyRunning) {
                try {
                    synchronized (process) {
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
     * Waits until the SauceConnect binary process ends
     */
    private class WaitForProcess implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            process.waitFor();
            return true;
        }
    }

    /**
     * Is responsible for destroying a running BrowserStackLocal binary process
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
