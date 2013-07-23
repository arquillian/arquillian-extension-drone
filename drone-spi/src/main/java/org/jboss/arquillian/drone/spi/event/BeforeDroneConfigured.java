package org.jboss.arquillian.drone.spi.event;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

public class BeforeDroneConfigured {

    private final Configurator<?, ? extends DroneConfiguration<?>> configurator;
    private final Class<?> droneType;
    private final Class<? extends Annotation> qualifier;

    public BeforeDroneConfigured(Configurator<?, ? extends DroneConfiguration<?>> configurator, Class<?> droneType,
            Class<? extends Annotation> qualifier) {
        this.configurator = configurator;
        this.droneType = droneType;
        this.qualifier = qualifier;
    }

    public Configurator<?, ? extends DroneConfiguration<?>> getConfigurator() {
        return configurator;
    }

    public Class<?> getDroneType() {
        return droneType;
    }

    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

}
