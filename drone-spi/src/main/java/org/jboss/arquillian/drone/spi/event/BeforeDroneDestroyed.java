package org.jboss.arquillian.drone.spi.event;

import java.lang.annotation.Annotation;

public class BeforeDroneDestroyed {

    private final Object instance;
    private final Class<?> droneType;
    private final Class<? extends Annotation> qualifier;

    public BeforeDroneDestroyed(Object instance, Class<?> droneType, Class<? extends Annotation> qualifier) {
        this.instance = instance;
        this.droneType = droneType;
        this.qualifier = qualifier;
    }

    public Object getInstance() {
        return instance;
    }

    public Class<?> getDroneType() {
        return droneType;
    }

    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

}
