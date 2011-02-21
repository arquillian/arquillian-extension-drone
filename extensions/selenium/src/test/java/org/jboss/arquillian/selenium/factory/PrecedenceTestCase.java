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
package org.jboss.arquillian.selenium.factory;

import java.util.Arrays;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.ManagerImpl;
import org.jboss.arquillian.impl.core.context.ClassContextImpl;
import org.jboss.arquillian.impl.core.context.SuiteContextImpl;
import org.jboss.arquillian.impl.core.spi.context.ApplicationContext;
import org.jboss.arquillian.impl.core.spi.context.ClassContext;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.configuration.SeleniumConfiguration;
import org.jboss.arquillian.selenium.example.AbstractTestCase;
import org.jboss.arquillian.selenium.impl.WebTestConfigurator;
import org.jboss.arquillian.selenium.impl.WebTestContext;
import org.jboss.arquillian.selenium.impl.WebTestRegistrar;
import org.jboss.arquillian.selenium.impl.WebTestRegistry;
import org.jboss.arquillian.selenium.spi.Configurator;
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
 * Tests Instantiator precedence and its retrieval chain.
 * 
 * 
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class PrecedenceTestCase extends AbstractTestCase
{
   @Mock
   private ServiceLoader serviceLoader;

   @Selenium
   DefaultSelenium unused;

   private ManagerImpl manager;

   @Before
   public void create()
   {
      manager = ManagerBuilder.from().context(SuiteContextImpl.class).context(ClassContextImpl.class).extensions(WebTestRegistrar.class, WebTestConfigurator.class).create();

      manager.getContext(ApplicationContext.class).getObjectStore().add(ServiceLoader.class, serviceLoader);
      manager.getContext(ApplicationContext.class).getObjectStore().add(ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class));

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
   public void testPrecedence() throws Exception
   {
      Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(Arrays.<Configurator> asList(new DefaultSeleniumFactory(), new WebDriverFactory(), new MockConfigurator()));

      manager.fire(new BeforeSuite());

      WebTestRegistry registry = manager.getContext(SuiteContext.class).getObjectStore().get(WebTestRegistry.class);
      Assert.assertNotNull("Web Test registry was created in the context", registry);

      Assert.assertTrue("Configurator is of mock type", registry.getConfigurator(DefaultSelenium.class) instanceof MockConfigurator);

      manager.fire(new BeforeClass(this.getClass()));

      WebTestContext context = manager.getContext(ClassContext.class).getObjectStore().get(WebTestContext.class);
      Assert.assertNotNull("Web Test object holder was created in the context", context);

      SeleniumConfiguration configuration = context.get(SeleniumConfiguration.class);
      Assert.assertEquals("SeleniumConfiguration has *testbrowser set as browser", "*testbrowser", configuration.getBrowser());
   }

   class MockConfigurator implements Configurator<DefaultSelenium>
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
       * org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor)
       */
      public Object createConfiguration(ArquillianDescriptor descriptor)
      {
         SeleniumConfiguration configuration = new SeleniumConfiguration();
         configuration.setBrowser("*testbrowser");
         return configuration;
      }

   }

}