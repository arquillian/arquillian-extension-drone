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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.threading.ExecutorService;
import org.jboss.arquillian.drone.spi.CachingCallable;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointContext;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;

public class DronePointContextImpl<DRONE> implements DronePointContext<DRONE> {
    private static final Logger LOGGER = Logger.getLogger(DronePointContextImpl.class.getName());

    private final DronePoint<DRONE> dronePoint;
    private final Map<Class<? extends MetadataKey<?>>, Object> metadataMap;

    private CachingCallable<DRONE> futureInstance;
    private DroneConfiguration<?> configuration;

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Instance<ExecutorService> executorService;

    @Inject
    private Event<BeforeDroneInstantiated> beforeDroneInstantiatedEvent;

    @Inject
    private Event<AfterDroneInstantiated> afterDroneInstantiatedEvent;

    public DronePointContextImpl(DronePoint<DRONE> dronePoint) {
        this.dronePoint = dronePoint;
        metadataMap = new HashMap<Class<? extends MetadataKey<?>>, Object>();
    }

    @Override
    public DronePoint<DRONE> getDronePoint() {
        return dronePoint;
    }

    @Override
    public DRONE getInstance() throws IllegalStateException {
        final CachingCallable<DRONE> futureInstance = this.futureInstance;
        if (futureInstance == null) {
            throw new IllegalStateException(MessageFormat.format("Future instance callable is not stored for drone " +
                "point {0}!", dronePoint));
        }

        boolean newInstance = !futureInstance.isValueCached();
        if (newInstance) {
            beforeDroneInstantiatedEvent.fire(new BeforeDroneInstantiated(dronePoint));
        }

        DRONE drone = instantiateDrone(futureInstance);

        if (newInstance) {
            afterDroneInstantiatedEvent.fire(new AfterDroneInstantiated(dronePoint));
        }

        // If someone sets new future instance, we need to do another round
        if (futureInstance != this.futureInstance) {
            return getInstance();
        } else {
            return drone;
        }
    }

    @Override
    public <CAST_DRONE> CAST_DRONE getInstanceAs(Class<CAST_DRONE> droneClass) throws IllegalArgumentException,
        IllegalStateException {
        Validate.notNull(droneClass, "Given drone class cannot be null!");

        if (!dronePoint.conformsTo(droneClass)) {
            throw new IllegalStateException(MessageFormat.format("Could not cast instance from {0} to {1}!",
                dronePoint.getDroneType().getName(), droneClass.getName()));
        }

        return droneClass.cast(getInstance());
    }

    @Override
    public <CONF extends DroneConfiguration<CONF>> CONF getConfigurationAs(Class<CONF> configurationClass) throws
        IllegalArgumentException, IllegalStateException {
        Validate.notNull(configurationClass, "Given configuration class cannot be null!");

        if (configuration == null) {
            throw new IllegalStateException(MessageFormat.format("Configuration is not set for drone point {0}!",
                dronePoint));
        }

        if (!configurationClass.isAssignableFrom(configuration.getClass())) {
            throw new IllegalStateException(MessageFormat.format("Could not cast configuration from {0} to {1}!",
                configuration.getClass().getName(), configurationClass.getName()));
        }

        return configurationClass.cast(configuration);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <KEY extends MetadataKey<VALUE>, VALUE> VALUE getMetadata(Class<KEY> keyClass) throws
        IllegalArgumentException {
        Validate.notNull(keyClass, "Given key class canoot be null!");

        return (VALUE) metadataMap.get(keyClass);
    }

    @Override
    public boolean isInstantiated() {
        return hasFutureInstance() && futureInstance.isValueCached();
    }

    @Override
    public boolean hasFutureInstance() {
        return futureInstance != null;
    }

    @Override
    public boolean hasConfiguration() {
        return configuration != null;
    }

    @Override
    public <KEY extends MetadataKey<VALUE>, VALUE> boolean hasMetadata(Class<KEY> keyClass) {
        Validate.notNull(keyClass, "Given key class canoot be null!");

        return metadataMap.containsKey(keyClass);
    }

    @Override
    public void setFutureInstance(CachingCallable<DRONE> futureInstance) {
        if (hasFutureInstance()) {
            LOGGER.log(Level.FINE, "Future instance was previously set for drone point {0}, replacing.",
                dronePoint);
        }
        this.futureInstance = futureInstance;
    }

    @Override
    public <CONF extends DroneConfiguration<CONF>> void setConfiguration(CONF configuration) {
        if (hasConfiguration()) {
            LOGGER.log(Level.FINE, "Configuration was previously set for drone point {0}, replacing.");
        }
        this.configuration = configuration;
    }

    @Override
    public <KEY extends MetadataKey<VALUE>, VALUE> void setMetadata(Class<KEY> keyClass, VALUE metadata) {
        Validate.notNull(keyClass, "Given key class cannot be null!");

        metadataMap.put(keyClass, metadata);
    }

    @Override
    public void removeFutureInstance() {
        if (!hasFutureInstance()) {
            LOGGER.log(Level.WARNING, "Could not remove future instance, because it was not set! Drone point: {0}.",
                dronePoint);
        }
        this.futureInstance = null;
    }

    @Override
    public void removeConfiguration() {
        if (!hasConfiguration()) {
            LOGGER.log(Level.WARNING, "Could not remove configuration, because it was not set! Drone point: {0}.",
                dronePoint);
        }
        this.configuration = null;
    }

    @Override
    public <KEY extends MetadataKey<VALUE>, VALUE> void removeMetadata(Class<KEY> keyClass) {
        Validate.notNull(keyClass, "Given key class canoot be null!");

        metadataMap.remove(keyClass);
    }

    private <T> T instantiateDrone(CachingCallable<T> droneCallable) {
        // FIXME we need to make some kind of global drone configuration!

        int timeout = droneContext.get().getGlobalDroneConfiguration(DroneLifecycleManager.GlobalDroneConfiguration
            .class)
            .getInstantiationTimeoutInSeconds();

        try {
            T drone;
            Future<T> futureDrone = executorService.get().submit(droneCallable);
            if (timeout > 0) {
                drone = futureDrone.get(timeout, TimeUnit.SECONDS);
            }
            // here we ignore the timeout, for instance if debugging is enabled
            else {
                drone = futureDrone.get();
            }
            return drone;
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance, thread interrupted", e);
        } catch (ExecutionException e) {
            // make exception a bit nicer
            Throwable cause = e.getCause();
            if (DroneTimeoutException.isCausedByTimeoutException(cause)) {
                throw new DroneTimeoutException(timeout, cause);
            }
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new DroneTimeoutException(timeout, e);
        }
    }
}
