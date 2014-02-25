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

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.CachingCallable;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.Filter;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of {@link DroneContext}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 */
public class DroneContextImpl implements DroneContext {

    private static final Logger LOGGER = Logger.getLogger(DroneContextImpl.class.getName());

    private Map<InjectionPoint<?>, DronePair<?, ?>> dronePairMap = new HashMap<InjectionPoint<?>, DronePair<?, ?>>();

    @Deprecated
    private DroneConfiguration<?> globalDroneConfiguration;

    @Inject
    private Instance<DroneExecutorService> executorService;

    @Inject
    private Event<BeforeDroneInstantiated> beforeDroneInstantiatedEvent;

    @Inject
    private Event<AfterDroneInstantiated> afterDroneInstantiatedEvent;

    @Override
    public <C extends DroneConfiguration<C>> C getGlobalDroneConfiguration(Class<C> configurationClass) {
        return (C) globalDroneConfiguration;
    }

    @Override
    public void setGlobalDroneConfiguration(DroneConfiguration<?> configuration) {
        globalDroneConfiguration = configuration;
    }

    @Override
    public <T> T getDrone(InjectionPoint<T> injectionPoint) throws IllegalStateException {
        DronePair<T, ?> pair = (DronePair<T, ?>) dronePairMap.get(injectionPoint);
        if (pair == null) {
            throw new IllegalArgumentException("Injection point doesn't exist!");
        }

        CachingCallable<T> droneCallable = pair.getDroneCallable();
        if (droneCallable == null) {
            throw new IllegalStateException("Drone callable not stored yet!");
        }

        boolean newInstance = !droneCallable.isValueCached();
        if (newInstance) {
            beforeDroneInstantiatedEvent.fire(new BeforeDroneInstantiated(droneCallable, injectionPoint));
        }

        T drone = instantiateDrone(droneCallable);

        if (newInstance) {
            afterDroneInstantiatedEvent.fire(new AfterDroneInstantiated(drone, injectionPoint));
        }

        CachingCallable<T> newDroneCallable = pair.getDroneCallable();

        if (newDroneCallable != droneCallable) {
            return getDrone(injectionPoint);
        } else {
            return drone;
        }
    }

    private <T> T instantiateDrone(CachingCallable<T> droneCallable) {
        // FIXME we need to make some kind of global drone configuration!

        int timeout = getGlobalDroneConfiguration(DroneCore.GlobalDroneConfiguration.class)
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
            Throwable cause = e.getCause();
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance within " + timeout + " "
                    + TimeUnit.SECONDS.toString().toLowerCase(), e);
        }
    }

    @Override
    public <C extends DroneConfiguration<C>> C getDroneConfiguration(InjectionPoint<?> injectionPoint, Class<C>
            configurationClass) throws IllegalArgumentException {
        DronePair<?, C> pair = (DronePair<?, C>) dronePairMap.get(injectionPoint);
        if (pair == null) {
            throw new IllegalArgumentException("Injection point doesn't exist!");
        }

        C configuration = pair.getConfiguration();
        if (configuration == null) {
            throw new IllegalStateException("Drone configuration not stored yet!");
        }

        return configuration;
    }

    @Override
    public <T> void storeFutureDrone(InjectionPoint<T> injectionPoint, CachingCallable<T> drone) {
        DronePair<T, ?> pair = (DronePair<T, ?>) dronePairMap.get(injectionPoint);
        if (pair == null) {
            throw new IllegalArgumentException("Injection point doesn't exist!");
        }

        if (pair.getDroneCallable() != null) {
            LOGGER.log(Level.FINE, "Future drone replaced at point: " + injectionPoint.toString());
        }

        pair.setDroneCallable(drone);
    }

    @Override
    public <T, C extends DroneConfiguration<C>> void storeDroneConfiguration(InjectionPoint<T> injectionPoint,
                                                                             C configuration) {
        DronePair<T, C> pair = (DronePair<T, C>) dronePairMap.get(injectionPoint);
        if (pair != null) {
            // FIXME shouldn't we just handle this peacefully with warning?
            throw new IllegalStateException("Injection point already exists!");
        }

        pair = new DronePair<T, C>();
        pair.setConfiguration(configuration);
        dronePairMap.put(injectionPoint, pair);
    }

    @Override
    public <T> boolean isDroneInstantiated(InjectionPoint<T> injectionPoint) {
        DronePair<T, ?> dronePair = (DronePair<T, ?>) dronePairMap.get(injectionPoint);
        if (dronePair == null) {
            return false;
        }
        if (dronePair.getDroneCallable() == null) {
            return false;
        }
        return dronePair.getDroneCallable().isValueCached();
    }

    @Override
    public <T> boolean isFutureDroneStored(InjectionPoint<T> injectionPoint) {
        DronePair<T, ?> dronePair = (DronePair<T, ?>) dronePairMap.get(injectionPoint);
        if (dronePair == null) {
            return false;
        }
        return dronePair.getDroneCallable() != null;
    }

    @Override
    public <T> boolean isDroneConfigurationStored(InjectionPoint<T> injectionPoint) {
        DronePair<T, ?> dronePair = (DronePair<T, ?>) dronePairMap.get(injectionPoint);
        if (dronePair == null) {
            return false;
        }
        return dronePair.getConfiguration() != null;
    }

    @Override
    public void removeDrone(InjectionPoint<?> injectionPoint) {
        DronePair<?, ?> pair = dronePairMap.get(injectionPoint);
        if (pair == null) {
            LOGGER.warning("Injection point doesn't exist!");
            return;
        }
        if (pair.getDroneCallable() == null) {
            LOGGER.warning("Couldn't remove drone, because it wasn't set!");
        }

        pair.setDroneCallable(null);
    }

    @Override
    public void removeDroneConfiguration(InjectionPoint<?> injectionPoint) {
        DronePair<?, ?> pair = dronePairMap.get(injectionPoint);
        if (pair == null) {
            LOGGER.warning("Injection point doesn't exist!");
            return;
        }
        if (pair.getDroneCallable() != null) {
            LOGGER.warning("Drone is still set, but you won't be able to access it anymore!");
        }
        if (pair.getConfiguration() == null) {
            LOGGER.warning("Couldn't remove configuration, because it wasn't set!");
        }

        pair.setConfiguration(null);

        dronePairMap.remove(injectionPoint);
    }

    @Override
    public void removeDrones(List<InjectionPoint<?>> injectionPoints) {
        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            removeDrone(injectionPoint);
        }
    }

    @Override
    public void removeDroneConfigurations(List<InjectionPoint<?>> injectionPoints) {
        for (InjectionPoint<?> injectionPoint : injectionPoints) {
            removeDroneConfiguration(injectionPoint);
        }
    }

    @Override
    public <T> InjectionPoint<? extends T> findSingle(Class<T> droneClass,
                                                      Filter... filters) throws IllegalStateException {
        List<InjectionPoint<? extends T>> injectionPoints = find(droneClass, filters);
        int count = injectionPoints.size();
        if (count != 1) {
            throw new IllegalStateException("Total injection points matched not equal to 1! Actual: " + count);
        }
        return injectionPoints.get(0);
    }

    @Override
    public <T> List<InjectionPoint<? extends T>> find(Class<T> droneClass, Filter... filters) {
        List<InjectionPoint<? extends T>> matchedInjectionPoints = new ArrayList<InjectionPoint<? extends T>>();

        for (InjectionPoint<?> injectionPoint : dronePairMap.keySet()) {
            if (!droneClass.isAssignableFrom(injectionPoint.getDroneType())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            InjectionPoint<? extends T> castInjectionPoint = (InjectionPoint<? extends T>) injectionPoint;

            boolean matches = true;

            for (Filter filter : filters) {
                if (!filter.accept(castInjectionPoint)) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                matchedInjectionPoints.add(castInjectionPoint);
            }
        }


        return matchedInjectionPoints;
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
