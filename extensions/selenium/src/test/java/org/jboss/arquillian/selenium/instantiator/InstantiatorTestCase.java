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
package org.jboss.arquillian.selenium.instantiator;

import java.util.Arrays;

import org.jboss.arquillian.impl.XmlConfigurationBuilder;
import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.selenium.SeleniumExtensionConfiguration;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.event.SeleniumHolder;
import org.jboss.arquillian.selenium.event.SeleniumStartupHandler;
import org.jboss.arquillian.selenium.example.AbstractTestCase;
import org.jboss.arquillian.selenium.spi.Instantiator;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.event.suite.TestEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.thoughtworks.selenium.CommandProcessor;
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
public class InstantiatorTestCase extends AbstractTestCase
{
   @Mock
   private ServiceLoader serviceLoader;

   // injection point
   @Selenium
   @SuppressWarnings("unused")
   private DefaultSelenium seleniumField;

   @Test
   @SuppressWarnings("unchecked")
   public void testPrecedence() throws Exception
   {
      Mockito.when(serviceLoader.all(Instantiator.class))
            .thenReturn(Arrays.<Instantiator> asList(
                  new DefaultSeleniumInstantiator(),
                  new WebDriverInstantiator(),
                  new MockInstantiator()));

      TestContext context = new TestContext(new ClassContext(new SuiteContext(serviceLoader)));
      TestEvent event = new TestEvent(this, getClass().getMethod("testPrecedence"));

      context.add(Configuration.class, new XmlConfigurationBuilder("arquillian.xml").build());
      
      SeleniumStartupHandler handler = new SeleniumStartupHandler();

      handler.callback(context, event);

      SeleniumHolder holder = context.get(SeleniumHolder.class);
      Assert.assertNotNull("Selenium object holder was created in context", holder);

      DefaultSelenium selenium = holder.retrieveSelenium(DefaultSelenium.class);

      Assert.assertTrue("Selenium was instantiated by MockInstantiator", selenium instanceof MockSelenium);

   }

   /**
    * Mock instantiator which should take precedence over default one
    * 
    * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
    * 
    */
   class MockInstantiator implements Instantiator<DefaultSelenium>
   {

      /*
       * (non-Javadoc)
       * 
       * @see org.jboss.arquillian.selenium.spi.Instantiator#getPrecedence()
       */
      public int getPrecedence()
      {
         return 10;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.jboss.arquillian.selenium.instantiator.Instantiator#create()
       */
      public DefaultSelenium create(SeleniumExtensionConfiguration configuration)
      {
         DefaultSelenium selenium = new MockSelenium();
         return selenium;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.jboss.arquillian.selenium.instantiator.Instantiator#destroy(java.lang.Object)
       */
      public void destroy(DefaultSelenium instance)
      {
      }
   }

   /**
    * Mock Selenium to test custom instantiator
    * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
    * 
    */
   class MockSelenium extends DefaultSelenium
   {

      /**
       * @param processor
       */
      public MockSelenium()
      {
         super(new MockCommandProcessor());
      }

   }

   /**
    * Mock command processor to mimic Selenium's behaviour
    * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
    * 
    */
   class MockCommandProcessor implements CommandProcessor
   {

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#doCommand(java.lang.String, java.lang.String[])
       */
      public String doCommand(String command, String[] args)
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#getBoolean(java.lang.String, java.lang.String[])
       */
      public boolean getBoolean(String string, String[] strings)
      {
         return false;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#getBooleanArray(java.lang.String, java.lang.String[])
       */
      public boolean[] getBooleanArray(String string, String[] strings)
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#getNumber(java.lang.String, java.lang.String[])
       */
      public Number getNumber(String string, String[] strings)
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#getNumberArray(java.lang.String, java.lang.String[])
       */
      public Number[] getNumberArray(String string, String[] strings)
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#getRemoteControlServerLocation()
       */
      public String getRemoteControlServerLocation()
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#getString(java.lang.String, java.lang.String[])
       */
      public String getString(String string, String[] strings)
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#getStringArray(java.lang.String, java.lang.String[])
       */
      public String[] getStringArray(String string, String[] strings)
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#setExtensionJs(java.lang.String)
       */
      public void setExtensionJs(String extensionJs)
      {
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#start()
       */
      public void start()
      {
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#start(java.lang.String)
       */
      public void start(String optionsString)
      {
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#start(java.lang.Object)
       */
      public void start(Object optionsObject)
      {
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.thoughtworks.selenium.CommandProcessor#stop()
       */
      public void stop()
      {
      }

   }

}