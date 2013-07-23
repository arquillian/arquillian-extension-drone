package org.jboss.arquillian.drone.spi.event;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.drone.spi.DroneConfiguration;

public class AfterDroneConfigured {

    private final DroneConfiguration<?> configuration;
    private final Class<?> droneType;
    private final Class<? extends Annotation> qualifier;

    public AfterDroneConfigured(DroneConfiguration<?> configuration, Class<?> droneType, Class<? extends Annotation> qualifier) {
        this.configuration = configuration;
        this.droneType = droneType;
        this.qualifier = qualifier;
    }

    public DroneConfiguration<?> getConfiguration() {
        return configuration;
    }

    public Class<?> getDroneType() {
        return droneType;
    }

    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

}
