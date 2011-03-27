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

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;

/**
 * Register of available {@link Configurator}s, {@link Instantiator}s and
 * {@link Destructor}s discovered via SPI.
 * 
 * Stores only one of them per type, so {@link WebTestRegistrar} is responsible
 * for selecting correct implementations.
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class DroneRegistry
{
   private Map<Class<?>, RegistryValue> registry = new HashMap<Class<?>, RegistryValue>();

   /**
    * Gets configurator for given object type
    * 
    * @param <T> Type of configurator object
    * @param type Configurator key
    * @return Configurator for objects of type <T>
    */
   @SuppressWarnings("unchecked")
   public <T> Configurator<T, ?> getConfiguratorFor(Class<T> type)
   {
      RegistryValue value = registry.get(type);
      if (value != null)
      {
         return (Configurator<T, ?>) value.configurator;
      }
      return null;
   }

   /**
    * Gets instantiator for given object type
    * 
    * @param <T> Type of instantiator object
    * @param key Instantiator key
    * @return Instantiator for objects of type <T>
    */
   @SuppressWarnings("unchecked")
   public <T> Instantiator<T, ?> getInstantiatorFor(Class<T> key)
   {
      RegistryValue value = registry.get(key);
      if (value != null)
      {
         return (Instantiator<T, ?>) value.instantiator;
      }
      return null;
   }

   /**
    * Gets descructor for given object type
    * 
    * @param <T> Type of destructor object
    * @param key Destructor key
    * @return Destructor for objects of type <T>
    */
   @SuppressWarnings("unchecked")
   public <T> Destructor<T> getDestructorFor(Class<T> key)
   {
      RegistryValue value = registry.get(key);
      if (value != null)
      {
         return (Destructor<T>) value.destructor;
      }
      return null;
   }

   /**
    * Registers a configurator for given object type
    * 
    * @param key Type to be registered
    * @param configurator Configurator to be stored
    * @return Modified registry
    */
   public DroneRegistry registerConfiguratorFor(Class<?> key, Configurator<?, ?> configurator)
   {
      RegistryValue entry = registry.get(key);
      if (entry != null)
      {
         entry.configurator = configurator;
      }
      else
      {
         registry.put(key, new RegistryValue().setConfigurator(configurator));
      }
      return this;
   }

   /**
    * Registers a instantiator for given object type
    * 
    * @param key Type to be registered
    * @param configurator Instantiator to be stored
    * @return Modified registry
    */
   public DroneRegistry registerInstantiatorFor(Class<?> key, Instantiator<?, ?> value)
   {
      RegistryValue entry = registry.get(key);
      if (entry != null)
      {
         entry.instantiator = value;
      }
      else
      {
         registry.put(key, new RegistryValue().setInstantiator(value));
      }
      return this;
   }

   /**
    * Registers a destructor for given object type
    * 
    * @param key Type to be registered
    * @param configurator Destructor to be stored
    * @return Modified registry
    */
   public DroneRegistry registerDestructorFor(Class<?> key, Destructor<?> value)
   {
      RegistryValue entry = registry.get(key);
      if (entry != null)
      {
         entry.destructor = value;
      }
      else
      {
         registry.put(key, new RegistryValue().setDestructor(value));
      }
      return this;
   }

   private static class RegistryValue
   {
      Configurator<?, ?> configurator;
      Instantiator<?, ?> instantiator;
      Destructor<?> destructor;

      /**
       * @param configurator the configurator to set
       */
      public RegistryValue setConfigurator(Configurator<?, ?> configurator)
      {
         this.configurator = configurator;
         return this;
      }

      /**
       * @param instantiator the instantiator to set
       */
      public RegistryValue setInstantiator(Instantiator<?, ?> instantiator)
      {
         this.instantiator = instantiator;
         return this;
      }

      /**
       * @param destructor the destructor to set
       */
      public RegistryValue setDestructor(Destructor<?> destructor)
      {
         this.destructor = destructor;
         return this;
      }

   }
}
