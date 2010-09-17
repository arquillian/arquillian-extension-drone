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

import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.spi.Instantiator;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * A handler which destroys a Selenium browser, Selenium WebDriver or Cheiron
 * instance from the current context. <br/>
 * <br/>
 * <b>Imports:</b><br/> {@link Selenium}<br/>
 * 
 * @{link {@link SeleniumHolder}<br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see Selenium
 * @see SeleniumHolder
 * 
 */
public class SeleniumShutdownHandler implements EventHandler<ClassEvent>
{
   /*
    * (non-Javadoc)
    * 
    * @seeorg.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.
    * arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, ClassEvent event) throws Exception
   {
      clearContext(context, event.getTestClass());

   }

   @SuppressWarnings("unchecked")
   private void clearContext(Context context, TestClass testClass)
   {
      SeleniumHolder holder = context.get(SeleniumHolder.class);
      if (holder == null)
      {
         throw new IllegalArgumentException("There is no Selenium object to be destroyed, was Selenium properly started?");
      }

      for (Field f : SecurityActions.getFieldsWithAnnotation(testClass.getJavaClass(), Selenium.class))
      {
         Class<?> typeClass = f.getType();
         if (!holder.contains(typeClass))
            break;

         // we do check type safety dynamically
         Instantiator destroyer = holder.retrieveInstantiator(typeClass);
         destroyer.destroy(holder.retrieveSelenium(typeClass));
         holder.remove(typeClass);

      }
   }
}
