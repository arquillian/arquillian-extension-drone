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
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.command.DestroyDrone;
import org.jboss.arquillian.drone.spi.command.PrepareDrone;
import org.jboss.arquillian.drone.spi.event.AfterDroneExtensionConfigured;
import org.jboss.arquillian.drone.spi.event.BeforeDroneExtensionConfigured;
import org.jboss.arquillian.drone.spi.filter.DeploymentFilter;
import org.jboss.arquillian.drone.spi.filter.LifecycleFilter;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DroneLifecycleManager {

    @Inject
    private Instance<Injector> injector;

    @Inject
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    @Inject
    @ApplicationScoped
    private InstanceProducer<DroneContext> droneContext;

    @Inject
    private Event<BeforeDroneExtensionConfigured> beforeDroneExtensionConfiguredEvent;

    @Inject
    private Event<AfterDroneExtensionConfigured> afterDroneExtensionConfiguredEvent;

    @Inject
    private Event<PrepareDrone> createDroneConfigurationCommand;

    @Inject
    private Event<DestroyDrone> destroyDroneCommand;

    @SuppressWarnings("unused")
    public void beforeSuite(@Observes BeforeSuite event) {
        if (droneContext.get() != null) {
            // Drone extension is already configured
            return;
        }

        DroneContext context = injector.get().inject(new DroneContextImpl());
        droneContext.set(context);

        beforeDroneExtensionConfiguredEvent.fire(new BeforeDroneExtensionConfigured());

        if (context.getGlobalDroneConfiguration(DroneConfiguration.class) == null) {
            GlobalDroneConfiguration configuration =
                    new GlobalDroneConfiguration().configure(arquillianDescriptor.get(), null);
            context.setGlobalDroneConfiguration(configuration);
        }

        afterDroneExtensionConfiguredEvent.fire(new AfterDroneExtensionConfigured());
    }

    @SuppressWarnings("unused")
    public void beforeClass(@Observes BeforeClass event) {
        Class<?> testClass = event.getTestClass().getJavaClass();

        Set<InjectionPoint<?>> injectionPoints = InjectionPoints.allInClass(testClass);

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            if (injectionPoint.getLifecycle() == InjectionPoint.Lifecycle.METHOD) {
                continue;
            }

            createDroneConfigurationCommand.fire(new PrepareDrone(injectionPoint));
        }
    }

    public void before(@Observes Before event) {
        InjectionPoint<?>[] injectionPoints = InjectionPoints.parametersInMethod(event.getTestMethod());

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            if (injectionPoint == null || injectionPoint.getLifecycle() != InjectionPoint.Lifecycle.METHOD) {
                continue;
            }

            createDroneConfigurationCommand.fire(new PrepareDrone(injectionPoint));
        }
    }

    public void after(@Observes After event) {
        DroneContext context = droneContext.get();
        LifecycleFilter lifecycleFilter = new LifecycleFilter(InjectionPoint.Lifecycle.METHOD);
        List<InjectionPoint<?>> injectionPoints = context.find(Object.class, lifecycleFilter);

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            destroyDroneCommand.fire(new DestroyDrone(injectionPoint));
        }
    }

    public void beforeUndeploy(@Observes BeforeUnDeploy event) {
        DroneContext context = droneContext.get();
        DeploymentFilter deploymentFilter = new DeploymentFilter(Pattern.quote(event.getDeployment().getName()));
        List<InjectionPoint<?>> injectionPoints = context.find(Object.class, deploymentFilter);

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            destroyDroneCommand.fire(new DestroyDrone(injectionPoint));
        }
    }

    public void afterClass(@Observes AfterClass event) {
        DroneContext context = droneContext.get();

        LifecycleFilter lifecycleFilter = new LifecycleFilter(InjectionPoint.Lifecycle.CLASS,
                InjectionPoint.Lifecycle.METHOD);
        List<InjectionPoint<?>> injectionPoints = context.find(Object.class, lifecycleFilter);

        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            destroyDroneCommand.fire(new DestroyDrone(injectionPoint));
        }
    }

    /**
     * Global Drone configuration. Applicable to any Drone type
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     */
    @Deprecated
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


}
