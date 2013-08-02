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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.DroneLifecycleEvent;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * Enriches test with drone instance and context path. Injects existing instance into every field annotated with {@link Drone}.
 * Handles enrichment for method arguments as well.
 *
 * This enricher is responsible for firing chain of events that transform a callable into real instance by firing
 * {@link BeforeDroneInstantiated} event.
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneTestEnricher implements TestEnricher {
    private static final Logger log = Logger.getLogger(DroneTestEnricher.class.getName());

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Event<DroneLifecycleEvent> droneLifecycleEvent;

    @Override
    public void enrich(Object testCase) {
        List<Field> droneEnrichements = SecurityActions.getFieldsWithAnnotation(testCase.getClass(), Drone.class);
        if (!droneEnrichements.isEmpty()) {
            droneEnrichement(testCase, droneEnrichements);
        }
    }

    @Override
    public Object[] resolve(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] resolution = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], Drone.class)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Resolving method " + method.getName() + " argument at position " + i);
                }

                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations[i]);

                Object value = getDroneInstance(parameterTypes[i], qualifier);
                Validate.notNull(value, "Retrieved a null from context, which is not a valid Drone browser object");

                resolution[i] = value;
            }
        }

        return resolution;
    }

    private void droneEnrichement(Object testCase, List<Field> fields) {
        try {
            for (Field f : fields) {
                // omit setting if already set
                if (f.get(testCase) != null) {
                    return;
                }

                Class<?> typeClass = f.getType();
                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

                Object value = getDroneInstance(typeClass, qualifier);
                f.set(testCase, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not enrich test with Drone members", e);
        }
    }

    private <T> T getDroneInstance(Class<T> type, Class<? extends Annotation> qualifier) {
        Validate.stateNotNull(droneContext.get(), "Drone Context must not be null");

        InstanceOrCallableInstance union = droneContext.get().get(type, qualifier);

        Validate.notNull(union, "Retrieved a null from context, which is not a valid Drone browser object");

        // execute chain to convert callable into browser
        if (union.isInstanceCallable()) {
            droneLifecycleEvent.fire(new BeforeDroneInstantiated(union, type, qualifier));
        }

        // return browser
        return union.asInstance(type);

    }
}
