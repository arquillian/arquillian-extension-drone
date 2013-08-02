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
package org.jboss.arquillian.drone.spi.event;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.drone.spi.Enhancer;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;

/**
 * This event is fired before Drone instance is enhanced by {@link Enhancer}. It is expected that it will never contain a
 * {@link Callable} Drone, but rather a real instance.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class BeforeDroneEnhanced extends BaseDroneEvent implements DroneEnhancementEvent {
    private final Enhancer<?> enhancer;
    private final InstanceOrCallableInstance instance;

    public BeforeDroneEnhanced(Enhancer<?> enhancer, InstanceOrCallableInstance instance, Class<?> droneType,
            Class<? extends Annotation> qualifier) {
        super(droneType, qualifier);
        this.enhancer = enhancer;
        this.instance = instance;
    }

    public Enhancer<?> getEnhancer() {
        return enhancer;
    }

    public InstanceOrCallableInstance getInstance() {
        return instance;
    }

}
