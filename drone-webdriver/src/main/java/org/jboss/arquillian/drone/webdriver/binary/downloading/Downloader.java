package org.jboss.arquillian.drone.webdriver.binary.downloading;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.execution.Execution;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.task.net.DownloadTool;

import static org.jboss.arquillian.drone.webdriver.utils.Constants.DRONE_TARGET_DIRECTORY;

/**
 * A util class that enables you to download files.
 */
public class Downloader {

    public static final Path DRONE_TARGET_DOWNLOADED_DIRECTORY = DRONE_TARGET_DIRECTORY.resolve("downloaded");
    private static final Logger log = Logger.getLogger(Downloader.class.toString());

    /**
     * Downloads file from the given url and stores it either in given directory or if the directory is null, then in
     * a directory <code>target/drone/downloaded/</code>. The downloaded file is then returned.
     *
     * @param targetDir
     *     A directory where the downloaded file should be stored
     * @param from
     *     A url a file should be downloaded from.
     *
     * @return The downloaded file
     */
    public static File download(File targetDir, URL from) {
        if (targetDir == null) {
            targetDir = DRONE_TARGET_DOWNLOADED_DIRECTORY.toFile();
        }
        String fromUrl = from.toString();
        String fileName = fromUrl.substring(fromUrl.lastIndexOf("/") + 1);
        File target = new File(targetDir + File.separator + fileName);
        File downloaded = null;

        synchronized (log) {
            if (target.exists() && target.isFile()) {
                downloaded = target;
            } else if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            if (downloaded == null) {
                for (int i = 2; i >= 0; i--) {
                    try {
                        downloaded = runDownloadExecution(from, target.getAbsolutePath(), fileName).await();
                    } catch (ExecutionException ee) {
                        System.err.print("ERROR: the downloading has failed. ");
                        if (i != 0) {
                            System.err.println("Trying again - number of remaining attempts: " + i);
                            continue;
                        } else {
                            System.err.println("For more information see the stacktrace of an exception");
                            throw ee;
                        }
                    }
                    break;
                }
            }
        }
        return downloaded;
    }

    private static Execution<File> runDownloadExecution(URL from, String target, String fileName) {
        Execution<File> execution = Spacelift.task(DownloadTool.class).from(from).to(target).execute();
        System.out.println(String.format("Drone: downloading %s from %s to %s ", fileName, from, target));

        while (!execution.isFinished()) {
            System.out.print(".");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.warning("Problem occurred when the thread was sleeping:\n" + e.getMessage());
            }
        }
        System.out.println();

        return execution;
    }
}
