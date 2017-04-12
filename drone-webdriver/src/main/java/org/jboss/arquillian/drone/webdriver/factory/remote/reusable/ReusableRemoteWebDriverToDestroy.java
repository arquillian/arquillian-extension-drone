package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 */
public class ReusableRemoteWebDriverToDestroy {

    private Logger log = Logger.getLogger(ReusableRemoteWebDriverToDestroy.class.getName());

    private boolean destroyed = false;

    private RemoteWebDriver remoteWebDriver;

    public ReusableRemoteWebDriverToDestroy(RemoteWebDriver remoteWebDriver) {
        this.remoteWebDriver = remoteWebDriver;
    }

    public void setRemoteWebDriver(RemoteWebDriver remoteWebDriver) {
        this.remoteWebDriver = remoteWebDriver;
    }

    public void destroy() {
        if (!destroyed) {
            try {
                remoteWebDriver.quit();
            } catch (WebDriverException e) {
                log.log(Level.WARNING, "@Drone {0} has been already destroyed and can't be destroyed again.",
                    remoteWebDriver.getClass().getSimpleName());
            }
        }
    }
}
