/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package org.arquillian.drone.appium.extension;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.drone.impl.DroneLifecycleManager;
import org.jboss.arquillian.drone.impl.DroneRegistrar;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

/**
 * @see org.jboss.arquillian.drone.impl.GlobalDroneConfigurationTestCase
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class DroneDefaultValuesTest extends AbstractTestTestBase {
    @Mock
    private ServiceLoader serviceLoader;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DroneLifecycleManager.class);
        extensions.add(DroneRegistrar.class);
        extensions.add(DefaultValuesModifier.class);
    }

    @Test
    public void defaultConfiguration() throws Exception {
        // given
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class);

        getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

        // then
        verifyTimeout(DefaultValuesModifier.DEFAULT_INSTANTIATION_TIMEOUT);
    }

    @Test
    public void specificConfiguration() throws Exception {
        // given
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class)
            .extension("drone").property("instantiationTimeoutInSeconds", "40");

        getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

        // then
        verifyTimeout(40);
    }

    private void verifyTimeout(int timeout) {
        // when
        getManager().fire(new BeforeSuite());

        DroneContext context = getManager().getContext(ApplicationContext.class).getObjectStore().get(DroneContext
            .class);
        Assert.assertNotNull("DroneContext was created in the context", context);

        DroneLifecycleManager.GlobalDroneConfiguration globalDroneConfiguration = context.getGlobalDroneConfiguration
            (DroneLifecycleManager.GlobalDroneConfiguration.class);
        Assert.assertNotNull("Global Drone configuration was created", globalDroneConfiguration);
        Assert.assertEquals("Drone timeout is set to " + timeout + " seconds", timeout,
            globalDroneConfiguration.getInstantiationTimeoutInSeconds());
    }
}
