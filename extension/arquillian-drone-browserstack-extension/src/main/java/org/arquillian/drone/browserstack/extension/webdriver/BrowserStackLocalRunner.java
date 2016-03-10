package org.arquillian.drone.browserstack.extension.webdriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.archive.UnzipTool;
import org.arquillian.spacelift.task.net.DownloadTool;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BrowserStackLocalRunner {

    private static Logger log = Logger.getLogger(BrowserStackLocalRunner.class.getName());

    private static AtomicBoolean isStarted = new AtomicBoolean(false);
    private static BrowserStackLocalRunner browserStackLocalRunner = null;

    private final File browserStackLocalDirectory = new File("target" + File.separator + "browserstacklocal");
    private final File browserStackLocalFile =
        new File(browserStackLocalDirectory.getPath() + File.separator + "BrowserStackLocal");
    private final String basicUrl = "https://www.browserstack.com/browserstack-local/";
    private Process process = null;

    private BrowserStackLocalRunner() {
    }

    public static BrowserStackLocalRunner createBrowserStackLocalInstance() {
        if (browserStackLocalRunner == null) {
            browserStackLocalRunner = new BrowserStackLocalRunner();
        }
        return browserStackLocalRunner;
    }

    public void runBrowserstackLocal(String accessKey) {
        if (process != null) {
            return;
        }
        if (!browserStackLocalFile.exists()) {
            prepareBrowserStackLocal();
        }
        browserStackLocalFile.setExecutable(true);

        ProcessBuilder processBuilder =
            new ProcessBuilder().command(browserStackLocalFile.getAbsolutePath(), "-v", accessKey);
        try {
            process = processBuilder.start();

            final Reader reader = new Reader();
            reader.start();
            Runtime.getRuntime().addShutdownHook(new CloseChildThread());

            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final Thread isStartedChecker = new Thread() {
                public void run() {
                    while (!isStarted.get()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    countDownLatch.countDown();
                }
            };
            isStartedChecker.start();
            countDownLatch.await(20, TimeUnit.SECONDS);



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
    }

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
                        while (in.ready() && !isEmpty(line = in.readLine())) {
                            System.out.println("[BrowserStackLocal]$ " + line);

                            if (!isStarted.get()) {
                                if (line.contains(
                                    "You can now access your local server(s) in our remote browser.")) {
                                    isStarted = new AtomicBoolean(true);

                                } else if (line.contains(
                                    "Either another browserstack local client is running on your machine or some server is listening on port")) {
                                    isAlreadyRunning = true;
                                    isStarted = new AtomicBoolean(true);
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

    private class CloseChildThread extends Thread {
        public void run() {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    ;
}
