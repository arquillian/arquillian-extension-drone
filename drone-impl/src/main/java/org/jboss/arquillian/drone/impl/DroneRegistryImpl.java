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
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.spi.Sortable;

/**
 * Default implementation of {@link DroneRegistry}
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class DroneRegistryImpl implements DroneRegistry {

    private final Map<Class<?>, RegistryValue> registry;

    DroneRegistryImpl() {
        registry = new HashMap<>();
    }

    @Override
    public DroneRegistry registerConfiguratorFor(Class<?> key, Configurator<?, ?> configurator) {
        RegistryValue entry = registry.get(key);
        if (entry != null) {
            entry.configurator = configurator;
        } else {
            registry.put(key, new RegistryValue().setConfigurator(configurator));
        }
        return this;
    }

    @Override
    public DroneRegistry registerInstantiatorFor(Class<?> key, Instantiator<?, ?> value) {
        RegistryValue entry = registry.get(key);
        if (entry != null) {
            entry.instantiator = value;
        } else {
            registry.put(key, new RegistryValue().setInstantiator(value));
        }
        return this;
    }

    @Override
    public DroneRegistry registerDestructorFor(Class<?> key, Destructor<?> value) {
        RegistryValue entry = registry.get(key);
        if (entry != null) {
            entry.destructor = value;
        } else {
            registry.put(key, new RegistryValue().setDestructor(value));
        }
        return this;
    }

    @Override
    public <T extends Sortable> T getEntryFor(Class<?> key, Class<T> entryType) throws IllegalStateException {
        RegisteredType regType = RegisteredType.getType(entryType);
        RegistryValue value = registry.get(key);

        if (value == null) {
            throw new IllegalStateException(getUnregisteredExceptionMessage(key, regType));
        }

        if (!regType.registeredIn(value)) {
            throw new IllegalStateException(getUnregisteredExceptionMessage(key, regType));
        }

        return regType.unwrap(value, entryType);
    }

    /**
     * Constructs pretty nice exception message when something was not registered
     *
     * @param registry
     *     The registry to be checked
     * @param unregistered
     *     The class which wasn't registered
     * @param registeredType
     *     Type of the builder which was not registered
     *
     * @return the exception message
     */
    String getUnregisteredExceptionMessage(Class<?> unregistered, RegisteredType registeredType) {
        StringBuilder sb = new StringBuilder();
        sb.append("No "
            + registeredType
            + " was found for object of type "
            + unregistered.getName()
            + ".\n"
            + "Make sure you have Drone extension depchain for the given browser on the classpath, that is org.jboss.arquillian.extension:arquillian-drone-webdriver-depchain:pom for WebDriver browsers, org.jboss.arquillian.extension:arquillian-drone-selenium-depchain:pom for Selenium 1 browsers and org.jboss.arquillian.graphene:graphene-webdriver:pom for Graphene2 browsers. If you are using different browser extension, please make sure it is on classpath.\n");

        sb.append("Currently registered " + registeredType + "s are: ");

        for (Map.Entry<Class<?>, RegistryValue> entry : registry.entrySet()) {
            if (registeredType.registeredIn(entry.getValue())) {
                sb.append(entry.getKey().getName()).append("\n");
            }
        }

        return sb.toString();
    }

    private enum RegisteredType {
        CONFIGURATOR {
            @Override
            public boolean registeredIn(RegistryValue value) {
                return value.configurator != null;
            }

            @Override
            public String toString() {
                return "configurator";
            }

            @Override
            public <T extends Sortable> T unwrap(RegistryValue value, Class<T> unwrapClass) {
                if (registeredIn(value)) {
                    return unwrapClass.cast(value.configurator);
                }
                return null;
            }
        },
        INSTANTIATOR {
            @Override
            public boolean registeredIn(RegistryValue value) {
                return value.instantiator != null;
            }

            @Override
            public String toString() {
                return "instantiator";
            }

            @Override
            public <T extends Sortable> T unwrap(RegistryValue value, Class<T> unwrapClass) {
                if (registeredIn(value)) {
                    return unwrapClass.cast(value.instantiator);
                }
                return null;
            }
        },
        DESTRUCTOR {
            @Override
            public boolean registeredIn(RegistryValue value) {
                return value.destructor != null;
            }

            @Override
            public String toString() {
                return "destructor";
            }

            @Override
            public <T extends Sortable> T unwrap(RegistryValue value, Class<T> unwrapClass) {
                if (registeredIn(value)) {
                    return unwrapClass.cast(value.destructor);
                }
                return null;
            }
        };

        public static RegisteredType getType(Class<? extends Sortable> registeredType) {
            Validate.stateNotNull(registeredType, "Registered type must not be null");
            if (Configurator.class.isAssignableFrom(registeredType)) {
                return CONFIGURATOR;
            } else if (Instantiator.class.isAssignableFrom(registeredType)) {
                return INSTANTIATOR;
            } else if (Destructor.class.isAssignableFrom(registeredType)) {
                return DESTRUCTOR;
            }

            throw new AssertionError("Unable to determine registered Type from " + registeredType.getName());
        }

        public abstract boolean registeredIn(RegistryValue value);

        public abstract <T extends Sortable> T unwrap(RegistryValue value, Class<T> unwrapClass);
    }

    private static class RegistryValue {
        Configurator<?, ?> configurator;
        Instantiator<?, ?> instantiator;
        Destructor<?> destructor;

        /**
         * @param configurator
         *     the configurator to set
         *
         * @return modified instance
         */
        public RegistryValue setConfigurator(Configurator<?, ?> configurator) {
            this.configurator = configurator;
            return this;
        }

        /**
         * @param instantiator
         *     the instantiator to set
         *
         * @return modified instance
         */
        public RegistryValue setInstantiator(Instantiator<?, ?> instantiator) {
            this.instantiator = instantiator;
            return this;
        }

        /**
         * @param destructor
         *     the destructor to set
         *
         * @return modified value
         */
        public RegistryValue setDestructor(Destructor<?> destructor) {
            this.destructor = destructor;
            return this;
        }
    }
}
