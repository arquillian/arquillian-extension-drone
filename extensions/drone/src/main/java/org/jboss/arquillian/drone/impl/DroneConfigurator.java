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
package org.jboss.arquillian.drone.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.drone.annotation.Drone;
import org.jboss.arquillian.drone.event.DroneConfigured;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * Configurator of Drone Configuration. Creates a configuration for every
 * field annotated with {@link Drone}.
 * 
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link ArquillianDescriptor}</li>
 * <li>{@link DroneRegistry}</li>
 * </ol>
 * 
 * <p>
 * Produces:
 * </p>
 * <ol>
 * <li>{@link DroneContext}</li>
 * </ol>
 * 
 * <p>
 * Fires:
 * </p>
 * <ol>
 * <li>{@link DroneConfigured}</li>
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
public class DroneConfigurator
{
   @Inject
   @ClassScoped
   private InstanceProducer<DroneContext> webTestContext;

   @Inject
   private Instance<ArquillianDescriptor> arquillianDescriptor;

   @Inject
   private Instance<DroneRegistry> registry;

   @Inject
   private Event<DroneConfigured> afterConfiguration;

   public void configureWebTest(@Observes BeforeClass event)
   {

      // check if any field is @Drone annotated
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(event.getTestClass().getJavaClass(), Drone.class);
      if (fields.isEmpty())
      {
         return;
      }

      DroneContext context = new DroneContext();
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

         DroneConfiguration<?> configuration = configurator.createConfiguration(arquillianDescriptor.get(), qualifier);
         webTestContext.get().add(configuration.getClass(), qualifier, configuration);
         afterConfiguration.fire(new DroneConfigured(f, qualifier, configuration));
      }

   }

}
