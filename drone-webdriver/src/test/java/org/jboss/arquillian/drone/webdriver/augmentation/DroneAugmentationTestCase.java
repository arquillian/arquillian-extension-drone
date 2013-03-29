package org.jboss.arquillian.drone.webdriver.augmentation;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.webdriver.spi.DroneAugmented;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.remote.RemoteWebDriver;

import qualifier.Port4444;

@RunWith(Arquillian.class)
public class DroneAugmentationTestCase {

    @Drone @Port4444
    private RemoteWebDriver driver;

    @Test
    public void test_that_driver_is_augmented_properly() {
        assertThat(driver, instanceOf(DroneAugmented.class));
        assertThat(((DroneAugmented) driver).getWrapped(), instanceOf(RemoteWebDriver.class));
    }
}
