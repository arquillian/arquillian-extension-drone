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

import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.webdriver.spi.DroneAugmented;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("Revise this test as it's testing deprecated components")
public class TestAugmentingEnhancer {

    private AugmentingEnhancer enhancer = new AugmentingEnhancer();

    @Test
    public void testEnhancing() {
        // given
        MutableCapabilities capabilities = new MutableCapabilities();
        RemoteWebDriver driver = mock(RemoteWebDriver.class);
        when(driver.getCapabilities()).thenReturn(capabilities);
        capabilities.setCapability(AugmentingEnhancer.DRONE_AUGMENTED, driver);
//        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, Boolean.TRUE);

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
        MutableCapabilities capabilities = new MutableCapabilities();
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
