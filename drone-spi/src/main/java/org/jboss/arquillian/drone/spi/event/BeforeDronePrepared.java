/**
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
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
package org.jboss.arquillian.drone.spi.event;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.Instantiator;

/**
 * This event is fired before Drone preparation is started. You'd need to modify {@link ArquillianDescriptor} in
 * order to change configuration before it is created.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class BeforeDronePrepared extends BaseDroneEvent implements DroneConfigurationEvent {

    private final Configurator<?, ? extends DroneConfiguration<?>> configurator;
    private final Instantiator<?, ? extends DroneConfiguration<?>> instantiator;

    public BeforeDronePrepared(Configurator<?, ? extends DroneConfiguration<?>> configurator,
        Instantiator<?, ? extends DroneConfiguration<?>> instantiator, DronePoint<?> dronePoint) {
        super(dronePoint);
        this.configurator = configurator;
        this.instantiator = instantiator;
    }

    /**
     * When Drone is already configured so it is not configured after this event, then the returned configurator is null
     *
     * @return configurator
     */
    public Configurator<?, ? extends DroneConfiguration<?>> getConfigurator() {
        return configurator;
    }

    /**
     * When Drone Callable is already crated so it is not created after this event, then the returned instantiator is null
     *
     * @return instantiator
     */
    public Instantiator<?, ? extends DroneConfiguration<?>> getInstantiator() {
        return instantiator;
    }
}
