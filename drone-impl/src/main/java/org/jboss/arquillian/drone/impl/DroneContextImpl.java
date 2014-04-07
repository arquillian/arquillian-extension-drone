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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.jboss.arquillian.drone.spi.Filter;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;

/**
 * Default implementation of {@link DroneContext}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 */
public class DroneContextImpl implements DroneContext {
    private static final Logger LOGGER = Logger.getLogger(DroneContextImpl.class.getName());

    private final Map<DronePoint<?>, DronePair<?, ?>> dronePairMap;

    @Deprecated
    private DroneConfiguration<?> globalDroneConfiguration;

    @Inject
    private Instance<ExecutorService> executorService;

    @Inject
    private Event<BeforeDroneInstantiated> beforeDroneInstantiatedEvent;

    @Inject
    private Event<AfterDroneInstantiated> afterDroneInstantiatedEvent;

    public DroneContextImpl() {
        dronePairMap = new HashMap<DronePoint<?>, DronePair<?, ?>>();
    }

    @Override
    public <C extends DroneConfiguration<C>> C getGlobalDroneConfiguration(Class<C> configurationClass) {
        return (C) globalDroneConfiguration;
    }

    @Override
    public void setGlobalDroneConfiguration(DroneConfiguration<?> configuration) {
        globalDroneConfiguration = configuration;
    }

    @Override
    public <T> T getDrone(DronePoint<T> dronePoint) throws IllegalStateException {
        DronePair<T, ?> pair = (DronePair<T, ?>) dronePairMap.get(dronePoint);
        if (pair == null) {
            throw new IllegalArgumentException(MessageFormat.format("Injection point doesn''t exist: {0}", dronePoint));
        }

        CachingCallable<T> droneCallable = pair.getDroneCallable();
        if (droneCallable == null) {
            throw new IllegalStateException(MessageFormat.format("Drone callable not stored yet for injection point " +
                "{0}!", dronePoint));
        }

        boolean newInstance = !droneCallable.isValueCached();
        if (newInstance) {
            beforeDroneInstantiatedEvent.fire(new BeforeDroneInstantiated(dronePoint));
        }

        T drone = instantiateDrone(droneCallable);

        if (newInstance) {
            afterDroneInstantiatedEvent.fire(new AfterDroneInstantiated(drone, dronePoint));
        }

        CachingCallable<T> newDroneCallable = pair.getDroneCallable();

        if (newDroneCallable != droneCallable) {
            return getDrone(dronePoint);
        } else {
            return drone;
        }
    }

    private <T> T instantiateDrone(CachingCallable<T> droneCallable) {
        // FIXME we need to make some kind of global drone configuration!

        int timeout = getGlobalDroneConfiguration(DroneLifecycleManager.GlobalDroneConfiguration.class)
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

    @Override
    public <C extends DroneConfiguration<C>> C getDroneConfiguration(DronePoint<?> dronePoint, Class<C>
        configurationClass) throws IllegalArgumentException {
        DronePair<?, C> pair = (DronePair<?, C>) dronePairMap.get(dronePoint);
        if (pair == null) {
            throw new IllegalArgumentException(MessageFormat.format("Injection point doesn''t exist: {0}",
                    dronePoint));
        }

        C configuration = pair.getConfiguration();
        if (configuration == null) {
            throw new IllegalStateException(MessageFormat.format("Drone configuration not stored yet! Injection " +
                "point: {0}", dronePoint));
        }

        return configuration;
    }

    @Override
    public <T> void storeFutureDrone(DronePoint<T> dronePoint, CachingCallable<T> drone) {
        DronePair<T, ?> pair = (DronePair<T, ?>) dronePairMap.get(dronePoint);
        if (pair == null) {
            throw new IllegalArgumentException(MessageFormat.format("Injection point doesn''t exist: {0}",
                    dronePoint));
        }

        if (pair.getDroneCallable() != null) {
            LOGGER.log(Level.FINE, "Future drone replaced at point {0}", dronePoint);
        }

        pair.setDroneCallable(drone);
    }

    @Override
    public <T, C extends DroneConfiguration<C>> void storeDroneConfiguration(DronePoint<T> dronePoint,
        C configuration) {
        DronePair<T, C> pair = (DronePair<T, C>) dronePairMap.get(dronePoint);
        if (pair != null) {
            // FIXME shouldn't we just handle this peacefully with warning?
            throw new IllegalStateException(MessageFormat.format("Injection point already exists: {0}",
                    dronePoint));
        }

        pair = new DronePair<T, C>();
        pair.setConfiguration(configuration);
        dronePairMap.put(dronePoint, pair);
    }

    @Override
    public <T> boolean isDroneInstantiated(DronePoint<T> dronePoint) {
        DronePair<T, ?> dronePair = (DronePair<T, ?>) dronePairMap.get(dronePoint);
        if (dronePair == null) {
            return false;
        }
        if (dronePair.getDroneCallable() == null) {
            return false;
        }
        return dronePair.getDroneCallable().isValueCached();
    }

    @Override
    public <T> boolean isFutureDroneStored(DronePoint<T> dronePoint) {
        DronePair<T, ?> dronePair = (DronePair<T, ?>) dronePairMap.get(dronePoint);
        if (dronePair == null) {
            return false;
        }
        return dronePair.getDroneCallable() != null;
    }

    @Override
    public <T> boolean isDroneConfigurationStored(DronePoint<T> dronePoint) {
        DronePair<T, ?> dronePair = (DronePair<T, ?>) dronePairMap.get(dronePoint);
        if (dronePair == null) {
            return false;
        }
        return dronePair.getConfiguration() != null;
    }

    @Override
    public void removeDrone(DronePoint<?> dronePoint) {
        DronePair<?, ?> pair = dronePairMap.get(dronePoint);
        if (pair == null) {
            LOGGER.log(Level.WARNING, "Couldn''t remove Drone, because it wasn''t prepared! Injection point: {0}",
                    dronePoint);
            return;
        }
        if (pair.getDroneCallable() == null) {
            LOGGER.log(Level.WARNING, "Couldn''t remove Drone, because it wasn''t set! Injection point: {0}",
                    dronePoint);
        }

        pair.setDroneCallable(null);
    }

    @Override
    public void removeDroneConfiguration(DronePoint<?> dronePoint) {
        DronePair<?, ?> pair = dronePairMap.get(dronePoint);
        if (pair == null) {
            LOGGER.log(Level.WARNING, "Couldn''t remove configuration, because the injection point wasn''t prepared. " +
                "Injection point: {0}", dronePoint);
            return;
        }
        if (pair.getDroneCallable() != null) {
            LOGGER.log(Level.WARNING, "Drone is still set, but you won''t be able to access it anymore! Injection " +
                "point: {0}", dronePoint);
        }
        if (pair.getConfiguration() == null) {
            LOGGER.log(Level.WARNING, "Couldn''t remove configuration, because it wasn''t set! Injection point: {0}",
                    dronePoint);
        }

        pair.setConfiguration(null);

        dronePairMap.remove(dronePoint);
    }

    @Override
    public void removeDrones(List<DronePoint<?>> dronePoints) {
        for (DronePoint<?> dronePoint : dronePoints) {
            removeDrone(dronePoint);
        }
    }

    @Override
    public void removeDroneConfigurations(List<DronePoint<?>> dronePoints) {
        for (DronePoint<?> dronePoint : dronePoints) {
            removeDroneConfiguration(dronePoint);
        }
    }

    @Override
    public <T> DronePoint<? extends T> findSingle(Class<T> droneClass,
        Filter... filters) throws IllegalStateException {
        List<DronePoint<? extends T>> dronePoints = find(droneClass, filters);
        int count = dronePoints.size();
        if (count != 1) {
            throw new IllegalStateException("Total injection points matched not equal to 1! Actual: " + count);
        }
        return dronePoints.get(0);
    }

    @Override
    public <T> List<DronePoint<? extends T>> find(Class<T> droneClass, Filter... filters) {
        List<DronePoint<? extends T>> matchedDronePoints = new ArrayList<DronePoint<? extends T>>();

        for (DronePoint<?> dronePoint : dronePairMap.keySet()) {
            if (!droneClass.isAssignableFrom(dronePoint.getDroneType())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            DronePoint<? extends T> castDronePoint = (DronePoint<? extends T>) dronePoint;

            boolean matches = true;

            for (Filter filter : filters) {
                if (!filter.accept(castDronePoint)) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                matchedDronePoints.add(castDronePoint);
            }
        }

        return matchedDronePoints;
    }

    private class DronePair<T, C extends DroneConfiguration<C>> {
        private CachingCallable<T> droneCallable;
        private C configuration;

        public CachingCallable<T> getDroneCallable() {
            return droneCallable;
        }

        public void setDroneCallable(CachingCallable<T> droneCallable) {
            this.droneCallable = droneCallable;
        }

        public C getConfiguration() {
            return configuration;
        }

        public void setConfiguration(C configuration) {
            this.configuration = configuration;
        }
    }
}
