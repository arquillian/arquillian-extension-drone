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
package org.jboss.arquillian.drone.spi;

import java.lang.annotation.Annotation;

/**
 * This event is fired when Drone instance is ready to be used, which is right after instantiation.
 *
 * @author Lukas Fryc
 */
public class DroneReady {

    private Class<?> type;
    private Class<? extends Annotation> qualifier;
    private Object instance;

    public DroneReady(Class<?> type, Class<? extends Annotation> qualifier, Object instance) {
        this.type = type;
        this.qualifier = qualifier;
        this.instance = instance;
    }

    /**
     * The type requested to be injected by Drone
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * The qualifier associated with given injection point
     */
    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

    /**
     * The injected instance
     *
     * @return
     */
    public Object getInstance() {
        return instance;
    }
}
