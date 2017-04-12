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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.spi.CachingCallable;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneInstanceEnhancer;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.event.AfterDroneDeenhanced;
import org.jboss.arquillian.drone.spi.event.AfterDroneEnhanced;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDeenhanced;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDestroyed;
import org.jboss.arquillian.drone.spi.event.BeforeDroneEnhanced;
import org.jboss.arquillian.drone.spi.event.DroneEnhancementEvent;

/**
 * DroneInstanceEnhancer/deenhancer of Drone instance with {@link DroneInstanceEnhancer} implementation available on the
 * classpath.
 * <p/>
 * This allows to modify behavior of the instance easily
 * <p/>
 * <p>
 * Observes:
 * </p>
 * {@link AfterDroneInstantiated} {@link BeforeDroneDestroyed}
 * <p/>
 * <p>
 * Fires:
 * </p>
 * {@link BeforeDroneEnhanced} {@link AfterDroneEnhanced} {@link BeforeDroneDeenhanced} {@link AfterDroneDeenhanced}
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class DroneEnhancer {

    private static final Logger log = Logger.getLogger(DroneEnhancer.class.getName());

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<DroneEnhancementEvent> droneEnhancementEvent;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> void enhanceDrone(@Observes AfterDroneInstantiated event, DroneContext context) {

        List<DroneInstanceEnhancer> enhancers = new ArrayList<DroneInstanceEnhancer>(serviceLoader.get().all(
            DroneInstanceEnhancer.class));

        Collections.sort(enhancers, PrecedenceComparator.getInstance());

        DronePoint<T> dronePoint = (DronePoint<T>) event.getDronePoint();
        T drone = context.get(dronePoint).getInstance();

        for (DroneInstanceEnhancer<?> enhancer : enhancers) {

            InstanceOrCallableInstance instanceOrCallableInstance = new CompatibilityInstanceOrCallableInstance(drone);

            if (enhancer.canEnhance(instanceOrCallableInstance, dronePoint.getDroneType(), dronePoint.getQualifier())) {
                log.log(Level.FINE,
                    "Enhancing Drone {0} using enhancer {2} with precedence {3}",
                    new Object[] {dronePoint, enhancer.getClass().getName(), enhancer.getPrecedence()});

                droneEnhancementEvent.fire(new BeforeDroneEnhanced(enhancer, dronePoint));
                DroneInstanceEnhancer<T> supportedEnhancer = (DroneInstanceEnhancer<T>) enhancer;
                final T enhancedDrone = supportedEnhancer.enhance(drone, dronePoint.getQualifier());
                if (enhancedDrone == null) {
                    throw new IllegalStateException("Enhanced drone cannot be null!");
                }
                if (enhancedDrone != drone) {
                    context.get(dronePoint).setFutureInstance(new ConstantValueCachingCallable<T>(enhancedDrone));
                    drone = enhancedDrone;
                }
                droneEnhancementEvent.fire(new AfterDroneEnhanced(dronePoint));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> void deenhanceDrone(@Observes BeforeDroneDestroyed event, DroneContext context) {

        List<DroneInstanceEnhancer> enhancers = new ArrayList<DroneInstanceEnhancer>(serviceLoader.get().all(
            DroneInstanceEnhancer.class));

        // here we are deenhancing in reversed order
        Collections.sort(enhancers, PrecedenceComparator.getReversedOrder());

        DronePoint<T> dronePoint = (DronePoint<T>) event.getDronePoint();
        T drone = context.get(dronePoint).getInstance();

        for (DroneInstanceEnhancer<?> enhancer : enhancers) {

            InstanceOrCallableInstance instanceOrCallableInstance = new CompatibilityInstanceOrCallableInstance(drone);

            if (enhancer.canEnhance(instanceOrCallableInstance, dronePoint.getDroneType(), dronePoint.getQualifier())) {
                log.log(Level.FINER,
                    "Deenhancing {0} using enhancer {1} with precedence {2}",
                    new Object[] {dronePoint, enhancer.getClass().getName(), enhancer.getPrecedence()});

                droneEnhancementEvent.fire(new BeforeDroneDeenhanced(enhancer, dronePoint));
                DroneInstanceEnhancer<T> supportedEnhancer = (DroneInstanceEnhancer<T>) enhancer;
                T deenhancedDrone = supportedEnhancer.deenhance(drone, dronePoint.getQualifier());
                if (deenhancedDrone == null) {
                    throw new IllegalStateException("Deenahnced drone cannot be null!");
                }
                if (deenhancedDrone != drone) {
                    context.get(dronePoint).setFutureInstance(new ConstantValueCachingCallable<T>(deenhancedDrone));
                    drone = deenhancedDrone;
                }
                droneEnhancementEvent.fire(new AfterDroneDeenhanced(dronePoint));
            }
        }
    }

    private class ConstantValueCachingCallable<V> implements CachingCallable<V> {

        private final V value;

        ConstantValueCachingCallable(V value) {
            this.value = value;
        }

        @Override
        public boolean isValueCached() {
            return true;
        }

        @Override
        public V call() throws Exception {
            return value;
        }
    }

    private class CompatibilityInstanceOrCallableInstance implements InstanceOrCallableInstance {

        private final Object value;

        CompatibilityInstanceOrCallableInstance(Object value) {
            this.value = value;
        }

        @Override
        public InstanceOrCallableInstance set(Object value) throws IllegalArgumentException {
            throw new UnsupportedOperationException("CompatibilityInstanceOrCallableInstance is read-only!");
        }

        @Override
        public boolean isInstance() {
            return true;
        }

        @Override
        public boolean isInstanceCallable() {
            return false;
        }

        @Override
        public <T> T asInstance(Class<T> type) throws IllegalStateException {
            return (T) value;
        }

        @Override
        public <T> Callable<T> asCallableInstance(Class<T> type) throws IllegalStateException {
            throw new UnsupportedOperationException("CompatibilityInstanceOrCallableInstance only provides instance!");
        }
    }
}
