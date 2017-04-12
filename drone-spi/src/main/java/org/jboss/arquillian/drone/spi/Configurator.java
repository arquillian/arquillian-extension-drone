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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;

/**
 * Defines a contract for creating configurations for Drone Driver
 *
 * @param <T>
 *     Type of the driver
 * @param <C>
 *     Type of the configuration
 *
 * @see org.jboss.arquillian.drone.spi.Qualifier
 */
public interface Configurator<T, C extends DroneConfiguration<C>> extends Sortable {
    /**
     * Creates a configuration for given Drone Driver. The instance is created before the first method of the test
     * suite is run
     * and stays in Arquillian's context until the test suite execution is finished.
     *
     * @param descriptor
     *     A descriptor to be parsed
     * @param qualifier
     *     A qualifier for this configuration
     *
     * @return Configuration holding values determined from Arquillian's descriptor
     */
    C createConfiguration(ArquillianDescriptor descriptor, DronePoint<T> dronePoint);
}
