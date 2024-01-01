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
 * Context unique for each {@link DronePoint}. It stores configuration, future instance and metadata.
 */
public interface DronePointContext<DRONE> {

    /**
     * Returns an instance of {@link DronePoint}, which is this context bound to.
     */
    DronePoint<DRONE> getDronePoint();

    /**
     * Returns an instance of drone. If the drone was not yet instantiated, it will fire {@link BeforeDroneInstantiated}
     * event, then instantiate the drone and fire {@link AfterDroneInstantiated} event.
     *
     * @throws java.lang.IllegalStateException
     *     if there is no future instance set.
     */
    DRONE getInstance() throws IllegalStateException;

    /**
     * Returns an instance of drone, cast to desired type. If the drone was not yet instantiated, it will fire
     * {@link BeforeDroneInstantiated} event, then instantiate the drone and fire {@link AfterDroneInstantiated} event.
     *
     * @param droneClass
     *     Class to cast drone instance to.
     *
     * @throws java.lang.IllegalArgumentException
     *     If the given class is null.
     * @throws java.lang.IllegalStateException
     *     If there is no future instance set or if the instance cannot be cast
     *     to desired type. Use {@link DronePoint#conformsTo(Class)} to make sure
     *     the drone can be cast to it.
     */
    <CAST_DRONE> CAST_DRONE getInstanceAs(Class<CAST_DRONE> droneClass) throws IllegalArgumentException,
        IllegalStateException;

    /**
     * Returns an instance of {@link DroneConfiguration} cast to desired type.
     *
     * @throws java.lang.IllegalStateException
     *     If there is no configuration set or the configuration cannot be cast
     *     to the desired type.
     * @throws java.lang.IllegalArgumentException
     *     If the given configuration class is null.
     */
    <CONF extends DroneConfiguration<CONF>> CONF getConfigurationAs(Class<CONF> configurationClass) throws
        IllegalArgumentException, IllegalStateException;

    /**
     * Returns saved metadata for the given key.
     *
     * @throws java.lang.IllegalArgumentException
     *     If the given key class is null.
     */
    <KEY extends MetadataKey<VALUE>, VALUE> VALUE getMetadata(Class<KEY> keyClass) throws IllegalArgumentException;

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
     * Returns true if there are metadata set for the given key.
     *
     * @throws java.lang.IllegalArgumentException
     *     If the given key class is null.
     */
    <KEY extends MetadataKey<VALUE>, VALUE> boolean hasMetadata(Class<KEY> keyClass) throws IllegalArgumentException;

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
     * Sets given metadata under specified key. If there are metadata set for the given key, it will replace them.
     *
     * @throws java.lang.IllegalArgumentException
     *     If the given key class is null.
     */
    <KEY extends MetadataKey<VALUE>, VALUE> void setMetadata(Class<KEY> keyClass,
        VALUE metadata) throws IllegalArgumentException;

    /**
     * Removes future or instantiated drone instance, depending on the state.
     */
    void removeFutureInstance();

    /**
     * Removes configuration.
     */
    void removeConfiguration();

    /**
     * Removes metadata set for the given key.
     *
     * @throws java.lang.IllegalArgumentException
     *     If the given key class is null.
     */
    <KEY extends MetadataKey<VALUE>, VALUE> void removeMetadata(Class<KEY> keyClass);

    /**
     * Utility interface used for unique identification of metadata in {@link DronePointContext}.
     * <p/>
     * Whenever you need
     * to store data into the context, you need to extend this interface and set the {@code VALUE} parameter to a
     * type of the metadata you want to store. Then use {@code .class} of your newly created interface as a key to
     * store and retrieve data from the context.
     *
     * @param <VALUE>
     *     Type of the value to be stored in the {@link DronePointContext}.
     */
    interface MetadataKey<VALUE> {
    }
}
