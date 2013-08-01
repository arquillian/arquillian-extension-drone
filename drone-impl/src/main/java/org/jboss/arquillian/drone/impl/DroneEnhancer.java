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
import org.jboss.arquillian.drone.spi.DroneContext.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.Enhancer;
import org.jboss.arquillian.drone.spi.event.AfterDroneDeenhanced;
import org.jboss.arquillian.drone.spi.event.AfterDroneEnhanced;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDeenhanced;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDestroyed;
import org.jboss.arquillian.drone.spi.event.BeforeDroneEnhanced;

/**
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneEnhancer {

    private static final Logger log = Logger.getLogger(DroneEnhancer.class.getName());

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<BeforeDroneEnhanced> beforeDroneEnhanced;

    @Inject
    private Event<AfterDroneEnhanced> afterDroneEnhanced;

    @Inject
    private Event<BeforeDroneDeenhanced> beforeDroneDeenhanced;

    @Inject
    private Event<AfterDroneDeenhanced> afterDroneDeenhanced;

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

                beforeDroneEnhanced.fire(new BeforeDroneEnhanced(enhancer, browser, type, qualifier));
                Object newBrowser = enhancer.enhance(browser.asInstance(type), qualifier);
                browser.set(newBrowser);
                afterDroneEnhanced.fire(new AfterDroneEnhanced(browser, type, qualifier));
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

                beforeDroneDeenhanced.fire(new BeforeDroneDeenhanced(enhancer, browser, type, qualifier));
                Object newBrowser = enhancer.deenhance(browser.asInstance(type), qualifier);
                browser.set(newBrowser);
                afterDroneDeenhanced.fire(new AfterDroneDeenhanced(browser, type, qualifier));
            }
        }
    }

}
