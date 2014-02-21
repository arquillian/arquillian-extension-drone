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

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.DroneLifecycleEvent;
import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enriches test with drone instance and context path. Injects existing instance into every field annotated with
 * {@link Drone}.
 * Handles enrichment for method arguments as well.
 * <p/>
 * This enricher is responsible for firing chain of events that transform a callable into real instance by firing
 * {@link BeforeDroneInstantiated} event.
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 */
public class DroneTestEnricher implements TestEnricher {
    private static final Logger log = Logger.getLogger(DroneTestEnricher.class.getName());

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Event<DroneLifecycleEvent> droneLifecycleEvent;

    @Override
    public void enrich(Object testCase) {
        DroneContext context = droneContext.get();
        Map<Field, InjectionPoint<?>> injectionPoints = InjectionPoints.fieldsInClass(testCase.getClass());

        for (Field field : injectionPoints.keySet()) {
            // omit setting if already set
            if (SecurityActions.getFieldValue(testCase, field) != null) {
                log.log(Level.FINER, "Skipped injection of field {0}", field.getName());
                continue;
            }

            InjectionPoint<?> injectionPoint = injectionPoints.get(field);

            log.log(Level.FINE, "Injecting @Drone for field {0} @{1} {2}",
                    new Object[] { injectionPoint.getDroneType().getSimpleName(),
                            injectionPoint.getQualifier().getSimpleName(), field.getName() });

            Object drone = context.getDrone(injectionPoint);
            Validate.notNull(drone, "Retrieved a null from Drone Context, which is not a valid Drone browser object");
            SecurityActions.setFieldValue(testCase, field, drone);
        }
    }

    @Override
    public Object[] resolve(Method method) {
        DroneContext context = droneContext.get();
        InjectionPoint<?>[] injectionPoints = InjectionPoints.parametersInMethod(method);
        Object[] resolution = new Object[injectionPoints.length];
        for (int i = 0; i < injectionPoints.length; i++) {
            InjectionPoint<?> injectionPoint = injectionPoints[i];
            if (injectionPoint == null) {
                resolution[i] = null;
                continue;
            }


            log.log(Level.FINE, "Injecting @Drone for method {0}, argument {1} @{2} ",
                    new Object[] { method.getName(),
                            injectionPoint.getDroneType().getSimpleName(),
                            injectionPoint.getQualifier().getSimpleName() });

            Object drone = context.getDrone(injectionPoint);
            Validate.notNull(drone, "Retrieved a null from Drone Context, which is not a valid Drone browser object");
            resolution[i] = drone;
        }

        return resolution;
    }
}
