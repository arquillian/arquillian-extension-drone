/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.junit.Assert;

import static org.hamcrest.CoreMatchers.is;

/**
 *
 */
public class CachingMockDroneFactory implements Configurator<MockDrone, MockDroneConfiguration>,
    Instantiator<MockDrone, MockDroneConfiguration>, Destructor<MockDrone> {

    private MockDrone instanceCache;

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Sortable#getPrecedence()
     */
    public int getPrecedence() {
        return 0;
    }

    @Override
    public MockDroneConfiguration createConfiguration(ArquillianDescriptor descriptor, DronePoint<MockDrone> dronePoint) {
        return new MockDroneConfiguration().configure(descriptor, dronePoint.getQualifier());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Destructor#destroyInstance(java.lang.Object)
     */
    public void destroyInstance(MockDrone instance) {
        // here we assert that instance we are going to destroy is the same as instance we created
        // this check verifies that deenhancers were called and they deenhanced instance
        Assert.assertThat("Instance we want to destroy must be deenhanced if previously enhanced", instance,
            is(instanceCache));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    public synchronized MockDrone createInstance(MockDroneConfiguration configuration) {
        if (instanceCache == null) {
            MockDrone instance = new MockDrone(configuration.getField());
            instanceCache = instance;
        }
        return instanceCache;
    }
}
