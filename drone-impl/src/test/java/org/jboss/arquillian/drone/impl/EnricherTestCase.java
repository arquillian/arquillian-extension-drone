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
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.threading.ExecutorService;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDroneConfiguration;
import org.jboss.arquillian.drone.impl.mockdrone.MockDroneFactory;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.drone.spi.InjectionPoint;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests Configurator precedence and its retrieval chain, uses qualifier as well.
 * <p/>
 * Additionally tests DroneTestEnricher
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class EnricherTestCase extends AbstractTestTestBase {
    private static final String DIFFERENT_FIELD = "ArquillianDescriptor @DifferentMock";
    private static final String METHOD_ARGUMENT_ONE_FIELD = "ArquillianDescriptor @MethodArgumentOne";

    @Mock
    private ServiceLoader serviceLoader;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DroneLifecycleManager.class);
        extensions.add(DroneRegistrar.class);
        extensions.add(DroneConfigurator.class);
        extensions.add(DroneTestEnricher.class);
        extensions.add(DroneDestructor.class);
    }

    @SuppressWarnings("rawtypes")
    @org.junit.Before
    public void setMocks() {
        ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone-different")
                .property("field", DIFFERENT_FIELD).extension("mockdrone-methodargumentone")
                .property("field", METHOD_ARGUMENT_ONE_FIELD);

        TestEnricher testEnricher = new DroneTestEnricher();
        //DroneInstanceCreator instanceCreator = new DroneInstanceCreator();
        getManager().inject(testEnricher);

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);
        Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(
                Arrays.<Configurator>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Instantiator.class)).thenReturn(
                Arrays.<Instantiator>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Destructor.class)).thenReturn(Arrays.<Destructor>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.onlyOne(TestEnricher.class)).thenReturn(testEnricher);

    }

    @Test
    public void testQualifer() throws Exception {
        getManager().getContext(ClassContext.class).activate(EnrichedClass.class);
        fire(new BeforeSuite());

        DroneContext context = getManager()
                .getContext(ApplicationContext.class).getObjectStore().get(DroneContext.class);
        Assert.assertNotNull("DroneContext was created in the context", context);

        DroneRegistry registry = getManager().getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
        Assert.assertNotNull("Drone registry was created in the context", registry);

        Assert.assertTrue("Configurator is of mock type",
                registry.getEntryFor(MockDrone.class, Configurator.class) instanceof MockDroneFactory);
        Assert.assertTrue("Instantiator is of mock type",
                registry.getEntryFor(MockDrone.class, Instantiator.class) instanceof MockDroneFactory);

        fire(new BeforeClass(EnrichedClass.class));

        InjectionPoint<MockDrone> invalidInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Lifecycle.CLASS);
        InjectionPoint<MockDrone> injectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Different.class, InjectionPoint.Lifecycle.CLASS);

        MockDroneConfiguration configuration = context.getDroneConfiguration(injectionPoint,
                MockDroneConfiguration.class);
        Assert.assertFalse("There is no MockDroneConfiguration with @Default qualifier",
                context.isDroneConfigurationStored(invalidInjectionPoint));
        Assert.assertNotNull("MockDroneConfiguration is stored with @Different qualifier", configuration);
        Assert.assertEquals("MockDrone was configured from @Different configuration", DIFFERENT_FIELD,
                configuration.getField());

        getManager().getContext(ClassContext.class).deactivate();
        getManager().getContext(ClassContext.class).destroy(EnrichedClass.class);
    }

    @Test
    public void testMethodQualiferWithCleanup() throws Exception {
        getManager().getContext(ClassContext.class).activate(MethodEnrichedClass.class);

        Object instance = new MethodEnrichedClass();
        Method testMethod = MethodEnrichedClass.class.getMethod("testMethodEnrichment", MockDrone.class);

        getManager().getContext(TestContext.class).activate(instance);
        fire(new BeforeSuite());

        DroneContext context = getManager()
                .getContext(ApplicationContext.class).getObjectStore().get(DroneContext.class);
        Assert.assertNotNull("DroneContext was created in the application context", context);

        DroneRegistry registry = getManager().getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
        Assert.assertNotNull("Drone registry was created in the context", registry);

        Assert.assertTrue("Configurator is of mock type",
                registry.getEntryFor(MockDrone.class, Configurator.class) instanceof MockDroneFactory);
        Assert.assertTrue("Instantiator is of mock type",
                registry.getEntryFor(MockDrone.class, Instantiator.class) instanceof MockDroneFactory);

        fire(new BeforeClass(MethodEnrichedClass.class));
        fire(new Before(instance, testMethod));

        TestEnricher testEnricher = serviceLoader.onlyOne(TestEnricher.class);

        testEnricher.enrich(instance);
        Object[] parameters = testEnricher.resolve(testMethod);

        InjectionPoint<MockDrone> injectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                MethodArgumentOne.class, InjectionPoint.Lifecycle.METHOD);
        Assert.assertTrue("Drone created", context.isDroneInstantiated(injectionPoint));

        testMethod.invoke(instance, parameters);

        fire(new After(instance, testMethod));
        fire(new AfterClass(MethodEnrichedClass.class));
        Assert.assertFalse("Drone destroyed", context.isDroneInstantiated(injectionPoint));
    }

    @Test(expected = IllegalStateException.class)
    public void testMethodQualiferUnregistered() throws Exception {
        getManager().getContext(ClassContext.class).activate(MethodEnrichedClassUnregistered.class);

        Object instance = new MethodEnrichedClassUnregistered();
        Method testMethod = MethodEnrichedClassUnregistered.class.getMethod("testMethodEnrichment", Object.class);

        getManager().getContext(TestContext.class).activate(instance);
        fire(new BeforeSuite());

        DroneContext context = getManager().getContext(ApplicationContext.class).getObjectStore().get(DroneContext
                .class);
        Assert.assertNotNull("DroneContext was created in the context", context);

        DroneRegistry registry = getManager().getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
        Assert.assertNotNull("Drone registry was created in the context", registry);

        Assert.assertTrue("Configurator is of mock type",
                registry.getEntryFor(MockDrone.class, Configurator.class) instanceof MockDroneFactory);
        Assert.assertTrue("Instantiator is of mock type",
                registry.getEntryFor(MockDrone.class, Instantiator.class) instanceof MockDroneFactory);

        fire(new BeforeClass(MethodEnrichedClassUnregistered.class));
        fire(new Before(instance, testMethod));

        TestEnricher testEnricher = serviceLoader.onlyOne(TestEnricher.class);
        testEnricher.enrich(instance);
        Object[] parameters = testEnricher.resolve(testMethod);

        InjectionPoint<Object> injectionPoint = new InjectionPointImpl<Object>(Object.class, Default.class,
                InjectionPoint.Lifecycle.METHOD);
        Assert.assertTrue("Drone created", context.isDroneInstantiated(injectionPoint));

        testMethod.invoke(instance, parameters);

        fire(new After(instance, testMethod));
        fire(new AfterClass(MethodEnrichedClassUnregistered.class));
        Assert.assertFalse("Drone destroyed", context.isDroneInstantiated(injectionPoint));
    }

    @Test
    public void testClassWithoutArquillianLifecycle() throws Exception {
        Object instance = new NonArquillianClass();
        Method testMethod = NonArquillianClass.class.getMethod("someMethod", MockDrone.class);

        fire(new BeforeSuite());

        DroneContext context = getManager().getContext(ApplicationContext.class).getObjectStore().get(DroneContext
                .class);
        Assert.assertNotNull("DroneContext was created in the context", context);

        TestEnricher testEnricher = serviceLoader.onlyOne(TestEnricher.class);
        testEnricher.enrich(instance);
        Object[] parameters = testEnricher.resolve(testMethod);

        InjectionPoint<MockDrone> classInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Lifecycle.CLASS);
        Assert.assertTrue("Class drone created", context.isDroneInstantiated(classInjectionPoint));

        InjectionPoint<MockDrone> methodInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Lifecycle.METHOD);
        Assert.assertTrue("Method drone created", context.isDroneInstantiated(methodInjectionPoint));

        testMethod.invoke(instance, parameters);
    }

    @Test()
    public void testClassWithoutArquillianLifecycleWithDeploymentDrone() throws Exception {
        Object instance = new NonArquillianClassWithDeploymentDrone();

        fire(new BeforeSuite());

        TestEnricher testEnricher = serviceLoader.onlyOne(TestEnricher.class);

        exception.expect(IllegalStateException.class);
        exception.expectMessage("has deployment lifecycle");
        testEnricher.enrich(instance);
    }

    static class EnrichedClass {
        @Drone
        @Different
        MockDrone unused;
    }

    static class MethodEnrichedClass {

        public void testMethodEnrichment(@Drone @MethodArgumentOne MockDrone unused) {
            Assert.assertNotNull("Mock drone instance was created", unused);
            Assert.assertEquals("MockDroneConfiguration is set via ArquillianDescriptor", METHOD_ARGUMENT_ONE_FIELD,
                    unused.getField());
        }
    }

    static class MethodEnrichedClassUnregistered {
        public void testMethodEnrichment(@Drone Object unused) {
        }
    }

    static class NonArquillianClass {
        @Drone
        MockDrone classDrone;

        public void someMethod(@Drone MockDrone methodDrone) {

        }
    }

    static class NonArquillianClassWithDeploymentDrone {
        @Drone
        @OperateOnDeployment("deployment")
        MockDrone deploymentDrone;
    }

}
