package org.jboss.arquillian.drone.impl;

import java.lang.annotation.Annotation;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneContext.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * Transformer of callables into real instances. Uses current thread to invoke {@see Callable} that defines Drone instance
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneInstanceCreator {

    // this executor will run callables in the same thread as caller
    // we need this in order to allow better Drone based code debugging
    private static final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

    @Inject
    @SuiteScoped
    private InstanceProducer<DroneInstanceCreator> droneInstanceCreator;

    @Inject
    private Instance<DroneContext> context;

    @Inject
    private Event<BeforeDroneInstantiated> beforeDroneInstantiated;

    @Inject
    private Event<AfterDroneInstantiated> afterDroneInstantiated;

    public void configureDroneServices(@Observes BeforeSuite event) {
        // create Drone Instance Creator Service
        droneInstanceCreator.set(this);
    }

    public Object createDroneInstance(InstanceOrCallableInstance union, Class<?> droneType,
            Class<? extends Annotation> qualifier, long timeout, TimeUnit timeoutTimeUnit) {
        try {
            beforeDroneInstantiated.fire(new BeforeDroneInstantiated(union, droneType, qualifier));
            Object browser = executorService.submit(union.asCallableInstance(droneType)).get(timeout, timeoutTimeUnit);
            union.set(browser);
            afterDroneInstantiated.fire(new AfterDroneInstantiated(union, droneType, qualifier));
            return union.asInstance(droneType);

        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance within " + timeout + "" + timeoutTimeUnit, e);
        }
    }
}
