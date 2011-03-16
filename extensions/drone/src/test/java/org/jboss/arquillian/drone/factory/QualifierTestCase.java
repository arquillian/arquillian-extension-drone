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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.arquillian.drone.annotation.Drone;
import org.jboss.arquillian.drone.configuration.SeleniumConfiguration;
import org.jboss.arquillian.drone.example.AbstractTestCase;
import org.jboss.arquillian.drone.factory.DefaultSeleniumFactory;
import org.jboss.arquillian.drone.factory.WebDriverFactory;
import org.jboss.arquillian.drone.impl.DroneConfigurator;
import org.jboss.arquillian.drone.impl.DroneContext;
import org.jboss.arquillian.drone.impl.DroneRegistrar;
import org.jboss.arquillian.drone.impl.DroneRegistry;
import org.jboss.arquillian.drone.impl.MethodContext;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.ManagerImpl;
import org.jboss.arquillian.impl.core.context.ClassContextImpl;
import org.jboss.arquillian.impl.core.context.SuiteContextImpl;
import org.jboss.arquillian.impl.core.spi.context.ApplicationContext;
import org.jboss.arquillian.impl.core.spi.context.ClassContext;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Tests Configurator precedence and its retrieval chain, uses qualifier as well
 * 
 * 
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class QualifierTestCase extends AbstractTestCase
{
   @Mock
   private ServiceLoader serviceLoader;

   @Drone
   @Different
   DefaultSelenium unused;

   private ManagerImpl manager;

   @Before
   public void create()
   {
      manager = ManagerBuilder.from().context(SuiteContextImpl.class).context(ClassContextImpl.class).extensions(DroneRegistrar.class, DroneConfigurator.class).create();

      ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class)
         .extension("selenium-different")
            .property("browser", "*testbrowser").property("url", "http://localhost:8888")
         .extension("selenium-methodargumentone")
            .property("browser", "*footestbrowser");

      manager.getContext(ApplicationContext.class).getObjectStore().add(ServiceLoader.class, serviceLoader);
      manager.getContext(ApplicationContext.class).getObjectStore().add(ArquillianDescriptor.class, desc);

      manager.getContext(SuiteContext.class).activate();
      manager.getContext(ClassContext.class).activate(this.getClass());
   }

   @After
   public void destroy()
   {
      manager.getContext(ClassContext.class).deactivate();
      manager.getContext(ClassContext.class).destroy(this.getClass());

      manager.getContext(SuiteContext.class).deactivate();
      manager.getContext(SuiteContext.class).destroy();
      manager.shutdown();
   }

   @Test
   @SuppressWarnings("rawtypes")
   public void testQualifer() throws Exception
   {
      Mockito.when(serviceLoader.all(Configurator.class))
         .thenReturn(Arrays.<Configurator> asList(new DefaultSeleniumFactory(), new WebDriverFactory(), new MockConfigurator()));

      manager.fire(new BeforeSuite());

      DroneRegistry registry = manager.getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
      Assert.assertNotNull("Drone registry was created in the context", registry);

      Assert.assertTrue("Configurator is of mock type", registry.getConfiguratorFor(DefaultSelenium.class) instanceof MockConfigurator);

      manager.fire(new BeforeClass(this.getClass()));

      DroneContext context = manager.getContext(ClassContext.class).getObjectStore().get(DroneContext.class);
      Assert.assertNotNull("Drone object holder was created in the context", context);

      SeleniumConfiguration configuration = context.get(SeleniumConfiguration.class);
      Assert.assertNull("There is no SeleniumConfiguration with @Default qualifier", configuration);

      configuration = context.get(SeleniumConfiguration.class, Different.class);
      Assert.assertNotNull("SeleniumConfiguration is stored with @Different qualifier", configuration);

      Assert.assertEquals("SeleniumConfiguration has *testbrowser set as browser", "*testbrowser", configuration.getBrowser());
      Assert.assertEquals("SeleniumConfiguration has http://127.0.0.1:8080 as url", "http://127.0.0.1:8080", configuration.getUrl());

   }
/*
   @Test
   @SuppressWarnings("rawtypes")
   public void testMethodQualifer(@Drone @MethodArgumentOne DefaultSelenium unused) throws Exception
   {
      Mockito.when(serviceLoader.all(Configurator.class))
         .thenReturn(Arrays.<Configurator> asList(new DefaultSeleniumFactory(), new WebDriverFactory(), new MockConfigurator()));

      manager.fire(new BeforeSuite());

      DroneRegistry registry = manager.getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
      Assert.assertNotNull("Drone registry was created in the context", registry);

      Assert.assertTrue("Configurator is of mock type", registry.getConfiguratorFor(DefaultSelenium.class) instanceof MockConfigurator);

      manager.fire(new BeforeClass(this.getClass()));

      
      DroneContext dc = manager.getContext(ClassContext.class).getObjectStore().get(DroneContext.class);
      Assert.assertNotNull("Drone object holder was created in the context", dc);
      MethodContext mc = manager.getContext(ClassContext.class).getObjectStore().get(MethodContext.class);
      Assert.assertNotNull("Method context object holder was created in the context", mc);

      Method thisMethod = this.getClass().getMethod("testMethodQualifier", DefaultSelenium.class);
      manager.fire(new org.jboss.arquillian.spi.event.suite.After(this, thisMethod));
      
      DroneContext context = mc.get(thisMethod);
      Assert.assertNotNull("Method context was stored", context);
      
      SeleniumConfiguration configuration = context.get(SeleniumConfiguration.class);
      Assert.assertNull("There is no SeleniumConfiguration with @Default qualifier", configuration);

      configuration = context.get(SeleniumConfiguration.class, MethodArgumentOne.class);
      Assert.assertNotNull("SeleniumConfiguration is stored with @MethodArgumentOne qualifier", configuration);

      Assert.assertEquals("SeleniumConfiguration has *testbrowser set as browser", "*footestbrowser", configuration.getBrowser());
      Assert.assertEquals("SeleniumConfiguration has http://127.0.0.1:8080 as url", "http://127.0.0.1:8080", configuration.getUrl());

   }
*/   
   class MockConfigurator implements Configurator<DefaultSelenium, SeleniumConfiguration>
   {

      /*
       * (non-Javadoc)
       * 
       * @see org.jboss.arquillian.selenium.spi.Sortable#getPrecedence()
       */
      public int getPrecedence()
      {
         return 10;
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.jboss.arquillian.selenium.spi.Configurator#createConfiguration(
       * org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor,
       * java.lang.Class)
       */
      public SeleniumConfiguration createConfiguration(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier)
      {
         System.setProperty("arquillian.selenium.different.url", "http://127.0.0.1:8080");

         SeleniumConfiguration configuration = new SeleniumConfiguration();
         configuration.configure(descriptor, qualifier);
         return configuration;
      }

   }

}
