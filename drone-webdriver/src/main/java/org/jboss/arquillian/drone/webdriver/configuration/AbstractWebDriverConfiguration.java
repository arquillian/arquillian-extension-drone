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
package org.jboss.arquillian.drone.webdriver.configuration;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Configuration shared among all WebDriver implementations. This means that all configurations maps to the same namespace,
 * however user decides which configuration is chosen by requesting a type of driver in the test.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see ArquillianDescriptor
 * @see org.jboss.arquillian.drone.configuration.ConfigurationMapper
 *
 */
public abstract class AbstractWebDriverConfiguration<T extends DroneConfiguration<T>> implements DroneConfiguration<T> {
    public static final String CONFIGURATION_NAME = "webdriver";

    protected String implementationClass;

    /**
     * Creates default Selenium WebDriver Configuration
     */
    public AbstractWebDriverConfiguration() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.selenium.spi.WebTestConfiguration#configure(org.jboss
     * .arquillian.impl.configuration.api.ArquillianDescriptor, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public T configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        ConfigurationMapper.fromArquillianDescriptor(descriptor, (T) this, qualifier);
        return ConfigurationMapper.fromSystemConfiguration((T) this, qualifier);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.selenium.spi.WebTestConfiguration#getConfigurationName ()
     */
    public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }

    /**
     * @return the implementationClass
     */
    public String getImplementationClass() {
        return implementationClass;
    }

    /**
     * @param implementationClass the implementationClass to set
     */
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

}
