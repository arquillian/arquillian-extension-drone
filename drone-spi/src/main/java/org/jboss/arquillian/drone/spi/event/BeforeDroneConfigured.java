/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.spi.event;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.InjectionPoint;

/**
 * This event is fired before Drone configuration is created. You'd need to modify {@link ArquillianDescriptor} in
 * order to
 * change configuration before it is created.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class BeforeDroneConfigured extends BaseDroneEvent implements DroneConfigurationEvent {

    private final Configurator<?, ? extends DroneConfiguration<?>> configurator;

    public BeforeDroneConfigured(Configurator<?, ? extends DroneConfiguration<?>> configurator,
                                 InjectionPoint<?> injectionPoint) {
        super(injectionPoint);
        this.configurator = configurator;
    }

    public Configurator<?, ? extends DroneConfiguration<?>> getConfigurator() {
        return configurator;
    }

}
