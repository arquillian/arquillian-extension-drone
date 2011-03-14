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
package org.jboss.arquillian.drone.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.drone.annotation.Drone;
import org.jboss.arquillian.drone.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.openqa.selenium.server.SeleniumServer;

/**
 * Destructor of drone instances. Disposes instance of every field annotated
 * with {@link Drone}
 * 
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link SeleniumServerConfiguration}</li>
 * <li>{@link SeleniumServer}</li>
 * </ol>
 * 
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link AfterClass}</li>
 * </ol>
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class DroneDestructor
{
   private static final Logger log = Logger.getLogger(DroneDestructor.class.getName());

   @Inject
   private Instance<DroneRegistry> registry;

   @Inject
   private Instance<DroneContext> webTestContext;

   @SuppressWarnings("unchecked")
   public void destroySelenium(@Observes AfterClass event)
   {
      Class<?> clazz = event.getTestClass().getJavaClass();
      for (Field f : SecurityActions.getFieldsWithAnnotation(clazz, Drone.class))
      {
         Class<?> typeClass = f.getType();
         Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

         // must be defined as raw because instance type to be destroyer cannot
         // be determined in compile time
         @SuppressWarnings("rawtypes")
         Destructor destructor = registry.get().getDestructorFor(typeClass);
         if (destructor == null)
         {
            throw new IllegalArgumentException("No destructor was found for object of type " + typeClass.getName());
         }
         if (log.isLoggable(Level.FINE))
         {
            log.fine("Using destructor defined in class: " + destructor.getClass().getName() + ", with precedence " + destructor.getPrecedence());
         }
         destructor.destroyInstance(webTestContext.get().get(typeClass, qualifier));
         webTestContext.get().remove(typeClass, qualifier);
      }
   }

}
