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
package org.jboss.arquillian.drone.configuration;

import java.lang.annotation.Annotation;
import java.util.Map;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Sample configuration
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class TypedMockDroneConfiguration implements DroneConfiguration<TypedMockDroneConfiguration> {

    private Map<String, Object> objectMap;

    private Map<String, String> stringMap;

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.DroneConfiguration#getConfigurationName()
     */
    public String getConfigurationName() {
        return "typedmockdrone";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.arquillian.drone.spi.DroneConfiguration#configure(org.jboss.arquillian.impl.configuration.api
     * .ArquillianDescriptor
     * , java.lang.Class)
     */
    @Override
    public TypedMockDroneConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        return ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
    }

    public Map<String, Object> getObjectMap() {
        return objectMap;
    }

    public void setObjectMap(Map<String, Object> objectMap) {
        this.objectMap = objectMap;
    }

    public Map<String, String> getStringMap() {
        return stringMap;
    }

    public void setStringMap(Map<String, String> stringMap) {
        this.stringMap = stringMap;
    }
}
