package org.jboss.arquillian.drone.webdriver.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.archive.UntarTool;
import org.arquillian.spacelift.task.archive.UnzipTool;

import static org.jboss.arquillian.drone.webdriver.utils.Constants.DRONE_TARGET_DIRECTORY;

/**
 * A util class for binary files
 */
public class BinaryFilesUtils {

    private static final Logger log = Logger.getLogger(BinaryFilesUtils.class.toString());

    /**
     * Extracts given archive into a directory <code>target/drone/md5hash(archive)/</code>
     * <br/>
     * If the archive is not one of the archives that are supported, then the file is only copied. Supported archive
     * types are: <code>.zip</code>, <code>.tar.gz</code> and <code>.tar.bz2</code>
     *
     * @param toExtract
     *     File that should be extracted
     *
     * @return Directory where the extraction is located
     *
     * @throws Exception
     *     If anything bad happens
     */
    public static File extract(File toExtract) throws Exception {

        String dir = getMd5hash(toExtract);
        if (dir == null) {
            dir = UUID.randomUUID().toString();
        }
        File targetDir = DRONE_TARGET_DIRECTORY.resolve(dir).toFile();

        synchronized (log) {
            if (!targetDir.exists() || targetDir.listFiles().length == 0) {

                targetDir.mkdirs();
                String filePath = toExtract.getAbsolutePath();

                log.info("Extracting zip file: " + toExtract + " to " + targetDir.getPath());
                if (filePath.endsWith(".zip")) {
                    Spacelift.task(toExtract, UnzipTool.class).toDir(targetDir).execute().await();
                } else if (filePath.endsWith(".tar.gz")) {
                    Spacelift.task(toExtract, UntarTool.class).gzip(true).toDir(targetDir).execute().await();
                } else if (filePath.endsWith(".tar.bz2")) {
                    Spacelift.task(toExtract, UntarTool.class).bzip2(true).toDir(targetDir).execute().await();
                } else {
                    log.info(
                        "The file " + toExtract + " is not compressed by a format that is supported by Drone. "
                            + "Drone supported formats are .zip, .tar.gz, .tar.bz2. The file will be only copied");
                    targetDir.mkdirs();
                    Files.copy(toExtract.toPath(), targetDir.toPath().resolve(toExtract.getName()),
                        StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return targetDir;
    }

    /**
     * Counts and returns md5 hash of the given file
     *
     * @param file
     *     A file the md5 hash should be counted for
     *
     * @return md5 hash of the given file
     */
    public static String getMd5hash(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return DigestUtils.md5Hex(fis);
        } catch (IOException e) {
            log.warning("A problem occurred when md5 hash of a file " + file + " was being retrieved:\n"
                + e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.warning("A problem occurred when FileInputStream of a file " + file
                        + "was being closed:\n" + e.getMessage());
                }
            }
        }
        return null;
    }
}
