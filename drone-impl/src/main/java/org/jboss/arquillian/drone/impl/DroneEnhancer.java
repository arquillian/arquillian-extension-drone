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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.Enhancer;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.event.AfterDroneDeenhanced;
import org.jboss.arquillian.drone.spi.event.AfterDroneEnhanced;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDeenhanced;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDestroyed;
import org.jboss.arquillian.drone.spi.event.BeforeDroneEnhanced;
import org.jboss.arquillian.drone.spi.event.DroneEnhancementEvent;

/**
 * Enhancer/deenhancer of Drone instance with {@link Enhancer} implementation available on the classpath.
 *
 * This allows to modify behavior of the instance easily
 *
 * <p>
 * Observes:
 * </p>
 * {@link AfterDroneInstantiated} {@link BeforeDroneDestroyed}
 *
 * <p>
 * Fires:
 * </p>
 * {@link BeforeDroneEnhanced} {@link AfterDroneEnhanced} {@link BeforeDroneDeenhanced} {@link AfterDroneDeenhanced}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneEnhancer {

    private static final Logger log = Logger.getLogger(DroneEnhancer.class.getName());

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<DroneEnhancementEvent> droneEnhancementEvent;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void enhanceDrone(@Observes AfterDroneInstantiated droneInstance, DroneContext context) {
        List<Enhancer> enhancers = new ArrayList<Enhancer>(serviceLoader.get().all(Enhancer.class));
        Collections.sort(enhancers, PrecedenceComparator.getInstance());

        InstanceOrCallableInstance browser = droneInstance.getInstance();
        final Class<?> type = droneInstance.getDroneType();
        final Class<? extends Annotation> qualifier = droneInstance.getQualifier();

        for (Enhancer enhancer : enhancers) {
            if (enhancer.canEnhance(type, qualifier)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Eenhancing using enhancer: " + enhancer.getClass().getName() + ", with precedence "
                            + enhancer.getPrecedence());
                }

                droneEnhancementEvent.fire(new BeforeDroneEnhanced(enhancer, browser, type, qualifier));
                Object newBrowser = enhancer.enhance(browser.asInstance(type), qualifier);
                browser.set(newBrowser);
                droneEnhancementEvent.fire(new AfterDroneEnhanced(browser, type, qualifier));
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void deenhanceDrone(@Observes BeforeDroneDestroyed droneInstance, DroneContext context) {

        List<Enhancer> enhancers = new ArrayList<Enhancer>(serviceLoader.get().all(Enhancer.class));
        // here we are deenhancing in reversed order
        Collections.sort(enhancers, PrecedenceComparator.getReversedOrder());

        InstanceOrCallableInstance browser = droneInstance.getInstance();
        final Class<?> type = droneInstance.getDroneType();
        final Class<? extends Annotation> qualifier = droneInstance.getQualifier();

        for (Enhancer enhancer : enhancers) {
            if (enhancer.canEnhance(type, qualifier)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Deenhancing using enhancer: " + enhancer.getClass().getName() + ", with precedence "
                            + enhancer.getPrecedence());
                }

                droneEnhancementEvent.fire(new BeforeDroneDeenhanced(enhancer, browser, type, qualifier));
                Object newBrowser = enhancer.deenhance(browser.asInstance(type), qualifier);
                browser.set(newBrowser);
                droneEnhancementEvent.fire(new AfterDroneDeenhanced(browser, type, qualifier));
            }
        }
    }

}
