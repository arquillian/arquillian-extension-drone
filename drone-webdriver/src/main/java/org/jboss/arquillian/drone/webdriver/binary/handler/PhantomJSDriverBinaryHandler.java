package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.IOException;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.PropertySecurityAction;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.jboss.arquillian.phantom.resolver.ResolvingPhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

/**
 * A class for handling PhantomJS binaries
 * <br/>
 * <b>Not fully implemented - downloading is not supported using an {@link ExternalBinarySource}</b>
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class PhantomJSDriverBinaryHandler extends AbstractBinaryHandler {

    public static final String PHANTOMJS_BINARY_VERSION_PROPERTY = "phantomjsBinaryVersion";
    public static final String PHANTOMJS_BINARY_URL_PROPERTY = "phantomjsBinaryUrl";

    private DesiredCapabilities capabilities;

    public PhantomJSDriverBinaryHandler(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    // TODO: fixme - rewrite to use download feature implemented in drone
    public String checkAndSetBinary(boolean performExecutableValidations) {
        try {
            ResolvingPhantomJSDriverService.createDefaultService(capabilities);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return checkAndSetBinaryWithoutResolve();
    }

    // TODO: fixme - rewrite to use download feature implemented in drone
    public String checkAndSetBinaryWithoutResolve() {
        String executablePath = PropertySecurityAction.getProperty(PHANTOMJS_EXECUTABLE_PATH_PROPERTY);

        if (Validate.empty(executablePath)) {
            executablePath = (String) capabilities.getCapability(PHANTOMJS_EXECUTABLE_PATH_PROPERTY);
        }

        if (!Validate.empty(executablePath)) {
            PropertySecurityAction.setProperty(PHANTOMJS_EXECUTABLE_PATH_PROPERTY, executablePath);
        }
        return executablePath;
    }

    // TODO: fixme - rewrite to use download feature implemented in drone
    //    protected File downloadAndPrepare(File targetDir, URL from) throws Exception {
    //        File downloaded = Downloader.download(targetDir, from);
    //        File extraction = BinaryFilesUtils.extract(downloaded);
    //        File[] dir = extraction.listFiles(file -> file.isDirectory());
    //        if (dir.length == 0) {
    //            throw new IllegalStateException(
    //                "There was expected that in the directory " + extraction + " should be directory containing PhantomJS");
    //        }
    //
    //        // fixme: this is only a temporary solution - it won't be publicly announced till it is fixed
    //        String phantomjsBinary = "phantomjs" + (PlatformUtils.isWindows() ? ".exe" : "");
    //        File binary = new File(dir[0].getAbsolutePath(), "bin" + File.separator + phantomjsBinary);
    //        if (!binary.exists()) {
    //            throw new IllegalStateException("The PhantomJS binary: " + binary + "does not exist.");
    //        }
    //        return markAsExecutable(binary);
    //    }

    @Override
    protected String getBinaryProperty() {
        return null;
    }

    @Override
    public String getSystemBinaryProperty() {
        return PHANTOMJS_EXECUTABLE_PATH_PROPERTY;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.PhantomJS().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return PHANTOMJS_BINARY_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return PHANTOMJS_BINARY_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return null;
    }

    @Override
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }
}
