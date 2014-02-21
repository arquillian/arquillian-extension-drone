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

import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.event.AfterDroneDestroyed;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDestroyed;
import org.jboss.arquillian.drone.spi.event.DroneLifecycleEvent;
import org.jboss.arquillian.drone.spi.filter.DeploymentFilter;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Destructor of Drone instance. Disposes both class scoped Drones as well as method scoped ones.
 * <p/>
 * <p>
 * Observes:
 * </p>
 * {@link AfterClass} {@link After}
 * <p/>
 * <p>
 * Fires:
 * </p>
 * {@link BeforeDroneDestroyed} {@link AfterDroneDestroyed}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 */
public class DroneDestructor {
    private static final Logger log = Logger.getLogger(DroneDestructor.class.getName());

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<DroneRegistry> registry;

    @Inject
    private Event<DroneLifecycleEvent> droneLifecycleEvent;

    @Inject
    private Instance<DroneContext> droneContext;

    public void destroyMethodScopedDrones(@Observes After event, DroneContext context) {

        Method method = event.getTestMethod();
        InjectionPoint<?>[] injectionPoints = InjectionPoints.parametersInMethod(method);

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            if (injectionPoint == null) {
                continue;
            }
            if (injectionPoint.getScope() != InjectionPoint.Scope.METHOD) {
                continue;
            }

            if (!context.isDroneInstantiated(injectionPoint)) {
                continue;
            }

            Destructor destructor = getDestructorFor(injectionPoint.getDroneType());

            Object drone = context.getDrone(injectionPoint);
            droneLifecycleEvent.fire(new BeforeDroneDestroyed(drone, injectionPoint));

            destructor.destroyInstance(drone);
            context.removeDrone(injectionPoint);

            droneLifecycleEvent.fire(new AfterDroneDestroyed(injectionPoint));
        }

    }

    public void destroyDeploymentDrones(@Observes BeforeUnDeploy event) {
        DroneContext context = droneContext.get();
        DeploymentFilter deploymentFilter = new DeploymentFilter(Pattern.quote(event.getDeployment().getName()));
        List<InjectionPoint<?>> injectionPoints = context.find(Object.class, deploymentFilter);

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            destroyDrone(injectionPoint);
        }

    }

    public void destroyClassDrones(@Observes AfterClass event) {
        Class<?> cls = event.getTestClass().getJavaClass();
        List<InjectionPoint<?>> injectionPoints = InjectionPoints.allInClass(cls);

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            /* FIXME add this after suite scoped drones are added
            if(injectionPoint.getScope() == SUITE) {

                continue;
            }*/
            destroyDrone(injectionPoint);
        }
    }

    private void destroyDrone(InjectionPoint<?> injectionPoint) {
        DroneContext context = droneContext.get();

        if (context.isDroneInstantiated(injectionPoint)) {
            Destructor destructor = getDestructorFor(injectionPoint.getDroneType());

            Object drone = context.getDrone(injectionPoint);
            droneLifecycleEvent.fire(new BeforeDroneDestroyed(drone, injectionPoint));
            destructor.destroyInstance(drone);
            droneLifecycleEvent.fire(new AfterDroneDestroyed(injectionPoint));
        }
        if (context.isFutureDroneStored(injectionPoint)) {
            context.removeDrone(injectionPoint);
        }
        if (context.isDroneConfigurationStored(injectionPoint)) {
            context.removeDroneConfiguration(injectionPoint);
        }
    }

    @SuppressWarnings("rawtypes")
    private Destructor getDestructorFor(Class<?> typeClass) {
        // must be defined as raw because instance type to be destroyer cannot
        // be determined in compile time
        Destructor destructor = registry.get().getEntryFor(typeClass, Destructor.class);

        if (log.isLoggable(Level.FINER)) {
            // FIXME possible nullpointerexception?
            log.fine("Using destructor defined in class: " + destructor.getClass().getName() + ", with precedence "
                    + destructor.getPrecedence());
        }

        return destructor;
    }
}
