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

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;

/**
 * Destructor of drone instances. Disposes instance of every field annotated with {@link Drone}. Disposes Drones created for
 * method arguments as well.
 *
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link DroneContext}</li>
 * <li>{@link DroneRegistry}</li>
 * <li>{@link MethodContext}</li>
 * </ol>
 *
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link After}</li>
 * <li>{@link AfterClass}</li>
 * </ol>
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneDestructor {
    private static final Logger log = Logger.getLogger(DroneDestructor.class.getName());

    @Inject
    private Instance<DroneRegistry> registry;

    @SuppressWarnings("unchecked")
    public void destroyClassScopedDrone(@Observes AfterClass event, DroneContext droneContext) {

        Class<?> clazz = event.getTestClass().getJavaClass();
        for (Field f : SecurityActions.getFieldsWithAnnotation(clazz, Drone.class)) {
            Class<?> typeClass = f.getType();
            Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

            @SuppressWarnings("rawtypes")
            Destructor destructor = getDestructorFor(typeClass);

            // get instance to be destroyed
            // if deployment failed, there is nothing to be destroyed
            Object instance = droneContext.get(typeClass, qualifier);
            if (instance != null) {
                destructor.destroyInstance(instance);
            }
            droneContext.remove(typeClass, qualifier);
        }
    }

    @SuppressWarnings("unchecked")
    public void destroyMethodScopedDrone(@Observes After event, MethodContext droneMethodContext) {

        Method method = event.getTestMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < parameterTypes.length; i++) {
            if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], Drone.class)) {
                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations[i]);

                @SuppressWarnings("rawtypes")
                Destructor destructor = getDestructorFor(parameterTypes[i]);

                // get instance to be destroyed
                // if deployment failed, there is nothing to be destroyed
                Object instance = droneMethodContext.get(parameterTypes[i], qualifier);
                if (instance != null) {
                    destructor.destroyInstance(instance);
                }

                droneMethodContext.remove(parameterTypes[i], qualifier);
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
