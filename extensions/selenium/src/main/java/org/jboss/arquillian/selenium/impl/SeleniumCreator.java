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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.selenium.SeleniumConfiguration;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.instantiator.InstantiatorUtil;
import org.jboss.arquillian.selenium.spi.Instantiator;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * A handler which sets a cached instance of Selenium browser for fields annotated with {@link Selenium}. <br/>
 * <b>Imports:</b><br/> {@link Selenium} <br/> {@link SeleniumHolder} <br/>
 * <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see SeleniumHolder
 * @see Selenium
 */
public class SeleniumCreator
{
   private static final Logger log = Logger.getLogger(SeleniumCreator.class.getName());

   @Inject
   private Instance<SeleniumConfiguration> seleniumConfiguration;

   @Inject
   private Instance<ServiceLoader> serviceLoader;

   @Inject
   @ClassScoped
   private InstanceProducer<SeleniumHolder> selenium;

   public void createSelenium(@Observes BeforeClass event)
   {
      // check if any field is @Selenium annotated
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(event.getTestClass().getJavaClass(), Selenium.class);
      if (fields.isEmpty())
      {
         return;
      }

      SeleniumHolder holder = new SeleniumHolder();
      for (Field f : fields)
      {
         Class<?> typeClass = f.getType();
         if (holder.contains(typeClass))
            break;

         Instantiator<?> instantiator = InstantiatorUtil.highest(InstantiatorUtil.filter(serviceLoader.get().all(Instantiator.class), typeClass));
         if (instantiator == null)
         {
            throw new IllegalArgumentException("No creation method was found for object of type " + typeClass.getName());
         }

         if (log.isLoggable(Level.FINE))
         {
            log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence " + instantiator.getPrecedence());
         }
         holder.hold(typeClass, typeClass.cast(instantiator.create(seleniumConfiguration.get())), instantiator);
      }

      selenium.set(holder);
   }

}
