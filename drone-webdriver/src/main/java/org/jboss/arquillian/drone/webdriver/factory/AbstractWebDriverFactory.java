package org.jboss.arquillian.drone.webdriver.factory;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilitiesRegistry;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.WebDriver;

abstract class AbstractWebDriverFactory<T extends WebDriver> implements Configurator<T, WebDriverConfiguration> {

    private static final Logger log = Logger.getLogger(AbstractWebDriverFactory.class.getName());
    @Inject
    protected Instance<BrowserCapabilitiesRegistry> registryInstance;

    protected abstract String getDriverReadableName();

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, DronePoint<T>
        dronePoint) {

        BrowserCapabilitiesRegistry registry = registryInstance.get();

        // first, try to create a BrowserCapabilities object based on Field/Parameter type of @Drone annotated field
        BrowserCapabilities browser = registry.getEntryFor(getDriverReadableName());
        WebDriverConfiguration configuration = new WebDriverConfiguration(browser).configure(descriptor,
            dronePoint.getQualifier());

        // if not set, we hit a webdriver configuration and we want to use browser capabilities
        if (browser == null && Validate.nonEmpty(configuration.getBrowserName())) {
            browser = registry.getEntryFor(configuration.getBrowserName().toLowerCase());
            if (browser == null) {
                throw new IllegalStateException(
                    MessageFormat
                        .format("Unable to initialize WebDriver instance. Please specify a valid browser " +
                                "instead of {1}. Available options are: {0}",
                            getAvailableBrowserCapabilities(), configuration.getBrowserName()));
            }
            configuration.setBrowserInternal(browser);
        }

        // if it is still null, go with defaults
        if (browser == null) {
            browser = registry.getEntryFor(WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
            log.log(Level.INFO, "Property \"browser\" was not specified, using default value of {0}",
                WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
            configuration.setBrowserInternal(browser);
        }

        return configuration;
    }

    private String getAvailableBrowserCapabilities() {

        BrowserCapabilitiesRegistry registry = registryInstance.get();

        StringBuilder sb = new StringBuilder();
        for (BrowserCapabilities browser : registry.getAllBrowserCapabilities()) {
            if (Validate.nonEmpty(browser.getReadableName())) {
                sb.append(browser.getReadableName()).append(", ");
            }
        }
        // trim
        if (sb.lastIndexOf(", ") != -1) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }
}
