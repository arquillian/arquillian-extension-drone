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

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.spi.Sortable;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Registar of factories. Registers every {@link Configurator},
 * {@link Instantiator} and {@link Destructor} found via SPI. Only ones with
 * highes precedence are kept. See {@link Sortable#getPrecedence()}
 * 
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link org.jboss.arquillian.core.spi.ServiceLoader}</li>
 * </ol>
 * 
 * <p>
 * Produces:
 * </p>
 * <ol>
 * <li>{@link DroneRegistry}</li>
 * </ol>
 * 
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link BeforeSuite}</li>
 * </ol>
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class DroneRegistrar
{
   @Inject
   @SuiteScoped
   private InstanceProducer<DroneRegistry> dronetRegistry;

   @Inject
   private Instance<ServiceLoader> serviceLoader;

   public void register(@Observes BeforeSuite event)
   {
      dronetRegistry.set(new DroneRegistry());
      registerConfigurators();
      registerInstantiators();
      registerDestructors();
   }

   private void registerConfigurators()
   {
      @SuppressWarnings("rawtypes")
      List<Configurator> list = new ArrayList<Configurator>(serviceLoader.get().all(Configurator.class));
      Collections.sort(list, SORTABLE_COMPARATOR);

      for (Configurator<?, ?> configurator : list)
      {
         Class<?> type = getFirstGenericParameterType(configurator.getClass(), Configurator.class);
         if (type != null)
         {
            dronetRegistry.get().registerConfiguratorFor(type, configurator);
         }
      }
   }

   public void registerInstantiators()
   {
      @SuppressWarnings("rawtypes")
      List<Instantiator> list = new ArrayList<Instantiator>(serviceLoader.get().all(Instantiator.class));
      Collections.sort(list, SORTABLE_COMPARATOR);

      for (Instantiator<?, ?> instantiator : list)
      {
         Class<?> type = getFirstGenericParameterType(instantiator.getClass(), Instantiator.class);
         if (type != null)
         {
            dronetRegistry.get().registerInstantiatorFor(type, instantiator);
         }
      }
   }

   public void registerDestructors()
   {
      @SuppressWarnings("rawtypes")
      List<Destructor> list = new ArrayList<Destructor>(serviceLoader.get().all(Destructor.class));
      Collections.sort(list, SORTABLE_COMPARATOR);

      for (Destructor<?> destructor : list)
      {
         Class<?> type = getFirstGenericParameterType(destructor.getClass(), Destructor.class);
         if (type != null)
         {
            dronetRegistry.get().registerDestructorFor(type, destructor);
         }
      }
   }

   private static Class<?> getFirstGenericParameterType(Class<?> clazz, Class<?> rawType)
   {
      for (Type interfaceType : clazz.getGenericInterfaces())
      {
         if (interfaceType instanceof ParameterizedType)
         {
            ParameterizedType ptype = (ParameterizedType) interfaceType;
            if (rawType.isAssignableFrom((Class<?>) ptype.getRawType()))
            {
               return (Class<?>) ptype.getActualTypeArguments()[0];
            }
         }
      }
      return null;
   }

   // comparator
   private static final Comparator<Sortable> SORTABLE_COMPARATOR = new Comparator<Sortable>()
   {
      public int compare(Sortable o1, Sortable o2)
      {
         return new Integer(o1.getPrecedence()).compareTo(new Integer(o2.getPrecedence()));
      }
   };

}
