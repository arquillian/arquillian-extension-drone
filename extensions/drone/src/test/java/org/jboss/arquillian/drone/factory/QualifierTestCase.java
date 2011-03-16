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
package org.jboss.arquillian.drone.factory;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.arquillian.drone.annotation.Drone;
import org.jboss.arquillian.drone.impl.DroneConfigurator;
import org.jboss.arquillian.drone.impl.DroneContext;
import org.jboss.arquillian.drone.impl.DroneRegistrar;
import org.jboss.arquillian.drone.impl.DroneRegistry;
import org.jboss.arquillian.drone.impl.DroneTestEnricher;
import org.jboss.arquillian.drone.impl.MethodContext;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.context.ApplicationContextImpl;
import org.jboss.arquillian.impl.core.context.ClassContextImpl;
import org.jboss.arquillian.impl.core.context.SuiteContextImpl;
import org.jboss.arquillian.impl.core.spi.Manager;
import org.jboss.arquillian.impl.core.spi.context.ClassContext;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests Configurator precedence and its retrieval chain, uses qualifier as well
 * 
 * 
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class QualifierTestCase
{
   private static final String DIFFERENT_FIELD = "ArquillianDescriptor @Different";
   private static final String METHOD_ARGUMENT_ONE_FIELD = "ArquillianDescriptor @MethodArgumentOne";

   @Mock
   private ServiceLoader serviceLoader;

   private Manager manager;

   @org.junit.Before
   public void createManager()
   {
      manager = ManagerBuilder.from()
            .context(ApplicationContextImpl.class)
            .context(SuiteContextImpl.class)
            .context(ClassContextImpl.class)
            .extensions(DroneRegistrar.class, DroneConfigurator.class, DroneTestEnricher.class).create();

      ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class)
            .extension("mockdrone-different")
            .property("field", DIFFERENT_FIELD)
            .extension("mockdrone-methodargumentone")
            .property("field", METHOD_ARGUMENT_ONE_FIELD);

      manager.bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
      manager.bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);

      manager.getContext(SuiteContext.class).activate();

   }

   @org.junit.After
   public void destroyManager()
   {
      manager.getContext(SuiteContext.class).deactivate();
      manager.getContext(SuiteContext.class).destroy();
      manager.shutdown();
   }

   @Test
   @SuppressWarnings("rawtypes")
   public void testQualifer() throws Exception
   {
      manager.getContext(ClassContext.class).activate(EnrichedClass.class);

      Mockito.when(serviceLoader.all(Configurator.class))
            .thenReturn(Arrays.<Configurator> asList(new DefaultSeleniumFactory(), new WebDriverFactory(), new MockDroneFactory()));

      manager.fire(new BeforeSuite());

      DroneRegistry registry = manager.getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
      Assert.assertNotNull("Drone registry was created in the context", registry);

      Assert.assertTrue("Configurator is of mock type", registry.getConfiguratorFor(MockDroneInstance.class) instanceof MockDroneFactory);

      manager.fire(new BeforeClass(EnrichedClass.class));

      DroneContext context = manager.getContext(ClassContext.class).getObjectStore().get(DroneContext.class);
      Assert.assertNotNull("Drone object holder was created in the context", context);

      MockDroneConfiguration configuration = context.get(MockDroneConfiguration.class);
      Assert.assertNull("There is no MockDroneConfiguration with @Default qualifier", configuration);

      configuration = context.get(MockDroneConfiguration.class, Different.class);
      Assert.assertNotNull("MockDroneConfiguration is stored with @Different qualifier", configuration);

      Assert.assertEquals("MockDroneConfiguration field is set via System properties", MockDroneFactory.FIELD_OVERRIDE, configuration.getField());

      manager.getContext(ClassContext.class).deactivate();
      manager.getContext(ClassContext.class).destroy(EnrichedClass.class);
   }

   @Test
   @SuppressWarnings("rawtypes")
   public void testMethodQualifer() throws Exception
   {
      TestEnricher testEnricher = new DroneTestEnricher();

      manager.getContext(ClassContext.class).activate(MethodEnrichedClass.class);

      Mockito.when(serviceLoader.all(Configurator.class))
            .thenReturn(Arrays.<Configurator> asList(new MockDroneFactory()));
      Mockito.when(serviceLoader.all(Instantiator.class))
            .thenReturn(Arrays.<Instantiator> asList(new MockDroneFactory()));
      Mockito.when(serviceLoader.all(Destructor.class))
            .thenReturn(Arrays.<Destructor> asList(new MockDroneFactory()));
      Mockito.when(serviceLoader.all(TestEnricher.class))
            .thenReturn(Arrays.<TestEnricher> asList(testEnricher));

      manager.fire(new BeforeSuite());

      DroneRegistry registry = manager.getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
      Assert.assertNotNull("Drone registry was created in the context", registry);

      Assert.assertTrue("Configurator is of mock type", registry.getConfiguratorFor(MockDroneInstance.class) instanceof MockDroneFactory);

      manager.fire(new BeforeClass(MethodEnrichedClass.class));

      MethodContext mc = manager.getContext(ClassContext.class).getObjectStore().get(MethodContext.class);
      Assert.assertNotNull("Method context object holder was created in the context", mc);

      Object instance = new MethodEnrichedClass();
      Method testMethod = MethodEnrichedClass.class.getMethod("testMethodEnrichment", MockDroneInstance.class);

      manager.inject(testEnricher);
      Object[] parameters = testEnricher.resolve(testMethod);
      testMethod.invoke(instance, parameters);
   }

   static class EnrichedClass
   {
      @Drone
      @Different
      MockDroneInstance unused;
   }

   static class MethodEnrichedClass
   {

      public void testMethodEnrichment(@Drone @MethodArgumentOne MockDroneInstance unused)
      {
         Assert.assertNotNull("Mock drone instance was created", unused);
         Assert.assertEquals("MockDroneConfiguration is set via ArquillianDescriptor", METHOD_ARGUMENT_ONE_FIELD, unused.getField());
      }
   }

}
