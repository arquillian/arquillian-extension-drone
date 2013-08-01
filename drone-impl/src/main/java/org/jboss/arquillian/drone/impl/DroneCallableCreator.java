/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.spi.event.AfterDroneCallableCreated;
import org.jboss.arquillian.drone.spi.event.AfterDroneConfigured;
import org.jboss.arquillian.drone.spi.event.BeforeDroneCallableCreated;

/**
 * Creator of {@link Callable} wrappers for Drone instances. The purpose of this is to be able to:
 *
 * <ol>
 * <li>Limit time needed to create a Drone instance and fail gracefully if time limit is not met</li>
 * <li>Allow instance to created as late as possible, allowing other extensions to start all required services</li>
 * </ol>
 *
 * <p>
 * Observes:
 * </p>
 * {@link AfterDroneConfigured}
 *
 * <p>
 * Fires:
 * </p>
 * {@link BeforeDroneCallableCreated} {@link AfterDroneCallableCreated}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneCallableCreator {
    private static final Logger log = Logger.getLogger(DroneCallableCreator.class.getName());

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<BeforeDroneCallableCreated> beforeDroneCallableCreated;

    @Inject
    private Event<AfterDroneCallableCreated> afterDroneCallableCreated;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createDroneCallable(@Observes AfterDroneConfigured event, DroneRegistry registry, DroneContext droneContext) {

        final Class<?> type = event.getDroneType();
        final Class<? extends Annotation> qualifier = event.getQualifier();
        final DroneConfiguration<?> configuration = event.getConfiguration().asInstance(DroneConfiguration.class);

        // @SuppressWarnings({ "rawtypes" })
        final Instantiator instantiator = registry.getEntryFor(type, Instantiator.class);
        if (log.isLoggable(Level.FINE)) {
            log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence "
                    + instantiator.getPrecedence());
        }

        beforeDroneCallableCreated.fire(new BeforeDroneCallableCreated(instantiator, type, qualifier));

        // create future instance
        Callable<?> instanceCallable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return instantiator.createInstance(configuration);
            }
        };
        InstanceOrCallableInstance futureDrone = new InstanceOrCallableInstanceImpl(instanceCallable);

        droneContext.add(type, qualifier, futureDrone);
        afterDroneCallableCreated.fire(new AfterDroneCallableCreated(futureDrone, type, qualifier));
    }
}
