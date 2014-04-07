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

import org.jboss.arquillian.drone.spi.DronePoint;

import java.lang.annotation.Annotation;

public class DronePointImpl<DRONE> implements DronePoint<DRONE> {

    private final Class<DRONE> droneClass;
    private final Class<? extends Annotation> qualifier;
    private final Lifecycle lifecycle;

    public DronePointImpl(Class<DRONE> droneClass, Class<? extends Annotation> qualifier, Lifecycle lifecycle) {
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
    public boolean conformsTo(Class<?> droneClass) {
        if (droneClass == null) {
            return true; // FIXME return true or throw InvalidArgumentException
        }
        return droneClass.isAssignableFrom(this.droneClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DronePointImpl that = (DronePointImpl) o;

        if (droneClass != null ? !droneClass.equals(that.droneClass) : that.droneClass != null) {
            return false;
        }
        if (lifecycle != that.lifecycle) {
            return false;
        }
        if (qualifier != null ? !qualifier.equals(that.qualifier) : that.qualifier != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = droneClass != null ? droneClass.hashCode() : 0;
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        result = 31 * result + (lifecycle != null ? lifecycle.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DronePointImpl{" +
                "droneClass=" + droneClass +
                ", qualifier=" + qualifier +
                ", lifecycle=" + lifecycle +
                '}';
    }
}
