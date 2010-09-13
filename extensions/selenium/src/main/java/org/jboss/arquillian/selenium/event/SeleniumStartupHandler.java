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
package org.jboss.arquillian.selenium.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.instantiator.Instantiator;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * A handler which creates a Selenium browser, Selenium WebDriver or Cheiron
 * implementation and binds it to the current context. The instance is stored in
 * {@link SeleniumHolder}, which defines default implementation if no
 * configuration is found in arquillian.xml file<br/>
 * <br/>
 * <b>Imports:</b><br/> {@link Selenium}<br/>
 * <br/>
 * <b>Exports:</b><br/> {@link SeleniumHolder}</br> <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see Selenium
 * @see SeleniumHolder
 * 
 */
public class SeleniumStartupHandler implements EventHandler<ClassEvent>
{

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.
    * arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, ClassEvent event) throws Exception
   {
      prepareContext(context, event.getTestClass());
   }

   private void prepareContext(Context context, TestClass testClass)
   {
      SeleniumHolder holder = new SeleniumHolder();
      for (Field f : SecurityActions.getFieldsWithAnnotation(testClass.getJavaClass(), Selenium.class))
      {
         Class<?> typeClass = f.getType();
         if (holder.contains(typeClass))
            break;

         Selenium annotation = f.getAnnotation(Selenium.class);
         Class<?>[] instantiatorClasses = annotation.instantiator();
         createAndStoreSelenium(holder, instantiatorClasses, typeClass);
      }

      context.add(SeleniumHolder.class, holder);
   }

   private void createAndStoreSelenium(SeleniumHolder holder, Class<?>[] instantiatorClasses, Class<?> typeClass)
   {
      List<Method> creators = new ArrayList<Method>();
      for (Class<?> instantiatorClass : instantiatorClasses)
      {
         creators.addAll(SecurityActions.getMethodsWithSignature(instantiatorClass, "create", typeClass));
      }

      if (creators.size() == 0)
      {
         throw new RuntimeException("No instantiator method was found for object of type " + typeClass.getName());
      }
      if (creators.size() != 1)
      {
         throw new RuntimeException("Could not determine which instantiator method should be used to create object of type " + typeClass.getName() + " because there are multiple methods available.");
      }

      try
      {
         Instantiator<?> instantiator = SecurityActions.newInstance(creators.get(0).getDeclaringClass().getName(), new Class<?>[0], new Object[0], Instantiator.class);
         holder.hold(typeClass, typeClass.cast(creators.get(0).invoke(instantiator, new Object[0])));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to instantiate Selenium driver", e);
      }
   }
}
