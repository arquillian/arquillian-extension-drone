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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.spi.event.AfterDroneConfigured;
import org.jboss.arquillian.drone.spi.event.BeforeDroneConfigured;
import org.jboss.arquillian.drone.spi.event.DroneConfigurationEvent;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * Creator of Drone configurations. Drone configuration is created either before class or before method, depending on the scope
 * of Drone instance, based on data provided in arquillian.xml.
 *
 * <p>
 * Creates:
 * </p>
 * {@link DroneContext}
 *
 * <p>
 * Observes:
 * </p>
 * {@link BeforeClass} {@link Before}
 *
 * <p>
 * Fires:
 * </p>
 * {@link BeforeDroneConfigured} {@link AfterDroneConfigured}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneConfigurator {
    private static Logger log = Logger.getLogger(DroneConfigurator.class.getName());

    @Inject
    @ClassScoped
    private InstanceProducer<DroneContext> droneContext;

    @Inject
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    @Inject
    private Event<DroneConfigurationEvent> droneConfigurationEvent;

    public void prepareGlobalDroneConfiguration(@Observes(precedence = 10) BeforeClass event, DroneRegistry registry) {
        // create Drone Context
        droneContext.set(new DroneContextImpl());

        GlobalDroneFactory configurator = new GlobalDroneFactory();
        droneConfigurationEvent.fire(new BeforeDroneConfigured(configurator, GlobalDrone.class, Default.class));
        GlobalDroneConfiguration configuration = configurator.createConfiguration(arquillianDescriptor.get(), Default.class);
        InstanceOrCallableInstance droneConfiguration = new InstanceOrCallableInstanceImpl(configuration);
        droneContext.get().add(configuration.getClass(), Default.class, droneConfiguration);
        droneConfigurationEvent.fire(new AfterDroneConfigured(droneConfiguration, GlobalDrone.class, Default.class));
    }

    public void prepareDroneConfiguration(@Observes BeforeClass event, DroneRegistry registry) {

        // check if any field is @Drone annotated
        List<Field> fields = SecurityActions.getFieldsWithAnnotation(event.getTestClass().getJavaClass(), Drone.class);
        if (fields.isEmpty()) {
            return;
        }

        for (Field f : fields) {
            Class<?> droneType = f.getType();
            Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

            Validate.notNull(arquillianDescriptor.get(), "ArquillianDescriptor should not be null");
            Configurator<?, ?> configurator = registry.getEntryFor(droneType, Configurator.class);

            droneConfigurationEvent.fire(new BeforeDroneConfigured(configurator, droneType, qualifier));
            DroneConfiguration<?> configuration = configurator.createConfiguration(arquillianDescriptor.get(), qualifier);
            InstanceOrCallableInstance droneConfiguration = new InstanceOrCallableInstanceImpl(configuration);

            droneContext.get().add(configuration.getClass(), qualifier, droneConfiguration);
            droneConfigurationEvent.fire(new AfterDroneConfigured(droneConfiguration, droneType, qualifier));
        }

    }

    public void configureDrone(@Observes Before event, DroneRegistry registry) {
        Method method = event.getTestMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // check if any field is @Drone annotated
        if (parameterTypes.length == 0) {
            return;
        }

        Validate.stateNotNull(droneContext.get(), "DroneContext should be available while working with method scoped instances");

        for (int i = 0; i < parameterTypes.length; i++) {
            if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], Drone.class)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Resolving method " + method.getName() + " argument at position " + i);
                }

                Validate.notNull(arquillianDescriptor.get(), "ArquillianDescriptor should not be null");
                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations[i]);

                Configurator<?, ?> configurator = registry.getEntryFor(parameterTypes[i], Configurator.class);

                droneConfigurationEvent.fire(new BeforeDroneConfigured(configurator, parameterTypes[i], qualifier));
                DroneConfiguration<?> configuration = configurator.createConfiguration(arquillianDescriptor.get(), qualifier);
                InstanceOrCallableInstance droneConfiguration = new InstanceOrCallableInstanceImpl(configuration);
                droneContext.get().add(configuration.getClass(), qualifier, droneConfiguration);
                droneConfigurationEvent.fire(new AfterDroneConfigured(droneConfiguration, parameterTypes[i], qualifier));
            }
        }
    }

    /**
     * Global Drone configuration. Applicable to any Drone type
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     *
     */
    public static class GlobalDroneConfiguration implements DroneConfiguration<GlobalDroneConfiguration> {

        private static final String CONFIGURATION_NAME = "drone";

        private int instantiationTimeoutInSeconds = 5;

        @Override
        public String getConfigurationName() {
            return CONFIGURATION_NAME;
        }

        @SuppressWarnings("deprecation")
        @Override
        public GlobalDroneConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
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
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     *
     */
    public static class GlobalDroneFactory implements Configurator<GlobalDrone, GlobalDroneConfiguration>,
            Instantiator<GlobalDrone, GlobalDroneConfiguration>, Destructor<GlobalDrone> {
        @Override
        public GlobalDroneConfiguration createConfiguration(ArquillianDescriptor descriptor,
                Class<? extends Annotation> qualifier) {
            return new GlobalDroneConfiguration().configure(descriptor, qualifier);
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
     *
     */
    public static class GlobalDrone {
    }

}
