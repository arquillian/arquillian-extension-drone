package org.jboss.arquillian.drone.webdriver.register;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(Arquillian.class)
public class WebDriverFactoryRegisterTestCase {

    @Drone
    private WebDriver browser;

    @Test
    public void verifyDrone() {
        assertNotNull(browser);
    }
}
