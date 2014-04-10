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

import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointFilter;

/**
 * Filter that accepts everything that its underlying one do not.
 */
public class InverseFilter<DRONE> implements DronePointFilter<DRONE> {

    private final DronePointFilter<DRONE> wrappedFilter;

    public InverseFilter(DronePointFilter<DRONE> wrappedFilter) {
        this.wrappedFilter = wrappedFilter;
    }

    @Override
    public boolean accepts(DroneContext context, DronePoint<? extends DRONE> dronePoint) {
        return !wrappedFilter.accepts(context, dronePoint);
    }
}
