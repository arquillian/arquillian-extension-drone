/**
 * 
 */
package org.jboss.arquillian.selenium.impl;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.selenium.spi.Configurator;
import org.jboss.arquillian.selenium.spi.Destructor;
import org.jboss.arquillian.selenium.spi.Instantiator;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class WebTestRegistry
{
   private Map<Class<?>, RegistryValue> registry = new HashMap<Class<?>, WebTestRegistry.RegistryValue>();

   @SuppressWarnings("unchecked")
   public <T> Configurator<T> getConfigurator(Class<T> key)
   {
      RegistryValue value = registry.get(key);
      if (value != null)
      {
         return (Configurator<T>) value.configurator;
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public <T> Instantiator<T> getInstantiator(Class<T> key)
   {
      RegistryValue value = registry.get(key);
      if (value != null)
      {
         return (Instantiator<T>) value.instantiator;
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public <T> Destructor<T> getDestructor(Class<T> key)
   {
      RegistryValue value = registry.get(key);
      if (value != null)
      {
         return (Destructor<T>) value.destructor;
      }
      return null;
   }

   public WebTestRegistry registerConfigurator(Class<?> type, Configurator<?> configurator)
   {
      RegistryValue entry = registry.get(type);
      if (entry != null)
      {
         entry.configurator = configurator;
      }
      else
      {
         registry.put(type, new RegistryValue().setConfigurator(configurator));
      }
      return this;
   }

   public WebTestRegistry registerInstantiator(Class<?> key, Instantiator<?> value)
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

   public WebTestRegistry registerDestructor(Class<?> key, Destructor<?> value)
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
      Configurator<?> configurator;
      Instantiator<?> instantiator;
      Destructor<?> destructor;

      /**
       * @param configurator the configurator to set
       */
      public RegistryValue setConfigurator(Configurator<?> configurator)
      {
         this.configurator = configurator;
         return this;
      }

      /**
       * @param instantiator the instantiator to set
       */
      public RegistryValue setInstantiator(Instantiator<?> instantiator)
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
