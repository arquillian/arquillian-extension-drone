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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.event.DroneConfigured;
import org.jboss.arquillian.drone.event.MethodDroneConfigured;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneReady;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.Enhancer;
import org.jboss.arquillian.drone.spi.Instantiator;

/**
 * Creator of drone instances. Creates a instance for every field annotated with {@link Drone}.
 *
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link DroneRegistry}</li>
 * <li>{@link DroneContext}</li>
 * </ol>
 *
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link DroneConfigured}</li>
 * </ol>
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneCreator {
    private static final Logger log = Logger.getLogger(DroneCreator.class.getName());

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<DroneReady> ready;

    @SuppressWarnings("unchecked")
    public void createWebTestBrowser(@Observes DroneConfigured event, DroneRegistry registry, DroneContext droneContext) {
        Field field = event.getInjected();
        Class<?> type = field.getType();
        Class<? extends Annotation> qualifier = event.getQualifier();
        DroneConfiguration<?> configuration = event.getConfiguration();

        @SuppressWarnings("rawtypes")
        Instantiator instantiator = registry.getEntryFor(type, Instantiator.class);

        if (log.isLoggable(Level.FINE)) {
            log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence "
                    + instantiator.getPrecedence());
        }

        Object instance = instantiator.createInstance(configuration);

        instance = enhance(type, qualifier, instance);

        droneContext.add(type, qualifier, instance);

        ready.fire(new DroneReady(type, qualifier, instance));
    }

    @SuppressWarnings("unchecked")
    public void createWebTestBrowser(@Observes MethodDroneConfigured event, DroneRegistry registry,
            MethodContext droneMethodContext) {
        Class<?> type = event.getDroneType();
        Class<? extends Annotation> qualifier = event.getQualifier();
        DroneConfiguration<?> configuration = event.getConfiguration();

        @SuppressWarnings("rawtypes")
        Instantiator instantiator = registry.getEntryFor(type, Instantiator.class);

        if (log.isLoggable(Level.FINE)) {
            log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence "
                    + instantiator.getPrecedence());
        }

        Object instance = instantiator.createInstance(configuration);

        instance = enhance(type, qualifier, instance);

        droneMethodContext.add(type, qualifier, instance);

        ready.fire(new DroneReady(type, qualifier, instance));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object enhance(Class<?> type, Class<? extends Annotation> qualifier, Object instance) {

        List<Enhancer> enhancers = new ArrayList<Enhancer>(serviceLoader.get().all(Enhancer.class));
        Collections.sort(enhancers, PrecedenceComparator.getInstance());

        for (Enhancer enhancer : enhancers) {
            if (enhancer.canEnhance(type, qualifier)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Applying enhancer: " + enhancer.getClass().getName() + ", with precedence "
                            + enhancer.getPrecedence());
                }
                instance = enhancer.enhance(instance, qualifier);
            }
        }

        return instance;
    }
}
