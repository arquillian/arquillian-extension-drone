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

import java.lang.reflect.Field;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
import org.jboss.arquillian.drone.webdriver.spi.DroneAugmented;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("Revise this test as it's testing deprecated components")
public class TestAugmentingEnhancer {

    private AugmentingEnhancer enhancer = new AugmentingEnhancer();

    @Test
    public void testCanEnhance() throws Exception {

        Unsafe unsafe = new Unsafe();
        RemoteWebDriver remoteDriver = unsafe.createInstanceWithoutInvokingConstructor(RemoteWebDriver.class);
        RemoteWebDriver reusableRemoteDriver = mock(ReusableRemoteWebDriver.class);
        DroneAugmented augmentedDriver = mock(DroneAugmented.class);
        InstanceOrCallableInstance instance1 = mock(InstanceOrCallableInstance.class);
        InstanceOrCallableInstance instance2 = mock(InstanceOrCallableInstance.class);
        InstanceOrCallableInstance instance3 = mock(InstanceOrCallableInstance.class);
        InstanceOrCallableInstance instance4 = mock(InstanceOrCallableInstance.class);

        doReturn(remoteDriver).when(instance1).asInstance(RemoteWebDriver.class);
        doReturn(remoteDriver).when(instance1).asInstance(WebDriver.class);
        doReturn(augmentedDriver).when(instance1).asInstance(ReusableRemoteWebDriver.class);

        doReturn(unsafe.createInstanceWithoutInvokingConstructor(FirefoxDriver.class)).when(instance2)
            .asInstance(WebDriver.class);
        doReturn(reusableRemoteDriver).when(instance3).asInstance(WebDriver.class);
        doReturn(augmentedDriver).when(instance4).asInstance(WebDriver.class);

        assertTrue("AugmentingEnhancer should enhance when droneType == RemoteWebDriver.class",
            enhancer.canEnhance(instance1, RemoteWebDriver.class, Default.class));
        assertTrue("AugmentingEnhancer should enhance when droneType == ReusableRemoteWebDriver.class",
            enhancer.canEnhance(instance1, ReusableRemoteWebDriver.class, Default.class));
        assertTrue("AugmentingEnhancer should enhance when real instance is RemoteWebDriver",
            enhancer.canEnhance(instance1, WebDriver.class, Default.class));
        assertTrue("AugmentingEnhancer should enhance when real instance already augmented!",
            enhancer.canEnhance(instance4, WebDriver.class, Default.class));

        assertFalse(
            "AugmentingEnhancer should not enhance when real instance is not RemoteWebDriver || ReusableRemoteWebDriver",
            enhancer.canEnhance(instance2, WebDriver.class, Default.class));
        assertFalse("AugmentingEnhancer should not enhance extensions of supported classes!",
            enhancer.canEnhance(instance3, WebDriver.class, Default.class));
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

    /**
     * We need to instantiate a class without calling a constructor.
     * <p>
     * This is needed because we don't want to pollute test environment with webdrivers not related to the browser tested
     * and by
     * default
     * RemoteWebDriver spawns Firefox driver. Yet, we can't mock it as Augementer asks for an exact class match *
     */
    @SuppressWarnings("restriction")
    private static class Unsafe {

        private sun.misc.Unsafe unsafe;

        Unsafe() {
            try {
                Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); // Internal reference
                f.setAccessible(true);
                this.unsafe = (sun.misc.Unsafe) f.get(null);
            } catch (SecurityException e) {
                throw new AssertionError("Unable to get sun.misc.Unsafe object for testing, " + e.getMessage());
            } catch (NoSuchFieldException e) {
                throw new AssertionError("Unable to get sun.misc.Unsafe object for testing, " + e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new AssertionError("Unable to get sun.misc.Unsafe object for testing, " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new AssertionError("Unable to get sun.misc.Unsafe object for testing, " + e.getMessage());
            }
        }

        <T> T createInstanceWithoutInvokingConstructor(Class<T> clazz) throws InstantiationException {
            return clazz.cast(unsafe.allocateInstance(clazz));
        }
    }
}
