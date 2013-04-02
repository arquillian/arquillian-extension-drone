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
import java.lang.reflect.Method;

import org.jboss.arquillian.drone.spi.Enhancer;
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
import org.jboss.arquillian.drone.webdriver.spi.DroneAugmented;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.AugmenterProvider;
import org.openqa.selenium.remote.ExecuteMethod;
import org.openqa.selenium.remote.InterfaceImplementation;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Augments {@link RemoteWebDriver} instances so that it implements all interfaces determined by provided {@link Capabilities}.
 *
 * @author Lukas Fryc
 */
public class AugmentingEnhancer implements Enhancer<RemoteWebDriver> {

    public static final String DRONE_AUGMENTED = "droneAugmented";

    /**
     * Augmenter instance which is able to handle invocations of {@link DroneAugmented} interface
     */
    private final Augmenter augmenter = new Augmenter() {
        {
            addDriverAugmentation(DRONE_AUGMENTED, new AugmenterProvider() {

                @Override
                public Class<?> getDescribedInterface() {
                    return DroneAugmented.class;
                }

                @Override
                public InterfaceImplementation getImplementation(Object value) {
                    return new DroneAugmentedImpl((RemoteWebDriver) value);
                }
            });
        }
    };

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public boolean canEnhance(Class<?> type, Class<? extends Annotation> qualifier) {
        return RemoteWebDriver.class == type || ReusableRemoteWebDriver.class == type;
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
            return (RemoteWebDriver) ((DroneAugmented) enhancedInstance).getWrapped();
        }
        return enhancedInstance;
    }

    /**
     * Implements {@link DroneAugmented} interfaces which allows unwrapping of an augmented instance
     */
    private class DroneAugmentedImpl implements InterfaceImplementation {

        private RemoteWebDriver original;

        public DroneAugmentedImpl(RemoteWebDriver original) {
            this.original = original;
        }

        @Override
        public Object invoke(ExecuteMethod executeMethod, Object self, Method method, Object... args) {
            return original;
        }
    }
}
