/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDroneFactory;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.spi.command.PrepareDrone;
import org.jboss.arquillian.drone.spi.event.AfterDroneDestroyed;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.AfterDronePrepared;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDestroyed;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDronePrepared;
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
 * Ensures that custom annotation has no effect
 */
@RunWith(MockitoJUnitRunner.class)
public class InjectionPointsTest extends AbstractTestTestBase {

    @Mock
    private ServiceLoader serviceLoader;

    private static void verifyInjections(MockDrone drone, MockDrone qualifiedDrone) {
        Assert.assertNotNull(drone);
        Assert.assertNotNull(qualifiedDrone);
        Assert.assertNotSame(drone, qualifiedDrone);
    }

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DroneLifecycleManager.class);
        extensions.add(DroneRegistrar.class);
        extensions.add(DroneConfigurator.class);
        extensions.add(DroneTestEnricher.class);
        extensions.add(DroneDestructor.class);
        extensions.add(DroneEnhancer.class);
        extensions.add(DroneTestEnrichBeforeClassObserver.class);
    }

    @SuppressWarnings("rawtypes")
    @org.junit.Before
    public void setMocks() {
        ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone")
            .property("field", "foobar");

        TestEnricher testEnricher = new DroneTestEnricher();
        getManager().inject(testEnricher);

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);
        Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(
            Arrays.<Configurator>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Instantiator.class)).thenReturn(
            Arrays.<Instantiator>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Destructor.class)).thenReturn(
            Arrays.<Destructor>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.onlyOne(TestEnricher.class)).thenReturn(testEnricher);
    }

    @Test
    public void customAnnotationHasNoEffect() throws Exception {
        runCustomAnnotationHasNoEffectTest(DummyClass.class, null, "testDummyMethod", false);
    }

    @Test
    public void customAnnotationHasNoEffectWithBeforeClass() throws Exception {
        runCustomAnnotationHasNoEffectTest(DummyClassWithBeforeClass.class, "beforeClassMethod", "testDummyMethod",
            true);
    }

    private void runCustomAnnotationHasNoEffectTest(Class<?> testClass, String beforeClassMethodName,
        String testMethodName, boolean createdInBeforeClass)
        throws Exception {

        getManager().getContext(ClassContext.class).activate(testClass);

        Object instance = testClass.newInstance();
        Method testMethod = testClass.getMethod(testMethodName);
        Method beforeClassMethod = null;
        if (beforeClassMethodName != null) {
            testClass.getMethod(beforeClassMethodName);
        }

        getManager().getContext(TestContext.class).activate(instance);
        fire(new BeforeSuite());

        DroneContext context = getManager()
            .getContext(ApplicationContext.class).getObjectStore().get(DroneContext.class);
        Assert.assertNotNull("DroneContext was created in the context", context);

        DroneRegistry registry = getManager().getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
        Assert.assertNotNull("Drone registry was created in the context", registry);

        Assert.assertTrue(registry.getEntryFor(MockDrone.class, Configurator.class) instanceof MockDroneFactory);
        Assert.assertTrue(registry.getEntryFor(MockDrone.class, Instantiator.class) instanceof MockDroneFactory);
        Assert.assertTrue(registry.getEntryFor(MockDrone.class, Destructor.class) instanceof MockDroneFactory);

        assertEventFired(PrepareDrone.class, 0);
        assertEventFired(BeforeDronePrepared.class, 0);
        assertEventFired(AfterDronePrepared.class, 0);

        fire(new BeforeClass(testClass));

        assertEventFired(PrepareDrone.class, 2);
        assertEventFired(BeforeDronePrepared.class, 2);
        assertEventFired(AfterDronePrepared.class, 2);

        if (createdInBeforeClass) {
            assertEventFired(BeforeDroneInstantiated.class, 2);
            assertEventFired(AfterDroneInstantiated.class, 2);
        }
        if (beforeClassMethod != null) {
            beforeClassMethod.invoke(null);
        }

        fire(new Before(instance, testMethod));

        assertEventFired(PrepareDrone.class, 2);
        assertEventFired(BeforeDronePrepared.class, 2);
        assertEventFired(AfterDronePrepared.class, 2);

        if (!createdInBeforeClass) {
            // was not instantiated yet
            assertEventFired(BeforeDroneInstantiated.class, 0);
            assertEventFired(AfterDroneInstantiated.class, 0);
        }

        TestEnricher testEnricher = serviceLoader.onlyOne(TestEnricher.class);
        testEnricher.enrich(instance);

        // enriched did the instantiation
        assertEventFired(BeforeDroneInstantiated.class, 2);
        assertEventFired(AfterDroneInstantiated.class, 2);

        Object[] dummyParameters = testEnricher.resolve(testMethod);

        assertEventFired(BeforeDroneInstantiated.class, 2);
        assertEventFired(AfterDroneInstantiated.class, 2);

        testMethod.invoke(instance, dummyParameters);

        fire(new After(instance, testMethod));

        // will be destroyed in after class
        assertEventFired(BeforeDroneDestroyed.class, 0);
        assertEventFired(AfterDroneDestroyed.class, 0);

        fire(new AfterClass(testClass));

        assertEventFired(BeforeDroneDestroyed.class, 2);
        assertEventFired(AfterDroneDestroyed.class, 2);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface CustomAnnotation {
    }

    static class DummyClass {
        @Drone
        @CustomAnnotation
        MockDrone drone;

        @Drone
        @Different
        @CustomAnnotation
        MockDrone qualifiedDrone;

        public void testDummyMethod() {
            verifyInjections(drone, qualifiedDrone);
        }
    }

    static class DummyClassWithBeforeClass {
        @Drone
        @CustomAnnotation
        static MockDrone drone;

        @Drone
        @Different
        @CustomAnnotation
        static MockDrone qualifiedDrone;

        @org.junit.BeforeClass
        public static void beforeClassMethod() {
            verifyInjections(drone, qualifiedDrone);
        }

        public void testDummyMethod() {
            verifyInjections(drone, qualifiedDrone);
        }
    }
}
