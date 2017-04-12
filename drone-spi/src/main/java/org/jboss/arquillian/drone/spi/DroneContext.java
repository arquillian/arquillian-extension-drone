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

/**
 * Context that stores global configuration and {@link DronePointContext} for drone points. It also allows to find drone
 * points based on given filters.
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
     * Returns an instance of {@link DronePointContext} for specified drone point, creating it if it does not exist.
     */
    <DRONE> DronePointContext<DRONE> get(DronePoint<DRONE> dronePoint);

    /**
     * Returns true if {@link DronePointContext} is already created for specified drone point.
     */
    <DRONE> boolean contains(DronePoint<DRONE> dronePoint);

    /**
     * Removes current instance of {@link DronePointContext} for specified drone point. If it was not created,
     * does nothing.
     */
    <DRONE> void remove(DronePoint<DRONE> dronePoint);

    /**
     * Returns a {@link FilterableResult} of injection points with drone type that can be cast to the given type.
     * Using the Object.class as the type, it returns all injection points stored.
     */
    <DRONE> FilterableResult<DRONE> find(Class<DRONE> droneClass);
}

