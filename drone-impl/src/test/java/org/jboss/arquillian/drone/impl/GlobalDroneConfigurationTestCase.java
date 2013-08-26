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
package org.jboss.arquillian.drone.impl;

import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.impl.DroneConfigurator.GlobalDroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Global Drone configuration test cases
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GlobalDroneConfigurationTestCase extends AbstractTestTestBase {

    @Mock
    private ServiceLoader serviceLoader;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DroneRegistrar.class);
        extensions.add(DroneConfigurator.class);
    }

    @Before
    public void setMocks() {

    }

    @Test
    public void defaultConfiguration() throws Exception {

        // given
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class);

        getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

        // then
        verifyTimeout(5);
    }

    @Test
    public void specificConfiguration() throws Exception {
        // given
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class)
                .extension("drone").property("instantiationTimeoutInSeconds", "30");

        getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

        // then
        verifyTimeout(30);
    }

    @Test
    public void specificConfigurationWithDebugMode() throws Exception {

        String oldDebugValue = System.getProperty("arquillian.debug", "false");
        try {
            // given
            System.setProperty("arquillian.debug", "true");
            ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class)
                    .extension("drone").property("instantiationTimeoutInSeconds", "30");

            getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
            getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

            // then
            verifyTimeout(0);
        } finally {
            System.setProperty("arquillian.debug", oldDebugValue);
        }
    }

    private void verifyTimeout(int timeout) {
        // when
        getManager().fire(new BeforeSuite());

        // then
        DroneRegistry registry = getManager().getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
        Assert.assertNotNull("Drone registry was created in the context", registry);

        // when
        getManager().fire(new BeforeClass(this.getClass()));

        // then
        DroneContext context = getManager().getContext(ClassContext.class).getObjectStore().get(DroneContext.class);
        Assert.assertNotNull("Drone object holder was created in the context", context);

        InstanceOrCallableInstance globalConfiguration = context.get(GlobalDroneConfiguration.class, Default.class);
        Assert.assertNotNull("Global Drone configuration was created", globalConfiguration);
        Assert.assertEquals("Drone timeout is set to " + timeout + " seconds", timeout,
                globalConfiguration.asInstance(GlobalDroneConfiguration.class).getInstantiationTimeoutInSeconds());

    }
}
