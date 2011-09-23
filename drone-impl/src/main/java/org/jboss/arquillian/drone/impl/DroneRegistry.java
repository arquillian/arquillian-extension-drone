/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.impl;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;

/**
 * Register of available {@link Configurator}s, {@link Instantiator}s and {@link Destructor}s discovered via SPI.
 *
 * Stores only one of them per type, so {@link DroneRegistrar} is responsible for selecting correct implementations.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneRegistry {

    public static enum RegisteredType {
        CONFIGURATOR {
            @Override
            public boolean registeredIn(RegistryValue value) {
                return value.configurator == null;
            }

            @Override
            public String toString() {
                return "configurator";
            }
        },
        INSTANTIATOR {
            @Override
            public boolean registeredIn(RegistryValue value) {
                return value.instantiator == null;
            }

            @Override
            public String toString() {
                return "instantiator";
            }
        },
        DESTRUCTOR {
            @Override
            public boolean registeredIn(RegistryValue value) {
                return value.destructor == null;
            }

            @Override
            public String toString() {
                return "destructor";
            }
        };

        public abstract boolean registeredIn(RegistryValue value);
    }

    private Map<Class<?>, RegistryValue> registry = new HashMap<Class<?>, RegistryValue>();

    /**
     * Gets configurator for given object type
     *
     * @param <T> Type of configurator object
     * @param type Configurator key
     * @return Configurator for objects of type <T>
     */
    @SuppressWarnings("unchecked")
    public <T> Configurator<T, ?> getConfiguratorFor(Class<T> type) {
        RegistryValue value = registry.get(type);
        if (value != null) {
            return (Configurator<T, ?>) value.configurator;
        }
        return null;
    }

    /**
     * Gets instantiator for given object type
     *
     * @param <T> Type of instantiator object
     * @param key Instantiator key
     * @return Instantiator for objects of type <T>
     */
    @SuppressWarnings("unchecked")
    public <T> Instantiator<T, ?> getInstantiatorFor(Class<T> key) {
        RegistryValue value = registry.get(key);
        if (value != null) {
            return (Instantiator<T, ?>) value.instantiator;
        }
        return null;
    }

    /**
     * Gets destructor for given object type
     *
     * @param <T> Type of destructor object
     * @param key Destructor key
     * @return Destructor for objects of type <T>
     */
    @SuppressWarnings("unchecked")
    public <T> Destructor<T> getDestructorFor(Class<T> key) {
        RegistryValue value = registry.get(key);
        if (value != null) {
            return (Destructor<T>) value.destructor;
        }
        return null;
    }

    /**
     * Registers a configurator for given object type
     *
     * @param key Type to be registered
     * @param configurator Configurator to be stored
     * @return Modified registry
     */
    public DroneRegistry registerConfiguratorFor(Class<?> key, Configurator<?, ?> configurator) {
        RegistryValue entry = registry.get(key);
        if (entry != null) {
            entry.configurator = configurator;
        } else {
            registry.put(key, new RegistryValue().setConfigurator(configurator));
        }
        return this;
    }

    /**
     * Registers a instantiator for given object type
     *
     * @param key Type to be registered
     * @param value Instantiator to be stored
     * @return Modified registry
     */
    public DroneRegistry registerInstantiatorFor(Class<?> key, Instantiator<?, ?> value) {
        RegistryValue entry = registry.get(key);
        if (entry != null) {
            entry.instantiator = value;
        } else {
            registry.put(key, new RegistryValue().setInstantiator(value));
        }
        return this;
    }

    /**
     * Registers a destructor for given object type
     *
     * @param key Type to be registered
     * @param value Destructor to be stored
     * @return Modified registry
     */
    public DroneRegistry registerDestructorFor(Class<?> key, Destructor<?> value) {
        RegistryValue entry = registry.get(key);
        if (entry != null) {
            entry.destructor = value;
        } else {
            registry.put(key, new RegistryValue().setDestructor(value));
        }
        return this;
    }

    /**
     * Constructs pretty nice exception message when something was not registered
     *
     * @param registry The registry to be checked
     * @param unregistered The class which wasn't registered
     * @param registeredType Type of the builder which was not registered
     * @return the exception message
     */
    static String getUnregisteredExceptionMessage(DroneRegistry registry, Class<?> unregistered, RegisteredType registeredType) {
        StringBuilder sb = new StringBuilder();
        sb.append("No "
                + registeredType
                + " was found for object of type "
                + unregistered.getName()
                + ".\n"
                + "Make sure you have a proper Drone extension on the classpath, that is arquillian-drone-selenium for Selenium browsers, arquillian-drone-webdriver for WebDriver browsers and arquillian-ajocado-drone for AjaxSelenium browsers. If you are using your own browser extension, please make sure it is on classpath.\n");

        sb.append("Currently registered " + registeredType + "s are: ");

        for (Map.Entry<Class<?>, RegistryValue> entry : registry.registry.entrySet()) {
            if (registeredType.registeredIn(entry.getValue())) {
                sb.append(entry.getKey().getName()).append("\n");
            }
        }

        return sb.toString();
    }

    private static class RegistryValue {
        Configurator<?, ?> configurator;
        Instantiator<?, ?> instantiator;
        Destructor<?> destructor;

        /**
         * @param configurator the configurator to set
         * @return modified instance
         */
        public RegistryValue setConfigurator(Configurator<?, ?> configurator) {
            this.configurator = configurator;
            return this;
        }

        /**
         * @param instantiator the instantiator to set
         * @return modified instance
         */
        public RegistryValue setInstantiator(Instantiator<?, ?> instantiator) {
            this.instantiator = instantiator;
            return this;
        }

        /**
         * @param destructor the destructor to set
         * @return modified value
         */
        public RegistryValue setDestructor(Destructor<?> destructor) {
            this.destructor = destructor;
            return this;
        }

    }
}
