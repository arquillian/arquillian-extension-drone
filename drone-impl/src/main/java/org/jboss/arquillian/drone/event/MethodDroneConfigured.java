/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.event;

import java.lang.annotation.Annotation;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * An event to inform other components that a drone instance was configured
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class MethodDroneConfigured {
    private Class<?> type;
    private Class<? extends Annotation> qualifier;
    private DroneConfiguration<?> configuration;

    /**
     * Creates the event
     *
     * @param qualifier Qualifier for current drone instance
     * @param configuration Configuration for drone instance
     */
    public MethodDroneConfigured(Class<?> type, Class<? extends Annotation> qualifier, DroneConfiguration<?> configuration) {
        this.type = type;
        this.qualifier = qualifier;
        this.configuration = configuration;
    }

    /**
     * @return the qualifier
     */
    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

    /**
     * @param qualifier the qualifier to set
     */
    public void setQualifier(Class<? extends Annotation> qualifier) {
        this.qualifier = qualifier;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(DroneConfiguration<?> configuration) {
        this.configuration = configuration;
    }

    /**
     * @return the configuration
     */
    public DroneConfiguration<?> getConfiguration() {
        return configuration;
    }

    /**
     * @return the type
     */
    public Class<?> getDroneType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setDroneType(Class<?> type) {
        this.type = type;
    }

}
