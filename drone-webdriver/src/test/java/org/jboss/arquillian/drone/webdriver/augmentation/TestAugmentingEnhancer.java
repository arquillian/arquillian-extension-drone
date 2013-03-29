package org.jboss.arquillian.drone.webdriver.augmentation;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.webdriver.spi.DroneAugmented;
import org.junit.Test;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class TestAugmentingEnhancer {

    private AugmentingEnhancer enhancer = new AugmentingEnhancer();

    @Test
    public void testCanEnhance() {
        assertTrue(enhancer.canEnhance(RemoteWebDriver.class, Default.class));
        assertTrue(enhancer.canEnhance(mock(RemoteWebDriver.class).getClass(), Default.class));
        assertFalse(enhancer.canEnhance(WebDriver.class, Default.class));
    }

    @Test
    public void testEnhancing() {
        // given
        DesiredCapabilities capabilities = new DesiredCapabilities();
        RemoteWebDriver driver = mock(RemoteWebDriver.class);
        when(driver.getCapabilities()).thenReturn(capabilities);
        capabilities.setCapability(AugmentingEnhancer.DRONE_AUGMENTED, driver);
        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, Boolean.TRUE);

        // when
        RemoteWebDriver enhanced = enhancer.enhance(driver, Default.class);

        // then
        assertThat(enhanced, instanceOf(DroneAugmented.class));
        assertThat(enhanced, instanceOf(TakesScreenshot.class));
        assertEquals(driver, ((DroneAugmented) enhanced).getWrapped());
    }

    @Test
    public void testDeenhancing() {
        // given
        DesiredCapabilities capabilities = new DesiredCapabilities();
        RemoteWebDriver driver = mock(RemoteWebDriver.class);
        when(driver.getCapabilities()).thenReturn(capabilities);
        capabilities.setCapability(AugmentingEnhancer.DRONE_AUGMENTED, driver);

        // when
        RemoteWebDriver enhanced = enhancer.enhance(driver, Default.class);
        assertThat(enhanced, instanceOf(DroneAugmented.class));
        RemoteWebDriver deenhanced = enhancer.deenhance(enhanced, Default.class);

        // then
        assertEquals(driver, deenhanced);
    }
}
