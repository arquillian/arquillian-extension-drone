/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.command.PrepareDrone;

/**
 * A helper class for enriching a test class with drone instance and context path.
 *
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
class DroneTestEnrichHelper {
    private static final Logger log = Logger.getLogger(DroneTestEnrichHelper.class.getName());

    /**
     * Enriches given test class with drone instance and context path. Injects existing instance into every field
     * annotated with {@link Drone}.
     *
     * @param testClass Test class to be enriched
     * @param testCase Instance of the test case (usually the class is same as the {@code testClass})
     * @param onlyStatic If the drone instance should be injected only into static fields
     * @param droneContext Drone context the drone instance should be retrieved from
     * @param prepareDroneCommand Prepare drone command for firing events
     */
    static void enrichTestClass(Class<?> testClass, Object testCase, boolean onlyStatic,
        Instance<DroneContext> droneContext, Event<PrepareDrone> prepareDroneCommand) {
        DroneContext context = droneContext.get();

        Map<Field, DronePoint<?>> injectionPoints = InjectionPoints.fieldsInClass(droneContext.get(),
            testClass);

        for (Field field : injectionPoints.keySet()) {
            if (onlyStatic && !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            // omit setting if already set
            if (SecurityActions.getFieldValue(testCase, field) != null) {
                log.log(Level.FINER, "Skipped injection of field {0}", field.getName());
                continue;
            }

            DronePoint<?> dronePoint = injectionPoints.get(field);

            ensureInjectionPointPrepared(dronePoint, droneContext, prepareDroneCommand);

            log.log(Level.FINE, "Injecting @Drone for field {0}, injection point {1}",
                new Object[] { dronePoint.getDroneType().getSimpleName(), dronePoint }
            );

            Object drone = context.get(dronePoint).getInstance();
            Validate.stateNotNull(drone, "Retrieved a null from Drone Context, " +
                    "which is not a valid Drone browser object. \nClass: {0}, field: {1}, injection point: {2}",
                testClass.getName(), field.getName(), dronePoint
            );
            SecurityActions.setFieldValue(testCase, field, drone);
        }
    }

    /**
     * Ensures whether the given drone point is prepared for injection
     *
     * @param dronePoint Drone point that should be checked
     * @param droneContext Drone context the prepared drone instance should be retrieved from
     * @param prepareDroneCommand Prepare drone command for firing events
     */
    static void ensureInjectionPointPrepared(DronePoint<?> dronePoint, Instance<DroneContext> droneContext,
        Event<PrepareDrone> prepareDroneCommand) {
        if (!droneContext.get().get(dronePoint).hasFutureInstance()) {
            if (dronePoint.getLifecycle() != DronePoint.Lifecycle.DEPLOYMENT) {
                log.log(Level.WARNING, "Injection point {0} was not prepared yet. It will be prepared now, " +
                    "but it''s recommended that all drones with class lifecycle are prepared in " +
                    "@BeforeClass by the DroneLifecycleManager!", dronePoint);

                prepareDroneCommand.fire(new PrepareDrone(dronePoint));
            } else {
                throw new IllegalStateException(MessageFormat.format("Injection point {0} has deployment lifecycle " +
                    "and has to be prepared in @BeforeClass.", dronePoint));
            }
        }
    }
}
