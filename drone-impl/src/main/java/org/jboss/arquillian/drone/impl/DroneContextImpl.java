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
import org.jboss.arquillian.drone.spi.DronePointFilter;

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
    public <T> DronePoint<T> findSingle(Class<T> droneClass,
                                        DronePointFilter<? super T>... filters) throws IllegalStateException {
        List<DronePoint<T>> dronePoints = find(droneClass, filters);
        int count = dronePoints.size();
        if (count != 1) {
            StringBuilder builder = new StringBuilder("Total injection points matched not equal to 1! Actual count: ");
            builder.append(count).append(". Matched points: [ ");
            int i = 0;
            for (DronePoint<T> dronePoint : dronePoints) {
                if(i > 0) {
                    builder.append(", ");
                }
                builder.append(dronePoint);
                i++;
            }
            builder.append(" ]");
            throw new IllegalStateException(builder.toString());
        }
        return dronePoints.get(0);
    }

    @Override
    public <T> List<DronePoint<T>> find(Class<T> droneClass, DronePointFilter<? super T>... filters) {
        List<DronePoint<T>> matchedDronePoints = new ArrayList<DronePoint<T>>();

        // We need to drop the generic type in order to be able to call 'accepts' on filters with <? super T>
        for (DronePoint<?> dronePoint : droneContextMap.keySet()) {
            if (!dronePoint.conformsTo(droneClass)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            DronePoint<T> castDronePoint = (DronePoint<T>) dronePoint;

            boolean matches = true;

            for (DronePointFilter<? super T> filter : filters) {
                if (!filter.accepts(this, castDronePoint)) {
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
