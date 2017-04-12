/**
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.spi.command.PrepareDrone;
import org.jboss.arquillian.drone.spi.event.AfterDronePrepared;
import org.jboss.arquillian.drone.spi.event.BeforeDronePrepared;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * Creator of Drone configurations. Drone configuration is created either before class or before method,
 * depending on the scope
 * of Drone instance, based on data provided in arquillian.xml.
 * <p/>
 * <p>
 * Creates:
 * </p>
 * {@link DroneContext}
 * <p/>
 * <p>
 * Observes:
 * </p>
 * {@link BeforeClass} {@link Before}
 * <p/>
 * <p>
 * Fires:
 * </p>
 * {@link BeforeDronePrepared} {@link AfterDronePrepared}
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class DroneConfigurator {
    private static Logger logger = Logger.getLogger(DroneConfigurator.class.getName());

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    @Inject
    private Event<BeforeDronePrepared> beforeDronePreparedEvent;

    @Inject
    private Event<AfterDronePrepared> afterDronePreparedEvent;

    public void prepareDrone(@Observes PrepareDrone command, DroneRegistry registry) {
        DronePoint<?> dronePoint = command.getDronePoint();
        prepare(dronePoint, registry);
    }

    private <DRONE> void prepare(DronePoint<DRONE> dronePoint, DroneRegistry registry) {
        Validate.stateNotNull(droneContext.get(),
            "DroneContext should be available while working with method scoped instances");

        Configurator<DRONE, ?> droneConfigurator = getDroneConfigurator(registry, dronePoint);
        Instantiator callableInstantiator = getCallableInstantiator(registry, dronePoint);

        if (droneConfigurator != null || callableInstantiator != null) {
            beforeDronePreparedEvent.fire(new BeforeDronePrepared(droneConfigurator, callableInstantiator, dronePoint));
            performDronePreparation(dronePoint, droneConfigurator, callableInstantiator);
            afterDronePreparedEvent.fire(new AfterDronePrepared(dronePoint));
        }
    }

    private <DRONE> void performDronePreparation(final DronePoint<DRONE> dronePoint,
        Configurator<DRONE, ?> droneConfigurator, final Instantiator instantiator) {

        final DroneContext context = droneContext.get();
        final ArquillianDescriptor descriptor = arquillianDescriptor.get();
        Validate.stateNotNull(descriptor, "ArquillianDescriptor should not be null");

        // If nobody else provided the configuration, we have to do it
        DroneConfiguration configuration = droneConfigurator.createConfiguration(descriptor, dronePoint);
        context.get(dronePoint).setConfiguration(configuration);

        // create future instance
        CachingCallableImpl<DRONE> futureDrone = new CachingCallableImpl<DRONE>() {
            @Override
            protected DRONE createInstance() throws Exception {
                DroneConfiguration<?> configuration = context
                    .get(dronePoint)
                    .getConfigurationAs(DroneConfiguration.class);
                return (DRONE) instantiator.createInstance(configuration);
            }
        };

        context.get(dronePoint).setFutureInstance(futureDrone);
    }

    private <DRONE> Configurator<DRONE, ?> getDroneConfigurator(DroneRegistry registry, DronePoint<DRONE> dronePoint) {
        if (droneContext.get().get(dronePoint).hasConfiguration()) {
            logger.log(Level.WARNING, "Could not configure drone for injection point {0}, " +
                "because it was already configured!", dronePoint);
            return null;
        }
        return registry.getEntryFor(dronePoint.getDroneType(), Configurator.class);
    }

    private <DRONE> Instantiator getCallableInstantiator(DroneRegistry registry, final DronePoint<DRONE> dronePoint) {
        if (droneContext.get().get(dronePoint).hasFutureInstance()) {
            logger.log(Level.WARNING, "Could not create drone callable for injection point {0}, " +
                "because it was already created!", dronePoint);
            return null;
        }

        final Instantiator instantiator = registry.getEntryFor(dronePoint.getDroneType(), Instantiator.class);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", " +
                "with precedence " + instantiator.getPrecedence());
        }
        return instantiator;
    }
}
