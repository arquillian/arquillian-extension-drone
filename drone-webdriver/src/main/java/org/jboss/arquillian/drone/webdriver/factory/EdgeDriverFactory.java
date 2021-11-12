package org.jboss.arquillian.drone.webdriver.factory;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.binary.handler.EdgeDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * EdgeDriver.
 */
public class EdgeDriverFactory extends AbstractWebDriverFactory<EdgeDriver> implements
    Configurator<EdgeDriver, WebDriverConfiguration>, Instantiator<EdgeDriver, WebDriverConfiguration>,
    Destructor<EdgeDriver> {

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.Edge().getReadableName();

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Sortable#getPrecedence()
     */
    @Override
    public int getPrecedence() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Destructor#destroyInstance(java.lang.Object)
     */
    @Override
    public void destroyInstance(EdgeDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public EdgeDriver createInstance(WebDriverConfiguration configuration) {
        EdgeOptions edgeOptions = getEdgeOptions(configuration);

        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[]{EdgeOptions.class},
            new Object[]{edgeOptions}, EdgeDriver.class);
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

    public EdgeOptions getEdgeOptions(WebDriverConfiguration configuration) {
        return new EdgeOptions().merge(getCapabilities(configuration));
    }

    @Deprecated
    public Capabilities getCapabilities(WebDriverConfiguration configuration, boolean performValidations) {
        return getCapabilities(configuration);
    }

    public Capabilities getCapabilities(WebDriverConfiguration configuration) {
        DesiredCapabilities capabilities = new DesiredCapabilities(configuration.getCapabilities());

        capabilities.setPlatform(BrowserCapabilitiesList.Capabilities.EDGE.getPlatformName());
        capabilities.setBrowserName(BrowserCapabilitiesList.Capabilities.EDGE.getBrowserName());

        new EdgeDriverBinaryHandler(capabilities).checkAndSetBinary(true);

        return capabilities;
    }
}
