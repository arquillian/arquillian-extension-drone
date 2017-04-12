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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.impl.mockdrone.CachingMockDroneFactory;
import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneInstanceEnhancer;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.context.ClassContext;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests Configurator precedence and its retrieval chain, uses qualifier as well.
 * <p/>
 * Additionally tests DroneTestEnricher
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class EnhancerTestCase extends AbstractTestTestBase {
    private static final String DIFFERENT_FIELD = "ArquillianDescriptor @DifferentMock";
    private static final String METHOD_ARGUMENT_ONE_FIELD = "ArquillianDescriptor @MethodArgumentOne";

    @Mock
    private ServiceLoader serviceLoader;

    @Mock
    private MockDrone enhanced1;

    @Mock
    private MockDrone enhanced2;

    private MockDrone notEnhanced;
    private MockDrone deEnhanced;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DroneLifecycleManager.class);
        extensions.add(DroneRegistrar.class);
        extensions.add(DroneConfigurator.class);
        extensions.add(DroneEnhancer.class);
        extensions.add(DroneTestEnricher.class);
        extensions.add(DroneDestructor.class);
    }

    @SuppressWarnings("rawtypes")
    @org.junit.Before
    public void setUp() {
        ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone-different")
            .property("field", DIFFERENT_FIELD).extension("mockdrone-methodargumentone")
            .property("field", METHOD_ARGUMENT_ONE_FIELD);

        TestEnricher testEnricher = new DroneTestEnricher();
        getManager().inject(testEnricher);

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);

        CachingMockDroneFactory factory = new CachingMockDroneFactory();

        Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(
            Arrays.<Configurator>asList(factory));
        Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(
            Arrays.<Configurator>asList(factory));
        Mockito.when(serviceLoader.all(Instantiator.class)).thenReturn(
            Arrays.<Instantiator>asList(factory));
        Mockito.when(serviceLoader.all(Destructor.class)).thenReturn(Arrays.<Destructor>asList(factory));

        Mockito.when(serviceLoader.all(DroneInstanceEnhancer.class)).thenReturn(
            Arrays.<DroneInstanceEnhancer>asList(new MockDroneEnhancer2(), new MockDroneEnhancer1()));
        Mockito.when(serviceLoader.onlyOne(TestEnricher.class)).thenReturn(testEnricher);
        Mockito.when(enhanced2.getField()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                return notEnhanced.getField();
            }
        });

        notEnhanced = null;
        deEnhanced = null;
    }

    @Test
    public void testClassLevel() throws Exception {
        getManager().getContext(ClassContext.class).activate(EnrichedClass.class);
        fire(new BeforeSuite());

        DroneContext context = getManager().getContext(ApplicationContext.class).getObjectStore().get(DroneContext
            .class);
        Assert.assertNotNull("DroneContext created in the context", context);

        fire(new BeforeClass(EnrichedClass.class));

        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.CLASS,
            AnnotationMocks.drone());

        MockDrone drone = context.get(dronePoint).getInstance();

        assertEquals("both enhancerns were applied", enhanced2, drone);
        assertThat("the initial instance provided by Drone was not enhanced", notEnhanced, is(not(nullValue())));

        fire(new AfterClass(EnrichedClass.class));

        assertFalse(context.get(dronePoint).isInstantiated());
        assertThat(notEnhanced, equalTo(deEnhanced));
    }

    @Test
    public void testMethodLevel() throws Exception {
        getManager().getContext(ClassContext.class).activate(MethodEnrichedClass.class);

        Object instance = new MethodEnrichedClass();
        Method testMethod = MethodEnrichedClass.class.getMethod("testMethodEnrichment", MockDrone.class);

        getManager().getContext(TestContext.class).activate(instance);
        fire(new BeforeSuite());

        DroneContext context = getManager()
            .getContext(ApplicationContext.class).getObjectStore().get(DroneContext.class);
        Assert.assertNotNull("DroneContext created in the context", context);

        fire(new BeforeClass(MethodEnrichedClass.class));
        fire(new Before(instance, testMethod));

        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.METHOD,
            AnnotationMocks.drone(), AnnotationMocks.methodArgumentOneQualifier());

        TestEnricher testEnricher = serviceLoader.onlyOne(TestEnricher.class);

        testEnricher.enrich(instance);
        Object[] parameters = testEnricher.resolve(testMethod);

        assertTrue("Drone is created", context.get(dronePoint).isInstantiated());
        assertEquals("Drone was enhanced with both enhancers", enhanced2, context.get(dronePoint).getInstance());
        assertNotNull(notEnhanced);

        testMethod.invoke(instance, parameters);

        fire(new After(instance, testMethod));
        assertFalse("Drone was destroyed", context.get(dronePoint).isInstantiated());
        assertEquals(deEnhanced, notEnhanced);
    }

    static class EnrichedClass {
        @Drone
        MockDrone unused;
    }

    static class MethodEnrichedClass {

        public void testMethodEnrichment(@Drone @MethodArgumentOne MockDrone unused) {
            Assert.assertNotNull("Mock drone instance was created", unused);
            Assert.assertEquals("MockDroneConfiguration is set via ArquillianDescriptor", METHOD_ARGUMENT_ONE_FIELD,
                unused.getField());
        }
    }

    private class MockDroneEnhancer1 implements DroneInstanceEnhancer<MockDrone> {

        @Override
        public int getPrecedence() {
            return 200;
        }

        @Override
        public boolean canEnhance(InstanceOrCallableInstance instance, Class<?> droneType,
            Class<? extends Annotation> qualifier) {
            return MockDrone.class.isAssignableFrom(droneType);
        }

        @Override
        public MockDrone enhance(MockDrone instance, Class<? extends Annotation> qualifier) {
            assertThat(instance, not(equalTo(enhanced1)));
            assertThat(instance, not(equalTo(enhanced2)));
            notEnhanced = instance;
            return enhanced1;
        }

        @Override
        public MockDrone deenhance(MockDrone enhancedInstance, Class<? extends Annotation> qualifier) {
            assertThat(enhancedInstance, equalTo(enhanced1));
            deEnhanced = notEnhanced;
            return notEnhanced;
        }
    }

    private class MockDroneEnhancer2 implements DroneInstanceEnhancer<MockDrone> {

        @Override
        public int getPrecedence() {
            return 0;
        }

        @Override
        public boolean canEnhance(InstanceOrCallableInstance instance, Class<?> droneType,
            Class<? extends Annotation> qualifier) {
            return true;
        }

        @Override
        public MockDrone enhance(MockDrone instance, Class<? extends Annotation> qualifier) {
            assertThat(instance, equalTo(enhanced1));
            return enhanced2;
        }

        @Override
        public MockDrone deenhance(MockDrone enhancedInstance, Class<? extends Annotation> qualifier) {
            assertThat(enhancedInstance, equalTo(enhanced2));
            return enhanced1;
        }
    }
}
