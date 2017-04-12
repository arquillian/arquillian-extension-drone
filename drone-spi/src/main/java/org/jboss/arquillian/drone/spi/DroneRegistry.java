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
package org.jboss.arquillian.drone.spi;

/**
 * Register of available {@link Configurator}s, {@link Instantiator}s and {@link Destructor}s discovered via SPI.
 */
public interface DroneRegistry {

    /**
     * Checks if the registry has an entry for given key and type
     *
     * @param key
     *     the key
     * @param entryType
     *     the type of the entry, that is {@link Configurator}, {@link Instantiator} or {@link Destructor}
     * @param <E>
     *     the entry type
     *
     * @return The SPI object for given type
     *
     * @throws IllegalStateException
     *     in case that no given entry exists for key and entryType combination
     */
    <E extends Sortable> E getEntryFor(Class<?> key, Class<E> entryType) throws IllegalStateException;

    /**
     * Registers a configurator for given object type
     *
     * @param key
     *     Type to be registered
     * @param configurator
     *     {@link Configurator} to be stored
     *
     * @return Modified registry
     */
    DroneRegistry registerConfiguratorFor(Class<?> key, Configurator<?, ?> configurator);

    /**
     * Registers an instantiator for given object type
     *
     * @param key
     *     Type to be registered
     * @param value
     *     {@link Instantiator} to be stored
     *
     * @return Modified registry
     */
    DroneRegistry registerInstantiatorFor(Class<?> key, Instantiator<?, ?> value);

    /**
     * Registers a destructor for given object type
     *
     * @param key
     *     Type to be registered
     * @param value
     *     {@link Destructor} to be stored
     *
     * @return Modified registry
     */
    DroneRegistry registerDestructorFor(Class<?> key, Destructor<?> value);
}
