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
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDroneConfiguration;
import org.jboss.arquillian.drone.impl.mockdrone.MockDroneFactory;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.jboss.arquillian.drone.impl.DroneTestEnricher.ARQUILLIAN_DRONE_CREATION_PROPERTY;

/**
 * Tests Configurator precedence and its retrieval chain, uses qualifier as well.
 * <p/>
 * Additionally tests DroneTestEnricher
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class EnricherTestCase extends AbstractTestTestBase {
    private static final String DIFFERENT_FIELD = "ArquillianDescriptor @DifferentMock";
    private static final String METHOD_ARGUMENT_ONE_FIELD = "ArquillianDescriptor @MethodArgumentOne";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    DeploymentDescription deploymentDescription1;

    @Mock
    DeploymentDescription deploymentDescription2;

    @Mock
    DeployableContainer deployableContainer;
    @Mock
    private ServiceLoader serviceLoader;

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

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
        getManager().inject(testEnricher);

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);
        Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(
            Arrays.<Configurator>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Instantiator.class)).thenReturn(
            Arrays.<Instantiator>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.all(Destructor.class)).thenReturn(Arrays.<Destructor>asList(new MockDroneFactory()));
        Mockito.when(serviceLoader.onlyOne(TestEnricher.class)).thenReturn(testEnricher);
        // These two stubbings are used in DeploymentTestCase -> hence Silent runner
        Mockito.when(deploymentDescription1.getName()).thenReturn(AnnotationMocks.DEPLOYMENT_1);
        Mockito.when(deploymentDescription2.getName()).thenReturn(AnnotationMocks.DEPLOYMENT_2);
    }

    @Test
    public void testQualifer() throws Exception {
        getManager().getContext(ClassContext.class).activate(EnrichedClass.class);

        DroneContext context = fireAndVerifyBeforeSuiteProcess();

        fire(new BeforeClass(EnrichedClass.class));

        DronePoint<MockDrone> invalidDronePoint = new DronePointImpl<MockDrone>(MockDrone.class,
            DronePoint.Lifecycle.CLASS, AnnotationMocks.drone());
        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.CLASS,
            AnnotationMocks.drone(), AnnotationMocks.differentQualifier());

        MockDroneConfiguration configuration = context.get(dronePoint).getConfigurationAs(MockDroneConfiguration.class);
        Assert.assertFalse("There is no MockDroneConfiguration with @Default qualifier",
            context.get(invalidDronePoint).hasConfiguration());
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
        DroneContext context = fireAndVerifyBeforeSuiteProcess();

        fire(new BeforeClass(MethodEnrichedClass.class));
        Object[] parameters = enrichClassAndResolveMethod(instance, testMethod);

        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.METHOD,
            AnnotationMocks.drone(), AnnotationMocks.methodArgumentOneQualifier());
        verifyDronePointInstantiated(true, context, dronePoint);

        testMethod.invoke(instance, parameters);

        fire(new After(instance, testMethod));
        fire(new AfterClass(MethodEnrichedClass.class));
        verifyDronePointInstantiated(false, context, dronePoint, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testMethodQualiferUnregistered() throws Exception {
        getManager().getContext(ClassContext.class).activate(MethodEnrichedClassUnregistered.class);

        Object instance = new MethodEnrichedClassUnregistered();
        Method testMethod = MethodEnrichedClassUnregistered.class.getMethod("testMethodEnrichment", Object.class);

        getManager().getContext(TestContext.class).activate(instance);
        DroneContext context = fireAndVerifyBeforeSuiteProcess();

        fire(new BeforeClass(MethodEnrichedClassUnregistered.class));
        Object[] parameters = enrichClassAndResolveMethod(instance, testMethod);

        DronePoint<Object> dronePoint = new DronePointImpl<Object>(Object.class, DronePoint.Lifecycle.METHOD,
            AnnotationMocks.drone());
        verifyDronePointInstantiated(true, context, dronePoint);

        testMethod.invoke(instance, parameters);

        fire(new After(instance, testMethod));
        fire(new AfterClass(MethodEnrichedClassUnregistered.class));
        verifyDronePointInstantiated(false, context, dronePoint, true);
    }

    @Test
    public void testClassWithoutArquillianLifecycle() throws Exception {
        verifyTestClassWithoutArquillianLifecycle(true);
    }

    @Test
    public void testClassWithoutArquillianLifecycleWithSkipDroneCreationTrue() throws Exception {
        System.setProperty(ARQUILLIAN_DRONE_CREATION_PROPERTY, "true");
        verifyTestClassWithoutArquillianLifecycle(false);
    }

    @Test
    public void testClassWithoutArquillianLifecycleWithSkipDroneCreationFalse() throws Exception {
        System.setProperty(ARQUILLIAN_DRONE_CREATION_PROPERTY, "false");
        verifyTestClassWithoutArquillianLifecycle(true);
    }

    private void verifyTestClassWithoutArquillianLifecycle(boolean shouldBeInstantiated) throws Exception {
        Object instance = new NonArquillianClass();
        Method testMethod = NonArquillianClass.class.getMethod("someMethod", MockDrone.class);

        fire(new BeforeSuite());

        DroneContext context =
            getManager().getContext(ApplicationContext.class).getObjectStore().get(DroneContext.class);
        Assert.assertNotNull("DroneContext was created in the context", context);

        TestEnricher testEnricher = serviceLoader.onlyOne(TestEnricher.class);
        testEnricher.enrich(instance);
        Object[] parameters = testEnricher.resolve(testMethod);

        DronePoint<MockDrone> classDronePoint = new DronePointImpl<MockDrone>(MockDrone.class,
            DronePoint.Lifecycle.CLASS,
            AnnotationMocks.drone());
        verifyDronePointInstantiated(shouldBeInstantiated, context, classDronePoint);

        DronePoint<MockDrone> methodDronePoint = new DronePointImpl<MockDrone>(MockDrone.class,
            DronePoint.Lifecycle.METHOD,
            AnnotationMocks.drone());
        verifyDronePointInstantiated(shouldBeInstantiated, context, methodDronePoint);

        testMethod.invoke(instance, parameters);
    }

    /**
     * Verifies whether the given {@link DronePoint} is instantiated or not and throws an AssertException with an
     * appropriate message if necessary. It is verified in the context of creation of the instance (not destruction)
     *
     * @param shouldInstantiated
     *     Whether the given {@link DronePoint} should be instantiated
     * @param context
     *     a Drone context the given {@link DronePoint} should belong to
     * @param dronePoint
     *     a drone point to be verified
     */
    void verifyDronePointInstantiated(boolean shouldInstantiated, DroneContext context, DronePoint<?> dronePoint) {
        verifyDronePointInstantiated(shouldInstantiated, context, dronePoint, false);
    }

    /**
     * Verifies whether the given {@link DronePoint} is instantiated or not and throws an AssertException with an
     * appropriate message if necessary.
     *
     * @param shouldInstantiated
     *     Whether the given {@link DronePoint} should be instantiated
     * @param context
     *     a Drone context the given {@link DronePoint} should belong to
     * @param dronePoint
     *     a drone point to be verified
     * @param shouldDestroyed
     *     whether the given {@link DronePoint} should be destroyed - based on this parameter only
     *     the message is modified
     */
    void verifyDronePointInstantiated(boolean shouldInstantiated, DroneContext context, DronePoint<?> dronePoint,
        boolean shouldDestroyed) {
        if (shouldInstantiated) {
            Assert.assertTrue("Drone should be created", context.get(dronePoint).isInstantiated());
        } else {
            String message = shouldDestroyed ? "Drone should be destroyed" : "Drone should NOT be created";
            Assert.assertFalse(message, context.get(dronePoint).isInstantiated());
        }
    }

    /**
     * Fires the {@link BeforeSuite} event, retrieve a {@link DroneContext} and {@link DroneRegistry} instances and
     * asserts that they are not null. Asserts that the {@link Configurator} and {@link Instantiator} are mock type.
     *
     * @return the drone context instance
     */
    DroneContext fireAndVerifyBeforeSuiteProcess() {
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
        return context;
    }

    /**
     * Enriches the given instance of a test class and resolves the given test method
     *
     * @param instance
     *     a test class instance to be enriched
     * @param testMethod
     *     a test method to be resolved
     *
     * @return resolved parameters of the given test method
     */
    Object[] enrichClassAndResolveMethod(Object instance, Method testMethod) {
        fire(new Before(instance, testMethod));

        TestEnricher testEnricher = serviceLoader.onlyOne(TestEnricher.class);
        testEnricher.enrich(instance);
        return testEnricher.resolve(testMethod);
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
}
