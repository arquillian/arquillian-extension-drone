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
 * Defines a contract for instantiating a DroneDriver
 *
 * @param <T>
 *     Type of drone instances
 * @param <C>
 *     Type of drone configurations
 */
public interface Instantiator<T, C extends DroneConfiguration<C>> extends Sortable {
    /**
     * Creates an instance of Drone Driver.
     * <p>
     * The instance is created before execution of the first method of the test class automatically by calling this
     * method. The
     * object is then bound to the Arquillian context, where it stays until the execution of the last test method is
     * finished.
     *
     * @param configuration
     *     the configuration object for the extension
     *
     * @return Newly created instance of the driver
     */
    T createInstance(C configuration);
}
