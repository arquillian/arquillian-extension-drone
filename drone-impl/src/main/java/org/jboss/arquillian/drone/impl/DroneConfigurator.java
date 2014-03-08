/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.CachingCallable;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.spi.command.PrepareDrone;
import org.jboss.arquillian.drone.spi.event.AfterDroneCallableCreated;
import org.jboss.arquillian.drone.spi.event.AfterDroneConfigured;
import org.jboss.arquillian.drone.spi.event.BeforeDroneCallableCreated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneConfigured;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * {@link BeforeDroneConfigured} {@link AfterDroneConfigured}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 */
public class DroneConfigurator {
    private static Logger logger = Logger.getLogger(DroneConfigurator.class.getName());

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    @Inject
    private Event<BeforeDroneConfigured> beforeDroneConfiguredEvent;

    @Inject
    private Event<AfterDroneConfigured> afterDroneConfiguredEvent;

    @Inject
    private Event<BeforeDroneCallableCreated> beforeDroneCallableCreatedEvent;

    @Inject
    private Event<AfterDroneCallableCreated> afterDroneCallableCreatedEvent;

    public void prepareDrone(@Observes PrepareDrone command, DroneRegistry registry) {
        InjectionPoint<?> injectionPoint = command.getInjectionPoint();

        configureDrone(registry, injectionPoint);

        createDroneCallable(registry, injectionPoint);
    }

    private <DRONE> void configureDrone(DroneRegistry registry, InjectionPoint<DRONE> injectionPoint) {
        ArquillianDescriptor descriptor = arquillianDescriptor.get();
        DroneContext context = droneContext.get();
        Validate.stateNotNull(descriptor, "ArquillianDescriptor should not be null");
        Validate.stateNotNull(context, "DroneContext should be available while working with method scoped instances");

        if (context.isDroneConfigurationStored(injectionPoint)) {
            logger.log(Level.WARNING, "Couldn''t configure drone for injection point {0}, " +
                    "because it was already configured!", injectionPoint);
            return;
        }

        Configurator<DRONE, ?> configurator = registry.getEntryFor(injectionPoint.getDroneType(), Configurator.class);

        beforeDroneConfiguredEvent.fire(new BeforeDroneConfigured(configurator, injectionPoint));

        DroneConfiguration configuration;
        // If nobody else provided the configuration
        if (!context.isDroneConfigurationStored(injectionPoint)) {
            configuration = configurator.createConfiguration(descriptor, injectionPoint);

            context.storeDroneConfiguration(injectionPoint, configuration);
        } else {
            configuration = context.getDroneConfiguration(injectionPoint, DroneConfiguration.class);
        }

        afterDroneConfiguredEvent.fire(new AfterDroneConfigured(configuration, injectionPoint));
    }

    private <DRONE> void createDroneCallable(DroneRegistry registry, final InjectionPoint<DRONE> injectionPoint) {
        final DroneContext context = droneContext.get();

        if (context.isFutureDroneStored(injectionPoint)) {
            logger.log(Level.WARNING, "Couldn''t create drone callable for injection point {0}, because it was already created!", injectionPoint);
            return;
        }

        final Instantiator instantiator = registry.getEntryFor(injectionPoint.getDroneType(), Instantiator.class);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", " +
                    "with precedence " + instantiator.getPrecedence());
        }

        beforeDroneCallableCreatedEvent.fire(new BeforeDroneCallableCreated(instantiator, injectionPoint));

        // create future instance
        CachingCallable futureDrone = new CachingCallableImpl<DRONE>() {
            @Override
            protected DRONE createInstance() throws Exception {
                DroneConfiguration<?> configuration = context.getDroneConfiguration(injectionPoint,
                        DroneConfiguration.class);
                return (DRONE) instantiator.createInstance(configuration);
            }
        };

        context.storeFutureDrone(injectionPoint, futureDrone);

        afterDroneCallableCreatedEvent.fire(new AfterDroneCallableCreated(injectionPoint));
    }

}
