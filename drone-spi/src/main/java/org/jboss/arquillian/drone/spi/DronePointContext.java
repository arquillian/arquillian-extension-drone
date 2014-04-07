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

public interface DronePointContext<DRONE> {

    /**
     * Returns an instance of drone. If the drone was not yet instantiated, it will fire {@link BeforeDroneInstantiated}
     * event, then instantiate the drone and fire {@link AfterDroneInstantiated} event.
     *
     * @throws java.lang.IllegalStateException if there is no future instance set.
     */
    DRONE getInstance() throws IllegalStateException;

    /**
     * Returns an instance of drone, cast to desired type. If the drone was not yet instantiated, it will fire
     * {@link BeforeDroneInstantiated} event, then instantiate the drone and fire {@link AfterDroneInstantiated} event.
     *
     * @param droneClass Class to cast drone instance to.
     * @throws java.lang.ClassCastException    If drone cannot be cast to desired type. Use
     *                                         {@link org.jboss.arquillian.drone.spi.DronePoint#conformsTo(Class)} to
     *                                         make sure the drone can be cast to it.
     * @throws java.lang.IllegalStateException If there is no future instance set.
     */
    // FIXME is it ok to throw ClassCastException?
    <CAST_DRONE extends DRONE> CAST_DRONE getInstanceAs(Class<CAST_DRONE> droneClass) throws ClassCastException,
            IllegalStateException;

    /**
     * Returns an instance of {@link DroneConfiguration} cast to desired type.
     *
     * @throws java.lang.ClassCastException    If the configuration cannot be cast to desired type.
     * @throws java.lang.IllegalStateException If there is no configuration set.
     */
    <CONF extends DroneConfiguration<CONF>> CONF getConfigurationAs(Class<CONF> configurationClass) throws
            ClassCastException, IllegalStateException;

    /**
     * Returns true if {@link CachingCallable#isValueCached()} is true.
     */
    boolean isInstantiated();

    /**
     * Returns true if future instance ({@link CachingCallable}) is set.
     */
    boolean hasFutureInstance();

    /**
     * Returns true if configuration is set.
     */
    boolean hasConfiguration();

    /**
     * Sets {@link CachingCallable} for future drone instantiation. Remember that the best practise is to instantiate
     * drones lazily, at the very last moment.
     */
    void setFutureInstance(CachingCallable<DRONE> futureInstance);

    /**
     * Sets the {@link DroneConfiguration}.
     */
    <CONF extends DroneConfiguration<CONF>> void setConfiguration(CONF configuration);

    /**
     * Removes future or instantiated drone instance, depending on the state.
     */
    void removeFutureInstance();

    /**
     * Removes configuration.
     */
    void removeConfiguration();
}
