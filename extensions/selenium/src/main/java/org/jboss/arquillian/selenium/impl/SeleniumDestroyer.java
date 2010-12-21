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
package org.jboss.arquillian.selenium.impl;

import java.lang.reflect.Field;

import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.spi.Instantiator;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.AfterClass;

/**
 * A handler which sets a cached instance of Selenium browser for fields annotated with {@link Selenium}. <br/>
 * <b>Imports:</b><br/> {@link Selenium} <br/> {@link SeleniumHolder} <br/>
 * <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see SeleniumHolder
 * @see Selenium
 */
public class SeleniumDestroyer
{
   @Inject
   @ClassScoped
   private Instance<SeleniumHolder> selenium;

   @SuppressWarnings("unchecked")
   public void destroySelenium(@Observes AfterClass event)
   {        
      Class<?> clazz = event.getTestClass().getJavaClass();
      for (Field f : SecurityActions.getFieldsWithAnnotation(clazz, Selenium.class))
      {
         Class<?> typeClass = f.getType();
         if (!selenium.get().contains(typeClass))
         {
            continue;
         }

         // we do check type safety dynamically
         Instantiator destroyer = selenium.get().retrieveInstantiator(typeClass);
         destroyer.destroy(selenium.get().retrieveSelenium(typeClass));
         selenium.get().remove(typeClass);
      }
   }

}
