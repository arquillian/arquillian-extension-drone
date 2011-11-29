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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDroneFactory;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests Destroyer activation when no context was created (no @Drone) won't fail
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DestroyerTestCase extends AbstractTestTestBase {

    @Mock
    private ServiceLoader serviceLoader;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DroneRegistrar.class);
        extensions.add(DroneConfigurator.class);
        extensions.add(DroneCreator.class);
        extensions.add(DroneTestEnricher.class);
        extensions.add(DroneDestructor.class);
    }

    @SuppressWarnings("rawtypes")
    @org.junit.Before
    public void setMocks() {
        ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone")
                .property("field", "foobar");

        TestEnricher testEnricher = new DroneTestEnricher();

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);
        Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(Arrays.<Configurator> asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Instantiator.class)).thenReturn(Arrays.<Instantiator> asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Destructor.class)).thenReturn(Arrays.<Destructor> asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.onlyOne(TestEnricher.class)).thenReturn(testEnricher);
    }

    @Test
    public void testDestructorWontTriggerAndFail() throws Exception {
        getManager().getContext(ClassContext.class).activate(DummyClass.class);

        Object instance = new DummyClass();
        Method testMethod = DummyClass.class.getMethod("testDummyMethod");

        getManager().getContext(TestContext.class).activate(instance);
        fire(new BeforeSuite());

        DroneRegistry registry = getManager().getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
        Assert.assertNotNull("Drone registry was created in the context", registry);

        Assert.assertTrue("Configurator is of mock type",
                registry.getEntryFor(MockDrone.class, Configurator.class) instanceof MockDroneFactory);

        fire(new BeforeClass(DummyClass.class));
        fire(new Before(instance, testMethod));

        DroneContext dc = getManager().getContext(ClassContext.class).getObjectStore().get(DroneContext.class);
        Assert.assertNull("Drone context was not created", dc);
        MethodContext mc = getManager().getContext(TestContext.class).getObjectStore().get(MethodContext.class);
        Assert.assertNull("Method context was not created", mc);

        fire(new After(instance, testMethod));
        fire(new AfterClass(DummyClass.class));

    }

    static class DummyClass {
        public void testDummyMethod() {
        }
    }
}
