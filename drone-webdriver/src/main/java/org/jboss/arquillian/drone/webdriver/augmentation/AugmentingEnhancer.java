/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import java.lang.annotation.Annotation;
import java.util.logging.Logger;
import org.jboss.arquillian.drone.spi.DroneInstanceEnhancer;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
import org.jboss.arquillian.drone.webdriver.spi.DroneAugmented;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Augments {@link RemoteWebDriver} instances so that it implements all interfaces determined by provided {@link
 * Capabilities}.
 *
 * @author Lukas Fryc
 */
public class AugmentingEnhancer implements DroneInstanceEnhancer<RemoteWebDriver> {
    public static final String DRONE_AUGMENTED = "droneAugmented";
    private static final Logger logger = Logger.getLogger(AugmentingEnhancer.class.getName());
    /**
     * Augmenter instance which is able to handle invocations of {@link DroneAugmented} interface
     */
    private static final Augmenter augmenter = new Augmenter().addDriverAugmentation(DRONE_AUGMENTED, DroneAugmented.class,
        (c, exe) -> () -> c.getCapability(AugmentingEnhancer.DRONE_AUGMENTED));

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public boolean canEnhance(InstanceOrCallableInstance instance, Class<?> droneType,
        Class<? extends Annotation> qualifier) {

        if (RemoteWebDriver.class == droneType || ReusableRemoteWebDriver.class == droneType) {
            return checkEnhance(instance, true);
        }

        Class<?> realInstanceClass = instance.asInstance(droneType).getClass();

        return checkEnhance(instance, RemoteWebDriver.class == realInstanceClass
            || ReusableRemoteWebDriver.class == realInstanceClass
            || DroneAugmented.class.isAssignableFrom(realInstanceClass));
    }

    private boolean checkEnhance(InstanceOrCallableInstance instance, boolean status) {
        try {
            RemoteWebDriver driver = instance.asInstance(RemoteWebDriver.class);
            if (driver.getCapabilities().getCapability(DRONE_AUGMENTED) != driver) {
                return false;
            } else {
                return status;
            }
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Augments the instance
     */
    @Override
    public RemoteWebDriver enhance(RemoteWebDriver instance, Class<? extends Annotation> qualifier) {
        return (RemoteWebDriver) augmenter.augment(instance);
    }

    /**
     * Unwraps the instance
     */
    @Override
    public RemoteWebDriver deenhance(RemoteWebDriver enhancedInstance, Class<? extends Annotation> qualifier) {

        if (enhancedInstance instanceof DroneAugmented) {

            RemoteWebDriver original = (RemoteWebDriver) ((DroneAugmented) enhancedInstance).getWrapped();

            Capabilities capabilities = enhancedInstance.getCapabilities();

            if (capabilities != null) {
                ((MutableCapabilities) enhancedInstance.getCapabilities()).setCapability(DRONE_AUGMENTED, Boolean.FALSE);
            }

            return original;
        }
        return enhancedInstance;
    }
}
