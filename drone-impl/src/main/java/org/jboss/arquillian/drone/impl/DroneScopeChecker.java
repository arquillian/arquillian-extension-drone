/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.drone.impl.DroneContextImpl.QualifiedKey;
import org.jboss.arquillian.drone.spi.DroneContext;

/**
 * An utility that allows once per event handling processing only a single instance of the same Drone type+qualifier
 *
 * @see {@link DroneContext}
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
class DroneScopeChecker {

    private Set<QualifiedKey> presentDrones;

    public DroneScopeChecker() {
        this.presentDrones = new HashSet<DroneContextImpl.QualifiedKey>();
    }

    /**
     * Checks whether given {@code droneType} and {@code qualifier} was already processed within current scope
     *
     * @param droneType Type of Drone, e.g. WebDriver
     * @param qualifier Qualifier
     * @return Returns {@code true} if this is first Drone of given type, {@code false} otherwise
     */
    public boolean isUniqueInScope(Class<?> droneType, Class<? extends Annotation> qualifier) {
        QualifiedKey key = new QualifiedKey(droneType, qualifier);
        // put qualified key there and return whether it was the first key
        return presentDrones.add(key);
    }
}
