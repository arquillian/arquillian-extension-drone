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

import org.jboss.arquillian.drone.spi.Filter;
import org.jboss.arquillian.drone.spi.InjectionPoint;

import java.util.HashSet;
import java.util.Set;

/**
 * Filter for finding injection points by the lifecycle.
 */
public class LifecycleFilter implements Filter {

    private final Set<InjectionPoint.Lifecycle> lifecycles;

    /**
     * Creates lifecycle filter which will match injection points with any of specified lifecycles. Usage of multiple
     * lifecycles behaves like disjunction.
     */
    public LifecycleFilter(InjectionPoint.Lifecycle lifecycle, InjectionPoint.Lifecycle... additionalLifecycles) {
        lifecycles = new HashSet<InjectionPoint.Lifecycle>();
        if(lifecycle == null) {
            throw new IllegalArgumentException("Lifecycle cannot be null!");
        }
        lifecycles.add(lifecycle);

        if(additionalLifecycles != null) {
            for (InjectionPoint.Lifecycle additionalLifecycle : additionalLifecycles) {
                if (additionalLifecycle != null) {
                    lifecycles.add(additionalLifecycle);
                }
            }
        }

    }

    @Override
    public boolean accept(InjectionPoint<?> injectionPoint) {
        return lifecycles.contains(injectionPoint.getLifecycle());
    }
}