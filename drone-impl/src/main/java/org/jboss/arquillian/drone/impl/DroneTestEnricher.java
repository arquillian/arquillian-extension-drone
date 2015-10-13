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

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.command.PrepareDrone;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * Enriches test with drone instance and context path. Injects existing instance into every field annotated with
 * {@link Drone}.
 * Handles enrichment for method arguments as well.
 * <p/>
 * This enricher is indirectly responsible for firing chain of events that transform a callable into real instance by
 * firing {@link BeforeDroneInstantiated} event.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class DroneTestEnricher implements TestEnricher {
    private static final Logger log = Logger.getLogger(DroneTestEnricher.class.getName());

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Event<PrepareDrone> prepareDroneCommand;

    @Override
    public void enrich(Object testCase) {
        DroneTestEnrichHelper.enrichTestClass(testCase.getClass(), testCase, false, droneContext, prepareDroneCommand);
    }

    @Override
    public Object[] resolve(Method method) {
        DroneContext context = droneContext.get();
        DronePoint<?>[] dronePoints = InjectionPoints.parametersInMethod(droneContext.get(), method);
        Object[] resolution = new Object[dronePoints.length];
        for (int i = 0; i < dronePoints.length; i++) {
            DronePoint<?> dronePoint = dronePoints[i];
            if (dronePoint == null) {
                resolution[i] = null;
                continue;
            }

            DroneTestEnrichHelper.ensureInjectionPointPrepared(dronePoint, droneContext, prepareDroneCommand);

            log.log(Level.FINE, "Injecting @Drone for method {0}, injection point {1}",
                new Object[] { method.getName(), dronePoint }
            );

            Object drone = context.get(dronePoint).getInstance();
            Validate.stateNotNull(drone, "Retrieved a null from Drone Context, which is not a valid Drone browser " +
                    "object" +
                    ".\nMethod: {0}, injection point: {1},", method.getName(), dronePoint);
            resolution[i] = drone;
        }

        return resolution;
    }
}
