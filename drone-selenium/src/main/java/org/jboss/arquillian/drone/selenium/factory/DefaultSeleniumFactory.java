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
package org.jboss.arquillian.drone.selenium.factory;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.selenium.configuration.SeleniumConfiguration;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.Instantiator;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for Selenium
 * browser object called {@link com.thoughtworks.selenium.DefaultSelenium} .
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DefaultSeleniumFactory implements Configurator<DefaultSelenium, SeleniumConfiguration>,
        Instantiator<DefaultSelenium, SeleniumConfiguration>, Destructor<DefaultSelenium> {

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.selenium.spi.Sortable#getPrecedence()
     */
    public int getPrecedence() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Destructor#destroyInstance(java.lang.Object)
     */
    public void destroyInstance(DefaultSelenium instance) {
        instance.close();
        instance.stop();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss. arquillian.drone.spi.DroneConfiguration)
     */
    public DefaultSelenium createInstance(SeleniumConfiguration configuration) {
        DefaultSelenium selenium = new DefaultSelenium(configuration.getServerHost(), configuration.getServerPort(),
                configuration.getBrowser(), configuration.getUrl());
        selenium.start();
        selenium.setSpeed(String.valueOf(configuration.getSpeed()));
        selenium.setTimeout(String.valueOf(configuration.getTimeout()));

        return selenium;
    }

    @Override
    public SeleniumConfiguration createConfiguration(ArquillianDescriptor descriptor, InjectionPoint<DefaultSelenium> injectionPoint) {
        return new SeleniumConfiguration().configure(descriptor, injectionPoint.getQualifier());
    }
}
