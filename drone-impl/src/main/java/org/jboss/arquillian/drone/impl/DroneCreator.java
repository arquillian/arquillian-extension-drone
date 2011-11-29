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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.event.DroneConfigured;
import org.jboss.arquillian.drone.event.MethodDroneConfigured;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneRegistry;
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

    @SuppressWarnings("unchecked")
    public void createWebTestBrowser(@Observes DroneConfigured event, DroneRegistry registry, DroneContext droneContext) {
        Field field = event.getInjected();
        Class<?> typeClass = field.getType();
        Class<? extends Annotation> qualifier = event.getQualifier();
        DroneConfiguration<?> configuration = event.getConfiguration();

        @SuppressWarnings("rawtypes")
        Instantiator instantiator = registry.getEntryFor(typeClass, Instantiator.class);

        if (log.isLoggable(Level.FINE)) {
            log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence "
                    + instantiator.getPrecedence());
        }

        droneContext.add(typeClass, qualifier, instantiator.createInstance(configuration));
    }

    @SuppressWarnings("unchecked")
    public void createWebTestBrowser(@Observes MethodDroneConfigured event, DroneRegistry registry,
            MethodContext droneMethodContext) {
        Class<?> typeClass = event.getDroneType();
        Class<? extends Annotation> qualifier = event.getQualifier();
        DroneConfiguration<?> configuration = event.getConfiguration();

        @SuppressWarnings("rawtypes")
        Instantiator instantiator = registry.getEntryFor(typeClass, Instantiator.class);

        if (log.isLoggable(Level.FINE)) {
            log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence "
                    + instantiator.getPrecedence());
        }

        droneMethodContext.add(typeClass, qualifier, instantiator.createInstance(configuration));
    }
}
