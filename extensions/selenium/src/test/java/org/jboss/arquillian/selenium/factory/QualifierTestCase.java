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
package org.jboss.arquillian.selenium.factory;

import java.lang.annotation.Annotation;
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

   @Selenium @Different
   DefaultSelenium unused;

   private ManagerImpl manager;

   @Before
   public void create()
   {
      manager = ManagerBuilder.from().context(SuiteContextImpl.class).context(ClassContextImpl.class).extensions(WebTestRegistrar.class, WebTestConfigurator.class).create();

      ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class)
         .extension("selenium-different")
            .property("browser", "*testbrowser")
            .property("url", "http://localhost:8888");
      
      
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
      Mockito.when(serviceLoader.all(Configurator.class)).thenReturn(Arrays.<Configurator> asList(new DefaultSeleniumFactory(), new WebDriverFactory(), new MockConfigurator()));

      manager.fire(new BeforeSuite());

      WebTestRegistry registry = manager.getContext(SuiteContext.class).getObjectStore().get(WebTestRegistry.class);
      Assert.assertNotNull("Web Test registry was created in the context", registry);

      Assert.assertTrue("Configurator is of mock type", registry.getConfigurator(DefaultSelenium.class) instanceof MockConfigurator);

      manager.fire(new BeforeClass(this.getClass()));

      WebTestContext context = manager.getContext(ClassContext.class).getObjectStore().get(WebTestContext.class);
      Assert.assertNotNull("Web Test object holder was created in the context", context);

      SeleniumConfiguration configuration = context.get(SeleniumConfiguration.class);      
      Assert.assertNull("There is no SeleniumConfiguration with @Default qualifier", configuration);
      
      configuration = context.get(SeleniumConfiguration.class, Different.class);
      Assert.assertNotNull("SeleniumConfiguration is stored with @Different qualifier", configuration);
      
      Assert.assertEquals("SeleniumConfiguration has *testbrowser set as browser", "*testbrowser", configuration.getBrowser());
      Assert.assertEquals("SeleniumConfiguration has http://127.0.0.1:8080 as url", "http://127.0.0.1:8080", configuration.getUrl());
      
      
   }

   class MockConfigurator implements Configurator<DefaultSelenium,SeleniumConfiguration>
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

      /* (non-Javadoc)
       * @see org.jboss.arquillian.selenium.spi.Configurator#createConfiguration(org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor, java.lang.Class)
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
