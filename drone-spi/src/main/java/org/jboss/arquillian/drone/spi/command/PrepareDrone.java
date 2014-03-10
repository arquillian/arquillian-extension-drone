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
package org.jboss.arquillian.drone.spi.command;

import org.jboss.arquillian.drone.spi.InjectionPoint;

/**
 * Command event that will trigger the preparation of a drone. After fired,
 * {@link org.jboss.arquillian.drone.spi.DroneContext} should contain
 * {@link org.jboss.arquillian.drone.spi.DroneConfiguration} and {@link org.jboss.arquillian.drone.spi.CachingCallable}
 * for specified injection point.
 */
public class PrepareDrone extends InjectionPointCommand {

    public PrepareDrone(InjectionPoint<?> injectionPoint) {
        super(injectionPoint);
    }

}
