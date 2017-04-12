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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Comparator;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.spi.DronePoint;

public class DronePointImpl<DRONE> implements DronePoint<DRONE> {

    private final Class<DRONE> droneClass;
    private final Annotation[] annotations;
    private final Lifecycle lifecycle;

    public DronePointImpl(Class<DRONE> droneClass, Lifecycle lifecycle, Annotation... annotations) {
        for (Annotation annotation : annotations) {
            if (annotation == null) {
                throw new IllegalArgumentException("Annotation cannot be null!");
            }
        }

        this.droneClass = droneClass;
        this.annotations = annotations;
        this.lifecycle = lifecycle;

        Arrays.sort(this.annotations, new Comparator<Annotation>() {
            @Override
            public int compare(Annotation o1, Annotation o2) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
        });
    }

    @Override
    public Class<DRONE> getDroneType() {
        return droneClass;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    @Deprecated
    public Class<? extends Annotation> getQualifier() {
        Class<? extends Annotation> qualifier = SecurityActions.getQualifier(annotations);
        return qualifier != null ? qualifier : Default.class;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        DronePointImpl<DRONE> that = (DronePointImpl<DRONE>) o;

        if (!Arrays.equals(annotations, that.annotations)) return false;
        if (droneClass != null ? !droneClass.equals(that.droneClass) : that.droneClass != null) return false;
        if (lifecycle != that.lifecycle) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = droneClass != null ? droneClass.hashCode() : 0;
        result = 31 * result + (annotations != null ? Arrays.hashCode(annotations) : 0);
        result = 31 * result + (lifecycle != null ? lifecycle.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DronePointImpl{" +
            "droneClass=" + droneClass +
            ", annotations=" + Arrays.toString(annotations) +
            ", lifecycle=" + lifecycle +
            '}';
    }
}
