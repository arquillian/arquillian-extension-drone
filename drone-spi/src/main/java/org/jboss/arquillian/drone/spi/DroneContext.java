/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.util.List;

/**
 * Context that stores drone configurations and cached drones.
 * <p/>
 * Context allows to store both class scoped Drones and method scoped Drones.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public interface DroneContext {

    /**
     * Returns drone extension configuration. This method is deprecated and will soon be replaced.
     */
    // TODO to be removed in Alpha 2
    @Deprecated
    <C extends DroneConfiguration<C>> C getGlobalDroneConfiguration(Class<C> configurationClass);

    /**
     * Sets drone extension configuration. This method is deprecated and will soon be replaced.
     */
    // TODO to be removed in Alpha 2
    @Deprecated
    void setGlobalDroneConfiguration(DroneConfiguration<?> configuration);

    /**
     * Returns an instance of drone. If the drone was not yet instantiated, it will fire {@link BeforeDroneInstantiated}
     * event, then instantiate the drone and fire {@link AfterDroneInstantiated} event.
     *
     * @param <T> type of the drone
     * @throws IllegalStateException
     */
    <T> T getDrone(InjectionPoint<T> injectionPoint) throws IllegalStateException;

    /**
     * Returns an instance of {@link DroneConfiguration} stored for specified injection point.
     *
     * @param configurationClass class to define the type of configuration to be returned
     * @param <C>                type of configuration to be returned
     * @throws IllegalArgumentException if there's no configuration stored for specified injection point
     */
    <C extends DroneConfiguration<C>> C getDroneConfiguration(InjectionPoint<?> injectionPoint,
                                                              Class<C> configurationClass) throws
            IllegalArgumentException;

    /**
     * Stores the {@link CachingCallable} for future drone instantiation.
     * <p/>
     * It throws {@link java.lang.IllegalStateException} if there is no configuration stored for specified injection
     * point.
     *
     * @throws java.lang.IllegalStateException
     */
    <T> void storeFutureDrone(InjectionPoint<T> injectionPoint, CachingCallable<T> drone) throws IllegalStateException;

    /**
     * Stores the {@link DroneConfiguration} for specified injection point.
     * <p/>
     * It throws {@link java.lang.IllegalStateException} if there is already a configuration stored for specified
     * injection point.
     *
     * @throws java.lang.IllegalStateException
     */
    <T, C extends DroneConfiguration<C>> void storeDroneConfiguration(InjectionPoint<T> injectionPoint,
                                                                      C configuration) throws IllegalStateException;

    /**
     * Returns true if {@link CachingCallable#isValueCached()} is true for specified injection point.
     */
    <T> boolean isDroneInstantiated(InjectionPoint<T> injectionPoint);

    /**
     * Returns true if {@link CachingCallable} is stored for specified injection point.
     */
    <T> boolean isFutureDroneStored(InjectionPoint<T> injectionPoint);

    /**
     * Returns true if {@link DroneConfiguration} for specified injection point is stored.
     */
    <T> boolean isDroneConfigurationStored(InjectionPoint<T> injectionPoint);

    /**
     * Removes future or instantiated drone, depending on the state, for specified injection point.
     */
    void removeDrone(InjectionPoint<?> injectionPoint);

    /**
     * Removes configuration for specified injection point.
     */
    void removeDroneConfiguration(InjectionPoint<?> injectionPoint);

    /**
     * Removes both future and instantiated drones for specified injection points.
     */
    void removeDrones(List<InjectionPoint<?>> injectionPoints);

    /**
     * Removes configurations for specified injection points.
     */
    void removeDroneConfigurations(List<InjectionPoint<?>> injectionPoints);

    /**
     * Returns a single injection point that get matched by all of specified filters and the backing drone type can
     * be cast to specified type.
     *
     * @throws IllegalStateException if matched injection points count is not exactly one
     */
    <T> InjectionPoint<? extends T> findSingle(Class<T> droneClass, Filter... filters) throws IllegalStateException;

    /**
     * Returns a list of injection points that get matched by all of specified filters and the backing drone type can
     * be cast to specified type. When no filters are passed in, it returns all injection points with drone type that
     * can be cast to specified type. Using the Object.class as the type, it returns all injection points stored.
     */
    <T> List<InjectionPoint<? extends T>> find(Class<T> droneClass, Filter... filters);
}

