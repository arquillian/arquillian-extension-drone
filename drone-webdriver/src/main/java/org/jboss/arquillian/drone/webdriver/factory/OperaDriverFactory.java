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

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.OperaDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.opera.core.systems.OperaDriver;

/**
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 */
public class OperaDriverFactory implements Configurator<OperaDriver, TypedWebDriverConfiguration<OperaDriverConfiguration>>,
        Instantiator<OperaDriver, TypedWebDriverConfiguration<OperaDriverConfiguration>>, Destructor<OperaDriver> {

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public TypedWebDriverConfiguration<OperaDriverConfiguration> createConfiguration(ArquillianDescriptor descriptor,
            Class<? extends Annotation> qualifier) {
        return new TypedWebDriverConfiguration<OperaDriverConfiguration>(OperaDriverConfiguration.class).configure(descriptor,
                qualifier);
    }

    @Override
    public OperaDriver createInstance(TypedWebDriverConfiguration<OperaDriverConfiguration> configuration) {

        DesiredCapabilities operaCapabilities = new DesiredCapabilities(configuration.getCapabilities());
        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { Capabilities.class },
                new Object[] { operaCapabilities }, OperaDriver.class);
    }

    @Override
    public void destroyInstance(OperaDriver instance) {
        instance.quit();
    }

}
