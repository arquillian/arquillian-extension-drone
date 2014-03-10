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
package org.jboss.arquillian.drone.impl;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDroneConfiguration;
import org.jboss.arquillian.drone.impl.mockdrone.MockDroneFactory;
import org.jboss.arquillian.drone.impl.mockdrone.MockDronePriorityFactory;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.Instantiator;
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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

/**
 * Tests Registar precedence and its retrieval chain.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RegistrarTestCase extends AbstractTestTestBase {
    @Mock
    private ServiceLoader serviceLoader;

    @Drone
    MockDrone unused;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DroneLifecycleManager.class);
        extensions.add(DroneRegistrar.class);
        extensions.add(DroneConfigurator.class);
    }

    @SuppressWarnings("rawtypes")
    @Before
    public void setMocks() {
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class);

        getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);
        Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(
                Arrays.<Configurator>asList(new MockDronePriorityFactory(), new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Instantiator.class)).thenReturn(
                Arrays.<Instantiator>asList(new MockDronePriorityFactory(), new MockDroneFactory()));

    }

    @Test
    public void testPrecedence() throws Exception {
        getManager().fire(new BeforeSuite());

        DroneRegistry registry = getManager().getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
        Assert.assertNotNull("Drone registry was created in the context", registry);

        Assert.assertNotNull("Configurator for MockDrone was created",
                registry.getEntryFor(MockDrone.class, Configurator.class));

        Assert.assertTrue("Configurator is of MockDronePriorityFactory type",
                registry.getEntryFor(MockDrone.class, Configurator.class) instanceof MockDronePriorityFactory);

        getManager().fire(new BeforeClass(this.getClass()));

        DroneContext context = getManager().getContext(ApplicationContext.class).getObjectStore().get(DroneContext
                .class);
        Assert.assertNotNull("Drone object holder was created in the context", context);

        InjectionPoint<MockDrone> injectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class, Default.class,
                InjectionPoint.Lifecycle.CLASS);

        MockDroneConfiguration configuration = context.getDroneConfiguration(injectionPoint,
                MockDroneConfiguration.class);
        Assert.assertEquals("MockDrone configuration was created by MockDronePriorityFactory",
                MockDronePriorityFactory.MOCK_DRONE_PRIORITY_FACTORY_FIELD, configuration.getField());
    }
}