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
package org.jboss.arquillian.drone.webdriver.factory;

import java.io.File;
import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.FirefoxDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for FirefoxDriver.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class FirefoxDriverFactory implements
        Configurator<FirefoxDriver, TypedWebDriverConfiguration<FirefoxDriverConfiguration>>,
        Instantiator<FirefoxDriver, TypedWebDriverConfiguration<FirefoxDriverConfiguration>>, Destructor<FirefoxDriver> {

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Sortable#getPrecedence()
     */
    @Override
    public int getPrecedence() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Destructor#destroyInstance(java.lang.Object)
     */
    @Override
    public void destroyInstance(FirefoxDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public FirefoxDriver createInstance(TypedWebDriverConfiguration<FirefoxDriverConfiguration> configuration) {

        String binary = configuration.getFirefoxBinary();
        String profile = configuration.getFirefoxProfile();

        // no argument constructor
        if (Validate.empty(binary) && Validate.empty(profile)) {
            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[0], new Object[0],
                    FirefoxDriver.class);
        }
        // only binary was specified
        else if (Validate.empty(profile)) {
            Validate.isExecutable(binary, "Firefox binary does not point to a valid executable,  " + binary);
            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { FirefoxBinary.class,
                    FirefoxProfile.class }, new Object[] { new FirefoxBinary(new File(binary)), null }, FirefoxDriver.class);
        }
        // only profile was specified
        else if (Validate.empty(binary)) {
            Validate.isValidPath(profile, "Firefox profile does not point to a valid path " + profile);

            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { FirefoxProfile.class },
                    new Object[] { new FirefoxProfile(new File(profile)) }, FirefoxDriver.class);
        }
        // both were specified
        else {
            Validate.isValidPath(profile, "Firefox profile does not point to a valid path,  " + profile);
            Validate.isExecutable(binary, "Firefox binary does not point to a valid executable,  " + binary);

            return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { FirefoxBinary.class,
                    FirefoxProfile.class }, new Object[] { new FirefoxBinary(new File(binary)),
                    new FirefoxProfile(new File(profile)) }, FirefoxDriver.class);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.core.spi.LoadableExtension#createConfiguration(org.jboss.arquillian.impl.configuration.api.
     * ArquillianDescriptor, java.lang.Class)
     */
    @Override
    public TypedWebDriverConfiguration<FirefoxDriverConfiguration> createConfiguration(ArquillianDescriptor descriptor,
            Class<? extends Annotation> qualifier) {
        return new TypedWebDriverConfiguration<FirefoxDriverConfiguration>(FirefoxDriverConfiguration.class,
                "org.openqa.selenium.firefox.FirefoxDriver").configure(descriptor, qualifier);
    }

}