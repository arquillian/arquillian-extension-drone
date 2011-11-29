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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.event.DroneConfigured;
import org.jboss.arquillian.drone.event.MethodDroneConfigured;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * Configurator of Drone Configuration. Creates a configuration for every field annotated with {@link Drone}.
 *
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor}</li>
 * <li>{@link DroneRegistry}</li>
 * </ol>
 *
 * <p>
 * Produces:
 * </p>
 * <ol>
 * <li>{@link DroneContext}</li>
 * <li>{@link MethodContext}</li>
 * </ol>
 *
 * <p>
 * Fires:
 * </p>
 * <ol>
 * <li>{@link DroneConfigured}</li>
 * <li>{@link MethodDroneConfigured}</li>
 * </ol>
 *
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link BeforeClass}</li>
 * </ol>
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneConfigurator {
    private static Logger log = Logger.getLogger(DroneConfigurator.class.getName());

    @Inject
    @ClassScoped
    private InstanceProducer<DroneContext> droneContext;

    @Inject
    @TestScoped
    private InstanceProducer<MethodContext> droneMethodContext;

    @Inject
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    @Inject
    private Event<DroneConfigured> afterConfiguration;

    @Inject
    private Event<MethodDroneConfigured> afterMethodConfiguration;

    public void configureDrone(@Observes BeforeClass event, DroneRegistry registry) {
        // check if any field is @Drone annotated
        List<Field> fields = SecurityActions.getFieldsWithAnnotation(event.getTestClass().getJavaClass(), Drone.class);
        if (fields.isEmpty()) {
            return;
        }

        droneContext.set(new DroneContext());
        for (Field f : fields) {
            Class<?> typeClass = f.getType();
            Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

            Validate.notNull(arquillianDescriptor.get(), "ArquillianDescriptor should not be null");
            Configurator<?, ?> configurator = registry.getEntryFor(typeClass, Configurator.class);
            DroneConfiguration<?> configuration = configurator.createConfiguration(arquillianDescriptor.get(), qualifier);
            droneContext.get().add(configuration.getClass(), qualifier, configuration);
            afterConfiguration.fire(new DroneConfigured(f, qualifier, configuration));
        }

    }

    public void configureDrone(@Observes Before event, DroneRegistry registry) {
        Method method = event.getTestMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // check if any field is @Drone annotated
        if (parameterTypes.length == 0) {
            return;
        }

        droneMethodContext.set(new MethodContext());
        for (int i = 0; i < parameterTypes.length; i++) {
            if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], Drone.class)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Resolving method " + method.getName() + " argument at position " + i);
                }

                Validate.notNull(arquillianDescriptor.get(), "ArquillianDescriptor should not be null");
                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations[i]);

                Configurator<?, ?> configurator = registry.getEntryFor(parameterTypes[i], Configurator.class);
                DroneConfiguration<?> configuration = configurator.createConfiguration(arquillianDescriptor.get(), qualifier);
                droneMethodContext.get().add(configuration.getClass(), qualifier, configuration);
                afterMethodConfiguration.fire(new MethodDroneConfigured(parameterTypes[i], qualifier, configuration));
            }
        }

    }
}
