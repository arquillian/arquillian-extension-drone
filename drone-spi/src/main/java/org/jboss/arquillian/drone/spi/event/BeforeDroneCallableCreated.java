package org.jboss.arquillian.drone.spi.event;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.Instantiator;

public class BeforeDroneCallableCreated {

    private final Instantiator<?, ? extends DroneConfiguration<?>> instantiator;
    private final Class<?> droneType;
    private final Class<? extends Annotation> qualifier;

    public BeforeDroneCallableCreated(Instantiator<?, ? extends DroneConfiguration<?>> instantiator, Class<?> droneType,
            Class<? extends Annotation> qualifier) {
        this.instantiator = instantiator;
        this.droneType = droneType;
        this.qualifier = qualifier;
    }

    public Instantiator<?, ? extends DroneConfiguration<?>> getInstantiator() {
        return instantiator;
    }

    public Class<?> getDroneType() {
        return droneType;
    }

    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

}
