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

import org.jboss.arquillian.drone.spi.InjectionPoint;

import java.lang.annotation.Annotation;

public class InjectionPointImpl<DRONE> implements InjectionPoint<DRONE> {

    private final Class<DRONE> droneClass;
    private final Class<? extends Annotation> qualifier;
    private final Lifecycle lifecycle;

    public InjectionPointImpl(Class<DRONE> droneClass, Class<? extends Annotation> qualifier, Lifecycle lifecycle) {
        this.droneClass = droneClass;
        this.qualifier = qualifier;
        this.lifecycle = lifecycle;
    }

    @Override
    public Class<DRONE> getDroneType() {
        return droneClass;
    }

    @Override
    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public int hashCode() {
        int hash = 31;

        hash = 89 * hash + (droneClass != null ? droneClass.hashCode() : 0);
        hash = 89 * hash + (qualifier != null ? qualifier.hashCode() : 0);
        hash = 89 * hash + (lifecycle != null ? lifecycle.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    @Override
    public String toString() {
        return "Drone type: " + (droneClass != null ? droneClass.getSimpleName() : " (null)") + ", " +
                "Qualifier: " + (qualifier != null ? qualifier.getSimpleName() : " (null)") + ", " +
                "Lifecycle: " + (lifecycle != null ? lifecycle.name() : "(null)");
    }
}
