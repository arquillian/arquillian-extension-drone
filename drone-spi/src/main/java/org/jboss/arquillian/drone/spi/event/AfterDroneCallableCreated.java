package org.jboss.arquillian.drone.spi.event;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;

public class AfterDroneCallableCreated {

    private final Callable<?> instanceCallable;
    private final Class<?> droneType;
    private final Class<? extends Annotation> qualifier;

    public AfterDroneCallableCreated(Callable<?> instanceCallable, Class<?> droneType, Class<? extends Annotation> qualifier) {
        this.instanceCallable = instanceCallable;
        this.droneType = droneType;
        this.qualifier = qualifier;
    }

    public Callable<?> getInstanceCallable() {
        return instanceCallable;
    }

    public Class<?> getDroneType() {
        return droneType;
    }

    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }
}
