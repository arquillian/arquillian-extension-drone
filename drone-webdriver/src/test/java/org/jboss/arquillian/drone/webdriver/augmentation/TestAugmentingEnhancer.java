/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.drone.webdriver.augmentation;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
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
        RemoteWebDriver driver = mock(RemoteWebDriver.class);
        ReusableRemoteWebDriver reusableDriver = mock(ReusableRemoteWebDriver.class);
        InstanceOrCallableInstance instance = mock(InstanceOrCallableInstance.class);

        doReturn(driver).when(instance).asInstance(RemoteWebDriver.class);
        doReturn(driver).when(instance).asInstance(WebDriver.class);
        doReturn(driver).when(instance).asInstance(driver.getClass());
        doReturn(reusableDriver).when(instance).asInstance(ReusableRemoteWebDriver.class);

        System.out.println(driver.getClass());

        assertTrue(enhancer.canEnhance(instance, RemoteWebDriver.class, Default.class));
        assertTrue(enhancer.canEnhance(instance, ReusableRemoteWebDriver.class, Default.class));

        assertFalse(enhancer.canEnhance(instance, WebDriver.class, Default.class));
        // extensions of supported classes can't be automatically augmented
        assertFalse(enhancer.canEnhance(instance, driver.getClass(), Default.class));
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
