/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.WebDriver;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for a generic
 * WebDriver browser.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class WebDriverFactory implements Configurator<WebDriver, TypedWebDriverConfiguration<WebDriverConfiguration>>,
        Instantiator<WebDriver, TypedWebDriverConfiguration<WebDriverConfiguration>>, Destructor<WebDriver> {

    @Inject
    private Instance<DroneRegistry> registryInstance;

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
    public void destroyInstance(WebDriver instance) {
        instance.quit();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @SuppressWarnings("unchecked")
    @Override
    public WebDriver createInstance(TypedWebDriverConfiguration<WebDriverConfiguration> configuration) {

        // check if there is a better instantiator then default one
        String implementationClassName = configuration.getImplementationClass();

        DroneRegistry registry = registryInstance.get();
        Class<?> implementationClass = SecurityActions.getClass(implementationClassName);
        @SuppressWarnings("rawtypes")
        Instantiator instantiator = registry.getEntryFor(implementationClass, Instantiator.class);

        // if we've found an instantiator and at the same time we are sure that it is not the current one
        // invoke it instead in order to get capabilities and other advanced stuff
        if (instantiator != null && instantiator.getClass() != this.getClass()) {
            return (WebDriver) instantiator.createInstance(configuration);
        }

        // this is a simple constructor which does not know anything advanced
        WebDriver driver = SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[0], new Object[0],
                WebDriver.class);
        return driver;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.core.spi.LoadableExtension#createConfiguration(org.jboss.arquillian.impl.configuration.api.
     * ArquillianDescriptor, java.lang.Class)
     */
    @Override
    public TypedWebDriverConfiguration<WebDriverConfiguration> createConfiguration(ArquillianDescriptor descriptor,
            Class<? extends Annotation> qualifier) {
        return new TypedWebDriverConfiguration<WebDriverConfiguration>(WebDriverConfiguration.class,
                "org.openqa.selenium.htmlunit.HtmlUnitDriver").configure(descriptor, qualifier);
    }

}
