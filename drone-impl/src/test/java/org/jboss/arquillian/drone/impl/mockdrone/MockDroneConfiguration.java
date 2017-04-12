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

import java.lang.annotation.Annotation;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

public class MockDroneConfiguration implements DroneConfiguration<MockDroneConfiguration> {

    private String field;

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.DroneConfiguration#getConfigurationName()
     */
    public String getConfigurationName() {
        return "mockdrone";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.arquillian.drone.spi.DroneConfiguration#configure(org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor
     * , java.lang.Class)
     */
    public MockDroneConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        return ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * @param field
     *     the field to set
     */
    public void setField(String field) {
        this.field = field;
    }
}
