package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.utils.PropertySecurityAction;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

/**
 *
 * A class for handling PhantomJS binaries
 * <br/>
 * <b>Not fully implemented - downloading is not supported using an {@link ExternalBinarySource}</b>
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class PhantomJSDriverBinaryHandler extends AbstractBinaryHandler {

    private DesiredCapabilities capabilities;

    public PhantomJSDriverBinaryHandler(DesiredCapabilities capabilities){
        this.capabilities = capabilities;
    }

    public String checkAndSetBinary(boolean performExecutableValidations) {
        String executablePath = (String) capabilities.getCapability(PHANTOMJS_EXECUTABLE_PATH_PROPERTY);

        if (Validate.empty(executablePath)) {
            executablePath = PropertySecurityAction.getProperty(PHANTOMJS_EXECUTABLE_PATH_PROPERTY);
        }

        // TODO: fixme - rewrite to use download feature implemented in drone
        if (Validate.empty(executablePath)) {
            capabilities.setCapability(PHANTOMJS_EXECUTABLE_PATH_PROPERTY, new File("target/drone-phantomjs").getAbsolutePath());
        }

        return executablePath;
    }

    public File downloadAndPrepare() throws Exception {
        String executablePath = checkAndSetBinary(true);
        return executablePath == null ? null : new File(executablePath);
    }

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
        return null;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return null;
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
