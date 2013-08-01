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
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.event.AfterDroneDestroyed;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDestroyed;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;

/**
 * Destructor of Drone instance. Disposes both class scoped Drones as well as method scoped ones.
 *
 * <p>
 * Observes:
 * </p>
 * {@link AfterClass} {@link After}
 *
 * <p>
 * Fires:
 * </p>
 * {@link BeforeDroneDestroyed} {@link AfterDroneDestroyed}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneDestructor {
    private static final Logger log = Logger.getLogger(DroneDestructor.class.getName());

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<DroneRegistry> registry;

    @Inject
    private Event<BeforeDroneDestroyed> beforeDroneDestroyed;

    @Inject
    private Event<AfterDroneDestroyed> afterDroneDestroyed;

    @SuppressWarnings("unchecked")
    public void destroyClassScopedDrone(@Observes AfterClass event, DroneContext droneContext) {

        Class<?> clazz = event.getTestClass().getJavaClass();
        for (Field f : SecurityActions.getFieldsWithAnnotation(clazz, Drone.class)) {
            Class<?> droneType = f.getType();
            Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

            @SuppressWarnings("rawtypes")
            Destructor destructor = getDestructorFor(droneType);

            // get instance to be destroyed
            // if deployment failed, there is nothing to be destroyed
            InstanceOrCallableInstance instance = droneContext.get(droneType, qualifier);
            if (instance != null) {
                beforeDroneDestroyed.fire(new BeforeDroneDestroyed(instance, droneType, qualifier));
                destructor.destroyInstance(instance.asInstance(droneType));
                droneContext.remove(droneType, qualifier);
                afterDroneDestroyed.fire(new AfterDroneDestroyed(droneType, qualifier));
            }

        }
    }

    @SuppressWarnings("unchecked")
    public void destroyMethodScopedDrone(@Observes After event, DroneContext droneContext) {

        Method method = event.getTestMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < parameterTypes.length; i++) {
            if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], Drone.class)) {
                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations[i]);

                Class<?> droneType = parameterTypes[i];

                @SuppressWarnings("rawtypes")
                Destructor destructor = getDestructorFor(droneType);

                // get instance to be destroyed
                // if deployment failed, there is nothing to be destroyed
                InstanceOrCallableInstance instance = droneContext.get(droneType, qualifier);
                if (instance != null) {
                    beforeDroneDestroyed.fire(new BeforeDroneDestroyed(instance, droneType, qualifier));
                    destructor.destroyInstance(instance.asInstance(droneType));
                    droneContext.remove(droneType, qualifier);
                    afterDroneDestroyed.fire(new AfterDroneDestroyed(droneType, qualifier));
                }
            }
        }

    }

    @SuppressWarnings("rawtypes")
    private Destructor getDestructorFor(Class<?> typeClass) {
        // must be defined as raw because instance type to be destroyer cannot
        // be determined in compile time
        Destructor destructor = registry.get().getEntryFor(typeClass, Destructor.class);

        if (log.isLoggable(Level.FINE)) {
            log.fine("Using destructor defined in class: " + destructor.getClass().getName() + ", with precedence "
                    + destructor.getPrecedence());
        }

        return destructor;
    }
}
