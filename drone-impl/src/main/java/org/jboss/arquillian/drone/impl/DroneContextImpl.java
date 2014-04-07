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
package org.jboss.arquillian.drone.impl;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointContext;
import org.jboss.arquillian.drone.spi.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Default implementation of {@link DroneContext}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 */
public class DroneContextImpl implements DroneContext {
    private static final Logger LOGGER = Logger.getLogger(DroneContextImpl.class.getName());

    private final Map<DronePoint<?>, DronePointContext<?>> droneContextMap;

    @Inject
    private Instance<Injector> injector;

    @Deprecated
    private DroneConfiguration<?> globalDroneConfiguration;

    public DroneContextImpl() {
        droneContextMap = new HashMap<DronePoint<?>, DronePointContext<?>>();
    }

    @Override
    public <C extends DroneConfiguration<C>> C getGlobalDroneConfiguration(Class<C> configurationClass) {
        return (C) globalDroneConfiguration;
    }

    @Override
    public void setGlobalDroneConfiguration(DroneConfiguration<?> configuration) {
        globalDroneConfiguration = configuration;
    }

    @Override
    public <DRONE> DronePointContext<DRONE> get(DronePoint<DRONE> dronePoint) {
        if (!droneContextMap.containsKey(dronePoint)) {
            DronePointContext pointContext = injector.get().inject(new DronePointContextImpl<DRONE>(dronePoint));
            droneContextMap.put(dronePoint, pointContext);
        }

        return (DronePointContext<DRONE>) droneContextMap.get(dronePoint);
    }

    @Override
    public <DRONE> boolean contains(DronePoint<DRONE> dronePoint) {
        return droneContextMap.containsKey(dronePoint);
    }

    @Override
    public <DRONE> void remove(DronePoint<DRONE> dronePoint) {
        // FIXME should we return the removed context?
        droneContextMap.remove(dronePoint);
    }

    @Override
    public <T> DronePoint<? extends T> findSingle(Class<T> droneClass,
                                                  Filter... filters) throws IllegalStateException {
        List<DronePoint<? extends T>> dronePoints = find(droneClass, filters);
        int count = dronePoints.size();
        if (count != 1) {
            throw new IllegalStateException("Total injection points matched not equal to 1! Actual: " + count);
        }
        return dronePoints.get(0);
    }

    @Override
    public <T> List<DronePoint<? extends T>> find(Class<T> droneClass, Filter... filters) {
        List<DronePoint<? extends T>> matchedDronePoints = new ArrayList<DronePoint<? extends T>>();

        for (DronePoint<?> dronePoint : droneContextMap.keySet()) {
            if (!dronePoint.conformsTo(droneClass)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            DronePoint<? extends T> castDronePoint = (DronePoint<? extends T>) dronePoint;

            boolean matches = true;

            for (Filter filter : filters) {
                if (!filter.accept(castDronePoint)) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                matchedDronePoints.add(castDronePoint);
            }
        }

        return matchedDronePoints;
    }

}
