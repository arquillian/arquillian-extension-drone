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
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;

public class DroneInstanceCreator {

    // this executor will run callables in the same thread as caller
    // we need this in order to allow better Drone based code debugging
    private static final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
    // Executors.newSingleThreadExecutor();

    @Inject
    private Instance<DroneContext> context;

    @Inject
    private Event<BeforeDroneInstantiated> beforeDroneInstantiated;

    @Inject
    private Event<AfterDroneInstantiated> afterDroneInstantiated;

    public Object createDroneInstance(Callable<?> instanceCallable, Class<?> droneType, Class<? extends Annotation> qualifier,
            long timeout, TimeUnit timeoutTimeUnit) {
        try {
            beforeDroneInstantiated.fire(new BeforeDroneInstantiated(instanceCallable, droneType, qualifier));
            Object browser = executorService.submit(instanceCallable).get(timeout, timeoutTimeUnit);
            // store instance in the context
            context.get().replace(droneType, qualifier, browser);
            afterDroneInstantiated.fire(new AfterDroneInstantiated(browser, droneType, qualifier));

            // we are returning instance in the context, because it might get modified by somebody listening to
            // the event
            return context.get().get(droneType, qualifier);

        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Unable to retrieve Drone Instance within " + timeout + "" + timeoutTimeUnit, e);
        }
    }
}
