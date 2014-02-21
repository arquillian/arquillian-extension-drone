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
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.CachingCallable;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.spi.event.AfterDroneCallableCreated;
import org.jboss.arquillian.drone.spi.event.AfterDroneConfigured;
import org.jboss.arquillian.drone.spi.event.AfterDroneExtensionConfigured;
import org.jboss.arquillian.drone.spi.event.BeforeDroneCallableCreated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneConfigured;
import org.jboss.arquillian.drone.spi.event.BeforeDroneExtensionConfigured;
import org.jboss.arquillian.drone.spi.event.DroneConfigurationEvent;
import org.jboss.arquillian.drone.spi.event.DroneLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import java.lang.annotation.Annotation;
import java.util.List;
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
    private static Logger log = Logger.getLogger(DroneConfigurator.class.getName());

    @Inject
    @ApplicationScoped
    private InstanceProducer<DroneContext> droneContext;

    @Inject
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    @Inject
    private Event<DroneConfigurationEvent> droneConfigurationEvent;

    @Inject
    private Event<DroneLifecycleEvent> droneLifecycleEvent;

    @Inject
    private Event<BeforeDroneExtensionConfigured> beforeDroneExtensionConfiguredEvent;

    @Inject
    private Event<AfterDroneExtensionConfigured> afterDroneExtensionConfiguredEvent;

    @Inject
    private Instance<Injector> injector;

    public void prepareDroneContext(@Observes(precedence = 10) BeforeSuite event) {
        if (droneContext.get() != null) {
            // Drone extension is already configured
            return;
        }

        DroneContext context = injector.get().inject(new DroneContextImpl());
        droneContext.set(context);

        beforeDroneExtensionConfiguredEvent.fire(new BeforeDroneExtensionConfigured());

        if (context.getGlobalDroneConfiguration(DroneConfiguration.class) == null) {
            GlobalDroneFactory configurator = new GlobalDroneFactory();
            GlobalDroneConfiguration configuration = configurator.createConfiguration(arquillianDescriptor.get(), null);
            context.setGlobalDroneConfiguration(configuration);
        }

        afterDroneExtensionConfiguredEvent.fire(new AfterDroneExtensionConfigured());
    }

    public void prepareDrones(@Observes BeforeClass event, DroneRegistry registry) {
        DroneContext context = droneContext.get();

        Class<?> testClass = event.getTestClass().getJavaClass();

        List<InjectionPoint<?>> injectionPoints = InjectionPoints.allInClass(testClass);

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            if (context.isDroneConfigurationStored(injectionPoint) && context.isFutureDroneStored(injectionPoint)) {
                continue;
            }

            configureDrone(registry, injectionPoint);

            createDroneCallable(injectionPoint, registry);
        }
    }

    private <T> DroneConfiguration<?> configureDrone(DroneRegistry registry, InjectionPoint<T> injectionPoint) {
        ArquillianDescriptor descriptor = arquillianDescriptor.get();
        DroneContext context = droneContext.get();
        Validate.stateNotNull(descriptor, "ArquillianDescriptor should not be null");
        Validate.stateNotNull(context, "DroneContext should be available while working with method scoped instances");

        Configurator<T, ?> configurator = registry.getEntryFor(injectionPoint.getDroneType(), Configurator.class);

        droneConfigurationEvent.fire(new BeforeDroneConfigured(configurator, injectionPoint));

        DroneConfiguration configuration;
        // If nobody else provided the configuration
        if (!context.isDroneConfigurationStored(injectionPoint)) {
            configuration = configurator.createConfiguration(descriptor, injectionPoint);

            context.storeDroneConfiguration(injectionPoint, configuration);
        } else {
            configuration = context.getDroneConfiguration(injectionPoint, DroneConfiguration.class);
        }

        droneConfigurationEvent.fire(new AfterDroneConfigured(configuration, injectionPoint));

        return configuration;
    }

    private <DRONE> CachingCallable<DRONE>
    createDroneCallable(final InjectionPoint<DRONE> injectionPoint, DroneRegistry registry) {

        final DroneContext context = droneContext.get();

        final Instantiator instantiator = registry.getEntryFor(injectionPoint.getDroneType(), Instantiator.class);
        if (log.isLoggable(Level.FINE)) {
            log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence "
                    + instantiator.getPrecedence());
        }

        droneLifecycleEvent.fire(new BeforeDroneCallableCreated(instantiator, injectionPoint));

        // create future instance
        CachingCallable<DRONE> futureDrone = new CachingCallableImpl<DRONE>() {
            @Override
            public DRONE createInstance() throws Exception {
                DroneConfiguration<?> configuration = context.getDroneConfiguration(injectionPoint,
                        DroneConfiguration.class);

                return (DRONE) instantiator.createInstance(configuration);
            }
        };

        context.storeFutureDrone(injectionPoint, futureDrone);

        droneLifecycleEvent.fire(new AfterDroneCallableCreated(injectionPoint));

        return futureDrone;
    }

    /**
     * Global Drone configuration. Applicable to any Drone type
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     */
    public static class GlobalDroneConfiguration implements DroneConfiguration<GlobalDroneConfiguration> {

        public static final String CONFIGURATION_NAME = "drone";

        public static final int DEFAULT_INSTANTIATION_TIMEOUT = 60;

        private int instantiationTimeoutInSeconds = DEFAULT_INSTANTIATION_TIMEOUT;

        @Override
        public String getConfigurationName() {
            return CONFIGURATION_NAME;
        }

        @SuppressWarnings("deprecation")
        @Override
        public GlobalDroneConfiguration configure(ArquillianDescriptor descriptor,
                                                  Class<? extends Annotation> qualifier) {
            // qualifier is ignored
            ConfigurationMapper.fromArquillianDescriptor(descriptor, this, Default.class);
            ConfigurationMapper.fromSystemConfiguration(this, Default.class);

            // if debugging is enabled
            if (Boolean.parseBoolean(SecurityActions.getProperty("arquillian.debug"))) {
                this.instantiationTimeoutInSeconds = 0;
            }

            return this;
        }

        public int getInstantiationTimeoutInSeconds() {
            return instantiationTimeoutInSeconds;
        }

        public void setInstantiationTimeoutInSeconds(int instantiationTimeoutInSeconds) {
            this.instantiationTimeoutInSeconds = instantiationTimeoutInSeconds;
        }
    }

    /**
     * Global Drone configuration creator
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     */
    public static class GlobalDroneFactory implements Configurator<GlobalDrone, GlobalDroneConfiguration>,
            Instantiator<GlobalDrone, GlobalDroneConfiguration>, Destructor<GlobalDrone> {
        @Override
        public GlobalDroneConfiguration createConfiguration(ArquillianDescriptor descriptor,
                                                            InjectionPoint<GlobalDrone> injectionPoint) {
            return new GlobalDroneConfiguration().configure(descriptor, null);
        }

        @Override
        public GlobalDrone createInstance(GlobalDroneConfiguration configuration) {
            return new GlobalDrone();
        }

        @Override
        public void destroyInstance(GlobalDrone instance) {

        }

        @Override
        public int getPrecedence() {
            return 0;
        }
    }

    /**
     * This is a virtual representation of global Drone browser. This way we allow extension creators to intercept
     * configuration the very same way as any other configuration.
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     */
    public static class GlobalDrone {
    }

}
