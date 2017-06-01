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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
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
    private Instance<Injector> injector;

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Event<PrepareDrone> prepareDroneCommand;

    @Inject
    @ApplicationScoped
    private InstanceProducer<DeploymentDronePointsRegistry> deploymentDronePointsRegistry;

    public static final String ARQUILLIAN_DRONE_CREATION_PROPERTY = "arquillian.drone.skip.creation";
    private static final String ARQUILLIAN_DRONE_CREATION_PROPERTY_MSG = "The property "
        + ARQUILLIAN_DRONE_CREATION_PROPERTY
        + " is set to true - the Drone injection point won't be instantiated for the ";

    @Override
    public void enrich(Object testCase) {
        enrichTestClass(testCase.getClass(), testCase, false);
    }

    @Override
    public Object[] resolve(Method method) {
        DroneContext context = droneContext.get();
        DronePoint<?>[] dronePoints = InjectionPoints.parametersInMethod(droneContext.get(), method);
        Object[] resolution = new Object[dronePoints.length];

        if (droneInstantiationShouldBeSkipped()) {
            List<DronePoint<?>> points =
                Arrays.stream(dronePoints).filter(dronePoint -> dronePoint != null).collect(Collectors.toList());
            if (points.size() > 0) {
                log.info(ARQUILLIAN_DRONE_CREATION_PROPERTY_MSG + "method: " + method);
            }
        } else {
            for (int i = 0; i < dronePoints.length; i++) {
                DronePoint<?> dronePoint = dronePoints[i];
                if (dronePoint == null) {
                    resolution[i] = null;
                    continue;
                }

                if (!ensureInjectionPointPrepared(dronePoint, true)) {
                    // in case of deployment-scoped drone point tied to a deployment that isn't deployed, only register
                    // it - it will be injected when the deployment is finished
                    registerDeploymentDronePoint(dronePoint, method);
                    continue;
                }
                log.log(Level.FINE, "Injecting @Drone for method {0}, injection point {1}",
                    new Object[] {method.getName(), dronePoint}
                );

                Object drone = context.get(dronePoint).getInstance();
                Validate
                    .stateNotNull(drone, "Retrieved a null from Drone Context, which is not a valid Drone browser " +
                        "object" +
                        ".\nMethod: {0}, injection point: {1},", method.getName(), dronePoint);
                resolution[i] = drone;
            }
        }

        return resolution;
    }

    /**
     * Enriches the given test class with a drone instance and a context path. Injects existing instance into every field
     * annotated with {@link Drone}.
     *
     * @param testClass
     *     Test class to be enriched
     * @param testCase
     *     Instance of the test case (usually the class is same as the {@code testClass})
     * @param onlyStatic
     *     If the drone instance should be injected only into static fields
     */
    public void enrichTestClass(Class<?> testClass, Object testCase, boolean onlyStatic) {

        DroneContext context = droneContext.get();
        Map<Field, DronePoint<?>> injectionPoints = InjectionPoints.fieldsInClass(droneContext.get(),
            testClass);

        if (injectionPoints.size() > 0 && droneInstantiationShouldBeSkipped()) {
            log.info(ARQUILLIAN_DRONE_CREATION_PROPERTY_MSG + "field(s): " + injectionPoints.keySet());
            return;
        }

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

            if (!ensureInjectionPointPrepared(dronePoint, false)) {
                // in case of deployment-scoped drone point tied to a deployment that isn't deployed yet, only register
                // it - it will be injected when the deployment is finished
                registerDeploymentDronePoint(dronePoint, testCase);
                continue;
            }

            log.log(Level.FINE, "Injecting @Drone for field {0}, injection point {1}",
                new Object[] {dronePoint.getDroneType().getSimpleName(), dronePoint}
            );

            Object drone = context.get(dronePoint).getInstance();
            Validate.stateNotNull(drone, "Retrieved a null from Drone Context, " +
                    "which is not a valid Drone browser object. \nClass: {0}, field: {1}, injection point: {2}",
                testClass.getName(), field.getName(), dronePoint
            );
            SecurityActions.setFieldValue(testCase, field, drone);
        }
    }

    private boolean droneInstantiationShouldBeSkipped() {
        return Boolean.valueOf(SecurityActions.getProperty(ARQUILLIAN_DRONE_CREATION_PROPERTY));
    }

    private void registerDeploymentDronePoint(DronePoint dronePoint, Object testCase) {
        if (deploymentDronePointsRegistry.get() == null) {
            deploymentDronePointsRegistry.set(injector.get().inject(new DeploymentDronePointsRegistry()));
        }
        deploymentDronePointsRegistry.get().addDronePoint(dronePoint, testCase);
    }

    /**
     * Ensures whether the given drone point is prepared for injection
     *
     * @param dronePoint
     *     Drone point that should be checked
     * @param forMethod
     *     Whether the given drone point is used as a method parameter
     *
     * @return whether the injection point is prepared or not. {@code false} is returned in case of deployment-scoped
     * drone point used as a instance variable and tied to a deployment that has not been deployed yet
     */
    private boolean ensureInjectionPointPrepared(DronePoint<?> dronePoint, boolean forMethod) {
        if (!droneContext.get().get(dronePoint).hasFutureInstance()) {
            if (dronePoint.getLifecycle() != DronePoint.Lifecycle.DEPLOYMENT) {
                if (dronePoint.getLifecycle() == DronePoint.Lifecycle.CLASS) {
                    log.log(Level.WARNING, "Injection point {0} was not prepared yet. It will be prepared now, " +
                        "but it''s recommended that all drones with class lifecycle are prepared in " +
                        "@BeforeClass by the DroneLifecycleManager!", dronePoint);
                }

                prepareDroneCommand.fire(new PrepareDrone(dronePoint));
            } else {
                if (forMethod) {
                    throw new IllegalStateException(
                        MessageFormat
                            .format("Injection point {0} was not prepared yet. "
                                    + "It has deployment lifecycle and is used as a method parameter. "
                                    + "In this case the injection point has to be prepared before the method starts. "
                                    + "Please make sure that the deployment was already deployed before the method starts. "
                                    + "In case of the manual deployment inside of the test method, "
                                    + "use the injection point as an instance variable.",
                                dronePoint));
                }
                return false;
            }
        }
        return true;
    }
}
