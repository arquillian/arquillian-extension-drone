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
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneContext.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.event.AfterDroneConfigured;
import org.jboss.arquillian.drone.spi.event.BeforeDroneConfigured;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * Creator of Drone configurations. Drone configuration is created either before class or before method, depending on the scope
 * of Drone instance, based on data provided in arquillian.xml.
 *
 * <p>
 * Creates:
 * </p>
 * {@see DroneContext}
 *
 * <p>
 * Observes:
 * </p>
 * {@see BeforeClass} {@see Before}
 *
 * <p>
 * Fires:
 * </p>
 * {@see BeforeDroneConfigured} {@see AfterDroneConfigured}
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
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    @Inject
    private Event<BeforeDroneConfigured> beforeDroneConfigured;

    @Inject
    private Event<AfterDroneConfigured> afterDroneConfigured;

    public void configureDrone(@Observes BeforeClass event, DroneRegistry registry) {

        // create Drone Context
        droneContext.set(new DroneContextImpl());

        // check if any field is @Drone annotated
        List<Field> fields = SecurityActions.getFieldsWithAnnotation(event.getTestClass().getJavaClass(), Drone.class);
        if (fields.isEmpty()) {
            return;
        }

        for (Field f : fields) {
            Class<?> droneType = f.getType();
            Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

            Validate.notNull(arquillianDescriptor.get(), "ArquillianDescriptor should not be null");
            Configurator<?, ?> configurator = registry.getEntryFor(droneType, Configurator.class);

            beforeDroneConfigured.fire(new BeforeDroneConfigured(configurator, droneType, qualifier));
            DroneConfiguration<?> configuration = configurator.createConfiguration(arquillianDescriptor.get(), qualifier);
            InstanceOrCallableInstance droneConfiguration = new InstanceOrCallableInstanceImpl(configuration);

            droneContext.get().add(configuration.getClass(), qualifier, droneConfiguration);
            afterDroneConfigured.fire(new AfterDroneConfigured(droneConfiguration, droneType, qualifier));
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

        Validate.stateNotNull(droneContext.get(), "DroneContext should be available while working with method scoped instances");

        for (int i = 0; i < parameterTypes.length; i++) {
            if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], Drone.class)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Resolving method " + method.getName() + " argument at position " + i);
                }

                Validate.notNull(arquillianDescriptor.get(), "ArquillianDescriptor should not be null");
                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations[i]);

                Configurator<?, ?> configurator = registry.getEntryFor(parameterTypes[i], Configurator.class);

                beforeDroneConfigured.fire(new BeforeDroneConfigured(configurator, parameterTypes[i], qualifier));
                DroneConfiguration<?> configuration = configurator.createConfiguration(arquillianDescriptor.get(), qualifier);
                InstanceOrCallableInstance droneConfiguration = new InstanceOrCallableInstanceImpl(configuration);
                droneContext.get().add(configuration.getClass(), qualifier, droneConfiguration);
                afterDroneConfigured.fire(new AfterDroneConfigured(droneConfiguration, parameterTypes[i], qualifier));
            }
        }
    }
}
