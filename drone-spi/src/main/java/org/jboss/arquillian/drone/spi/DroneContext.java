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
 * Context for Drone instance, Drone callable instances and Drone configuration.
 * <p/>
 * Context allows to store both class scoped Drones and method scoped Drones.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public interface DroneContext {

    // TODO to be removed in Alpha 2
    @Deprecated
    <C extends DroneConfiguration<C>> C getGlobalDroneConfiguration(Class<C> configurationClass);

    // TODO to be removed in Alpha 2
    @Deprecated
    void setGlobalDroneConfiguration(DroneConfiguration<?> configuration);

    /**
     * Gets the instance of drone.
     *
     * @param injectionPoint
     * @param <T>
     * @return
     * @throws IllegalStateException
     */
    <T> T getDrone(InjectionPoint<T> injectionPoint) throws IllegalStateException;

    <C extends DroneConfiguration<C>> C getDroneConfiguration(InjectionPoint<?> injectionPoint,
                                                              Class<C> configurationClass) throws
            IllegalArgumentException;

    /**
     *
     * @param injectionPoint
     * @param drone
     * @param <T>
     * @throws
     */
    <T> void storeFutureDrone(InjectionPoint<T> injectionPoint, CachingCallable<T> drone);

    <T, C extends DroneConfiguration<C>> void storeDroneConfiguration(InjectionPoint<T> injectionPoint,
                                                                      C configuration);

    <T> boolean isDroneInstantiated(InjectionPoint<T> injectionPoint);

    <T> boolean isFutureDroneStored(InjectionPoint<T> injectionPoint);

    <T> boolean isDroneConfigurationStored(InjectionPoint<T> injectionPoint);

    void removeDrone(InjectionPoint<?> injectionPoint);

    void removeDroneConfiguration(InjectionPoint<?> injectionPoint);

    void removeDrones(List<InjectionPoint<?>> injectionPoints);

    void removeDroneConfigurations(List<InjectionPoint<?>> injectionPoints);

    /**
     * @param droneClass
     * @param filters
     * @param <T>
     * @return
     * @throws IllegalStateException If matched injection points count is not exactly 1
     */
    <T> InjectionPoint<? extends T> findSingle(Class<T> droneClass, Filter... filters) throws IllegalStateException;

    <T> List<InjectionPoint<? extends T>> find(Class<T> droneClass, Filter... filters);
}

