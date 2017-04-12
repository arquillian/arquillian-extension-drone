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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.FilterableResult;
import org.jboss.arquillian.drone.spi.command.DestroyDrone;
import org.jboss.arquillian.drone.spi.command.PrepareDrone;
import org.jboss.arquillian.drone.spi.event.AfterDroneExtensionConfigured;
import org.jboss.arquillian.drone.spi.event.BeforeDroneExtensionConfigured;
import org.jboss.arquillian.drone.spi.filter.DeploymentFilter;
import org.jboss.arquillian.drone.spi.filter.LifecycleFilter;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

public class DroneLifecycleManager {
    public static final int CLASS_SCAN_PRECEDENCE = 75;
    private static final Logger log = Logger.getLogger(DroneLifecycleManager.class.getName());
    @Inject
    private Instance<Injector> injector;

    @Inject
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    @Inject
    @ApplicationScoped
    private InstanceProducer<DroneContext> droneContext;

    @Inject
    @ApplicationScoped
    private InstanceProducer<DeploymentDronePointsRegistry> deploymentDronePointsRegistry;

    @Inject
    private Event<BeforeDroneExtensionConfigured> beforeDroneExtensionConfiguredEvent;

    @Inject
    private Event<AfterDroneExtensionConfigured> afterDroneExtensionConfiguredEvent;

    @Inject
    private Event<PrepareDrone> createDroneConfigurationCommand;

    @Inject
    private Event<DestroyDrone> destroyDroneCommand;

    @Inject
    private Instance<TestClass> testClassInstance;

    public void managerStarted(@Observes ManagerStarted event) {
        try {
            DroneContext context = injector.get().inject(new DroneContextImpl());
            droneContext.set(context);
        } catch (TypeNotPresentException e) {
            log.log(Level.SEVERE,
                "Unable to create Drone Context due to missing services on classpath. Please make sure to use Arquillian Core 1.1.4.Final or later.");
            throw new IllegalStateException(
                "Unable to create Drone Context due to missing services on classpath. Please make sure to use Arquillian Core 1.1.4.Final or later.",
                e);
        }
    }

    public void configureDroneExtension(@Observes BeforeSuite event) {
        DroneContext context = droneContext.get();

        if (context.getGlobalDroneConfiguration(DroneConfiguration.class) != null) {
            return;
        }

        beforeDroneExtensionConfiguredEvent.fire(new BeforeDroneExtensionConfigured());

        if (context.getGlobalDroneConfiguration(DroneConfiguration.class) == null) {
            GlobalDroneConfiguration configuration =
                new GlobalDroneConfiguration().configure(arquillianDescriptor.get(), null);
            context.setGlobalDroneConfiguration(configuration);
        }

        afterDroneExtensionConfiguredEvent.fire(new AfterDroneExtensionConfigured());
    }

    @SuppressWarnings("unused")
    public void beforeClass(@Observes(precedence = CLASS_SCAN_PRECEDENCE) BeforeClass event) {
        Class<?> testClass = event.getTestClass().getJavaClass();
        Set<DronePoint<?>> dronePoints = InjectionPoints.allInClass(droneContext.get(), testClass);

        for (DronePoint<?> dronePoint : dronePoints) {

            // The deployment-scoped drones are only registered and not prepared - should be prepared in AfterDeploy
            if (dronePoint.getLifecycle() == DronePoint.Lifecycle.DEPLOYMENT) {
                registerDeploymentDronePoint(dronePoint);
                continue;
            }
            // We are not interested in method-scoped drones
            if (dronePoint.getLifecycle() == DronePoint.Lifecycle.METHOD) {
                continue;
            }

            createDroneConfigurationCommand.fire(new PrepareDrone(dronePoint));
        }
    }

    private void registerDeploymentDronePoint(DronePoint dronePoint) {
        if (deploymentDronePointsRegistry.get() == null) {
            deploymentDronePointsRegistry.set(injector.get().inject(new DeploymentDronePointsRegistry()));
        }
        deploymentDronePointsRegistry.get().addDronePoint(dronePoint, null);
    }

    public void before(@Observes Before event) {
        DronePoint<?>[] dronePoints = InjectionPoints.parametersInMethod(droneContext.get(), event.getTestMethod());

        for (DronePoint<?> dronePoint : dronePoints) {

            // We only need to prepare method-scoped drones - deployment-scoped drones should have been already prepared in AfterDeploy
            if (dronePoint == null || dronePoint.getLifecycle() != DronePoint.Lifecycle.METHOD) {
                continue;
            }
            createDroneConfigurationCommand.fire(new PrepareDrone(dronePoint));
        }
    }

    public void afterDeploy(@Observes AfterDeploy afterDeploy) {
        DeploymentDronePointsRegistry deplDronePoints = this.deploymentDronePointsRegistry.get();

        if (deplDronePoints != null) {
            String deplName = afterDeploy.getDeployment().getName();
            Map<DronePoint<?>, Object> filteredDronePoints = deplDronePoints.filterDeploymentDronePoints(deplName);

            for (DronePoint dronePoint : filteredDronePoints.keySet()) {
                if (!droneContext.get().get(dronePoint).hasFutureInstance()) {
                    createDroneConfigurationCommand.fire(new PrepareDrone(dronePoint));
                }

                // in case that deployment is done before the enrichment, then only prepare the DronePoint - Enrichment will be done later as a part of the standard process
                Object testClass = filteredDronePoints.get(dronePoint);
                if (testClass != null) {
                    // in case that deployment is done after the standard enrichment, we have to enrich the class manually
                    DroneTestEnricher droneTestEnricher = injector.get().inject(new DroneTestEnricher());
                    droneTestEnricher.enrichTestClass(testClassInstance.get().getJavaClass(), testClass, false);
                }
            }
        }
    }

    public void after(@Observes After event) {
        DroneContext context = droneContext.get();
        LifecycleFilter lifecycleFilter = new LifecycleFilter(DronePoint.Lifecycle.METHOD);
        FilterableResult<Object> dronePoints = context.find(Object.class).filter(lifecycleFilter);

        for (DronePoint<?> dronePoint : dronePoints) {
            destroyDroneCommand.fire(new DestroyDrone(dronePoint));
        }
    }

    public void beforeUndeploy(@Observes BeforeUnDeploy event) {
        DroneContext context = droneContext.get();
        DeploymentFilter deploymentFilter = new DeploymentFilter(Pattern.quote(event.getDeployment().getName()));
        LifecycleFilter lifecycleFilter = new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT);
        FilterableResult<Object> dronePoints = context.find(Object.class)
            .filter(deploymentFilter)
            .filter(lifecycleFilter);

        for (DronePoint<?> dronePoint : dronePoints) {
            destroyDroneCommand.fire(new DestroyDrone(dronePoint));
        }
    }

    public void afterClass(@Observes AfterClass event) {
        DroneContext context = droneContext.get();

        LifecycleFilter lifecycleFilter = new LifecycleFilter(DronePoint.Lifecycle.CLASS,
            DronePoint.Lifecycle.METHOD);
        FilterableResult<Object> dronePoints = context.find(Object.class).filter(lifecycleFilter);

        for (DronePoint<?> dronePoint : dronePoints) {
            destroyDroneCommand.fire(new DestroyDrone(dronePoint));
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

        @Override
        public GlobalDroneConfiguration configure(ArquillianDescriptor descriptor,
            Class<? extends Annotation> qualifier) {
            // qualifier is ignored
            ConfigurationMapper.fromArquillianDescriptor(descriptor, this, Default.class);

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
