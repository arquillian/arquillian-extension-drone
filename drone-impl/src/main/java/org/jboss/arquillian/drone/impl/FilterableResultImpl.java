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
package org.jboss.arquillian.drone.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointFilter;
import org.jboss.arquillian.drone.spi.FilterableResult;

public class FilterableResultImpl<DRONE> implements FilterableResult<DRONE> {

    private final DroneContext context;
    private final Set<DronePoint<DRONE>> wrapped;

    public FilterableResultImpl(DroneContext context, Set<DronePoint<DRONE>> wrapped) {
        if (context == null) {
            throw new IllegalArgumentException("DroneContext cannot be null!");
        }
        if (wrapped == null) {
            throw new IllegalArgumentException("Wrapped Set cannot be null!");
        }
        this.context = context;
        this.wrapped = wrapped;
    }

    @Override
    public FilterableResult<DRONE> filter(DronePointFilter<? super DRONE> filter) {
        Set<DronePoint<DRONE>> dronePoints = new HashSet<DronePoint<DRONE>>();
        for (DronePoint<DRONE> dronePoint : wrapped) {
            if (filter.accepts(context, dronePoint)) {
                dronePoints.add(dronePoint);
            }
        }

        return new FilterableResultImpl<DRONE>(context, dronePoints);
    }

    @Override
    public DronePoint<DRONE> single() {
        int count = size();
        if (count != 1) {
            StringBuilder builder = new StringBuilder("Total injection points matched not equal to 1! Actual count: ");
            builder.append(count).append(". Matched points: [ ");
            int i = 0;
            for (DronePoint<DRONE> dronePoint : wrapped) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(dronePoint);
                i++;
            }
            builder.append(" ]");
            throw new IllegalStateException(builder.toString());
        }
        return wrapped.iterator().next();
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public Iterator<DronePoint<DRONE>> iterator() {
        return wrapped.iterator();
    }
}
