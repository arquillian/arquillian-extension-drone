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
package org.jboss.arquillian.drone.spi.filter;

import java.util.HashSet;
import java.util.Set;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointFilter;

/**
 * Filter for finding injection points by the lifecycle.
 */
public class LifecycleFilter implements DronePointFilter<Object> {

    private final Set<DronePoint.Lifecycle> lifecycles;

    /**
     * Creates lifecycle filter which will match injection points with any of specified lifecycles. Usage of multiple
     * lifecycles behaves like disjunction.
     *
     * @throws java.lang.IllegalArgumentException
     *     If any of the given lifecycles is null.
     */
    public LifecycleFilter(DronePoint.Lifecycle lifecycle, DronePoint.Lifecycle... additionalLifecycles) throws
        IllegalArgumentException {
        lifecycles = new HashSet<DronePoint.Lifecycle>();
        if (lifecycle == null) {
            throw new IllegalArgumentException("Lifecycle cannot be null!");
        }
        lifecycles.add(lifecycle);

        if (additionalLifecycles != null) {
            for (DronePoint.Lifecycle additionalLifecycle : additionalLifecycles) {
                if (additionalLifecycle == null) {
                    throw new IllegalArgumentException("Lifecycle cannot be null");
                }

                lifecycles.add(additionalLifecycle);
            }
        }
    }

    @Override
    public boolean accepts(DroneContext context, DronePoint<?> dronePoint) {
        return lifecycles.contains(dronePoint.getLifecycle());
    }
}
