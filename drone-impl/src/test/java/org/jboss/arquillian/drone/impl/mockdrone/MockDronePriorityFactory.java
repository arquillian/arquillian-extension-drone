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
package org.jboss.arquillian.drone.impl.mockdrone;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.Instantiator;

import java.lang.annotation.Annotation;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 */
public class MockDronePriorityFactory implements Configurator<MockDrone, MockDroneConfiguration>,
        Instantiator<MockDrone, MockDroneConfiguration>, Destructor<MockDrone> {
    public static final String MOCK_DRONE_PRIORITY_FACTORY_FIELD = "Set by MockDronePriorityFactory";

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.arquillian.drone.spi.Sortable#getPrecedence()
     */
    public int getPrecedence() {
        return 10;
    }

    @Override
    public MockDroneConfiguration createConfiguration(ArquillianDescriptor descriptor, InjectionPoint<MockDrone>
            injectionPoint) {
        MockDroneConfiguration configuration = new MockDroneConfiguration().configure(descriptor, injectionPoint.getQualifier());
        configuration.setField(MOCK_DRONE_PRIORITY_FACTORY_FIELD);
        return configuration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.arquillian.drone.spi.Destructor#destroyInstance(java.lang.Object)
     */
    public void destroyInstance(MockDrone instance) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi
     * .DroneConfiguration)
     */
    public MockDrone createInstance(MockDroneConfiguration configuration) {
        MockDrone instance = new MockDrone(configuration.getField());
        return instance;
    }

}