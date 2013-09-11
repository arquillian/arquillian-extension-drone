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
import java.util.Map;
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

        for (Field f : droneEnrichements) {
            // omit setting if already set
            if (SecurityActions.getFieldValue(testCase, f) != null) {
                continue;
            }

            Class<?> typeClass = f.getType();
            Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

            log.log(Level.FINE, "Injecting @Drone for field {0} @{1} {2}",
                    new Object[] { typeClass.getSimpleName(), qualifier.getSimpleName(), f.getName() });

            Object value = getDroneInstance(typeClass, qualifier);
            Validate.notNull(value, "Retrieved a null from Drone Context, which is not a valid Drone browser object");
            SecurityActions.setFieldValue(testCase, f, value);
        }
    }

    @Override
    public Object[] resolve(Method method) {

        Map<Integer, Annotation[]> droneEnrichements = SecurityActions.getParametersWithAnnotation(method, Drone.class);
        Class<?>[] parameters = method.getParameterTypes();
        Object[] resolution = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> droneType = parameters[i];
            if (droneEnrichements.containsKey(i)) {

                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(droneEnrichements.get(i));
                log.log(Level.FINE, "Injecting @Drone for method {0}, argument {1} @{2} {3}", new Object[] { method.getName(),
                        droneType.getSimpleName(), qualifier.getSimpleName(), parameters[i].getName() });

                Object value = getDroneInstance(droneType, qualifier);
                Validate.notNull(value, "Retrieved a null from Drone Context, which is not a valid Drone browser object");
                resolution[i] = value;
            }
        }

        return resolution;
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
