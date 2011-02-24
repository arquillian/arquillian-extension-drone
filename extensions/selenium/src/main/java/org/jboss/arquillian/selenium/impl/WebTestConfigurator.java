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
package org.jboss.arquillian.selenium.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.event.WebTestConfigured;
import org.jboss.arquillian.selenium.spi.Configurator;
import org.jboss.arquillian.selenium.spi.WebTestConfiguration;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * Configurator of Web Test Configuration. Creates a configuration for every
 * field annotated with {@link Selenium}.
 * 
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link ArquillianDescriptor}</li>
 * <li>{@link WebTestRegistry}</li>
 * </ol>
 * 
 * <p>
 * Produces:
 * </p>
 * <ol>
 * <li>{@link WebTestContext}</li>
 * </ol>
 * 
 * <p>
 * Fires:
 * </p>
 * <ol>
 * <li>{@link WebTestConfigured}</li>
 * </ol>
 * 
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link BeforeClass}</li>
 * </ol>
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class WebTestConfigurator
{
   @Inject
   @ClassScoped
   private InstanceProducer<WebTestContext> webTestContext;

   @Inject
   private Instance<ArquillianDescriptor> arquillianDescriptor;

   @Inject
   private Instance<WebTestRegistry> registry;

   @Inject
   private Event<WebTestConfigured> afterConfiguration;

   public void configureWebTest(@Observes BeforeClass event)
   {

      // check if any field is @Selenium annotated
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(event.getTestClass().getJavaClass(), Selenium.class);
      if (fields.isEmpty())
      {
         return;
      }

      WebTestContext context = new WebTestContext();
      webTestContext.set(context);
      for (Field f : fields)
      {
         Class<?> typeClass = f.getType();
         Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

         Configurator<?, ?> configurator = registry.get().getConfiguratorFor(typeClass);
         if (configurator == null)
         {
            throw new IllegalArgumentException("No configurator was found for object of type " + typeClass.getName());
         }

         WebTestConfiguration<?> configuration = configurator.createConfiguration(arquillianDescriptor.get(), qualifier);
         webTestContext.get().add(configuration.getClass(), qualifier, configuration);
         afterConfiguration.fire(new WebTestConfigured(f, qualifier, configuration));
      }

   }

}
