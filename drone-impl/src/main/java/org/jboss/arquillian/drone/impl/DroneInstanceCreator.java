/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.impl.DroneConfigurator.GlobalDroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.DroneLifecycleEvent;

/**
 * Transformer of callables into real instances. Uses current thread to invoke {@link Callable} that defines Drone instance.
 *
 *
 * <p>
 * Observes:
 * </p>
 * {@link BeforeDroneInstantiated}
 *
 * <p>
 * Fires:
 * </p>
 * {@link AfterDroneInstantiated}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneInstanceCreator {

    // this executor will run callables in the same thread as caller
    // we need this in order to allow better Drone based code debugging
    private static final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

    @Inject
    private Instance<DroneContext> context;

    @Inject
    private Event<DroneLifecycleEvent> droneLifecycleEvent;

    public void createDroneInstance(@Observes(precedence = Integer.MAX_VALUE) BeforeDroneInstantiated event) {

        InstanceOrCallableInstance union = event.getInstanceCallable();
        Class<?> droneType = event.getDroneType();
        Class<? extends Annotation> qualifier = event.getQualifier();

        InstanceOrCallableInstance globalConfigurationUnion = context.get().get(GlobalDroneConfiguration.class, Default.class);
        Validate.stateNotNull(globalConfigurationUnion, "Drone global configuration should be available in the context");
        GlobalDroneConfiguration globalDroneConfiguration = globalConfigurationUnion.asInstance(GlobalDroneConfiguration.class);
        int timeout = globalDroneConfiguration.getInstantiationTimeoutInSeconds();

        try {
            Object browser = null;
            if (timeout > 0) {
                browser = executorService.submit(union.asCallableInstance(droneType)).get(timeout, TimeUnit.SECONDS);
            }
            // here we ignore the timeout, for instance if debugging is enabled
            else {
                browser = executorService.submit(union.asCallableInstance(droneType)).get();
            }
            union.set(browser);
            droneLifecycleEvent.fire(new AfterDroneInstantiated(union, droneType, qualifier));
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance, thread interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance within " + timeout + " "
                    + TimeUnit.SECONDS.toString().toLowerCase(), e);
        }
    }
}
