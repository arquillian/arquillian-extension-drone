package org.jboss.arquillian.drone.webdriver.factory;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.binary.handler.EdgeDriverBinaryHandler;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;

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

        EdgeDriverService service = new EdgeDriverService.Builder()
                .withLogOutput(System.out).build();
        return SecurityActions.newInstance(configuration.getImplementationClass(),
                new Class<?>[]{EdgeDriverService.class, EdgeOptions.class},
                new Object[]{service, edgeOptions}, EdgeDriver.class);
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

    public EdgeOptions getEdgeOptions(WebDriverConfiguration configuration) {
        Capabilities capabilities = configuration.getCapabilities();
        EdgeOptions edgeOptions = new EdgeOptions();
        CapabilitiesOptionsMapper.mapCapabilities(edgeOptions, capabilities, BROWSER_CAPABILITIES);

        new EdgeDriverBinaryHandler(edgeOptions).checkAndSetBinary(true);

        return edgeOptions;
    }
}
