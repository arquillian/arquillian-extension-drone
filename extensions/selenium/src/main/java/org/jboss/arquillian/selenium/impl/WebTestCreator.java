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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.event.WebTestConfigured;
import org.jboss.arquillian.selenium.spi.Instantiator;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;

/**
 * A handler which sets a cached instance of Selenium browser for fields
 * annotated with {@link Selenium}. <br/>
 * <b>Imports:</b><br/> {@link Selenium} <br/> {@link WebTestContext} <br/>
 * <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see WebTestContext
 * @see Selenium
 */
public class WebTestCreator
{
   private static final Logger log = Logger.getLogger(WebTestCreator.class.getName());

   @Inject
   private Instance<WebTestRegistry> registry;

   @Inject
   private Instance<WebTestContext> webTestContext;

   public void createWebTestBrowser(@Observes WebTestConfigured event)
   {
      Field field = event.getInjected();
      Class<?> typeClass = field.getType();
      Class<? extends Annotation> qualifier = event.getQualifier();
      Object configuration = event.getConfiguration();

      Instantiator<?> instantiator = registry.get().getInstantiator(typeClass);

      if (instantiator == null)
      {
         throw new IllegalArgumentException("No instantiator was found for object of type " + typeClass.getName());
      }
      if (log.isLoggable(Level.FINE))
      {
         log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence " + instantiator.getPrecedence());
      }

      webTestContext.get().add(typeClass, qualifier, instantiator.createInstance(configuration));
   }
}
