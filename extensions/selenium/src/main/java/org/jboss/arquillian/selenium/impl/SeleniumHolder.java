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

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.selenium.spi.Instantiator;

/**
 * Holds Selenium object in cache. It is used to store Selenium context between
 * test method calls in Arquillian testing context. Holds Instantiator of given
 * object as well.
 * 
 * Generic approach allows to have an arbitrary implementation of Selenium,
 * varying from Selenium WebDriver to Ajocado.
 * 
 * Current implementation limits occurrence of the testing browser to one per
 * class. For instance, you can have {#link DefaultSelenium} and {#link WebDriver}
 * browsers in your test class, but you can't add another WebDriver.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see Instantiator
 */
public class SeleniumHolder
{
   // cache holder
   @SuppressWarnings("unchecked")
   private Map<Class<?>, Tuple> cache = new HashMap<Class<?>, Tuple>();

   /**
    * Stores an instance of Selenium in the holder.
    * 
    * @param clazz The class of the instance store
    * @param instance The instance to be stored
    */
   public <T> void hold(Class<?> clazz, T instance, Instantiator<?> instantiator)
   {
      cache.put(clazz, new Tuple<T>(instance, instantiator));
   }

   /**
    * Retrieves instance stored in holder by key {@code clazz}
    * 
    * @param <T> Type of the instance stored
    * @param clazz The key used to find the instance
    * @return The instance if found or {@code null} otherwise
    */
   public <T> T retrieveSelenium(Class<T> clazz)
   {
      @SuppressWarnings("unchecked")
      Tuple<T> tuple = cache.get(clazz);
      if (tuple == null)
      {
         return null;
      }

      return tuple.instance;
   }

   public <T> Instantiator<T> retrieveInstantiator(Class<T> clazz)
   {
      @SuppressWarnings("unchecked")
      Tuple<T> tuple = cache.get(clazz);
      if (tuple == null)
      {
         return null;
      }

      return tuple.instantiator;
   }

   /**
    * Checks if there is already an instance stored by {@code clazz} in the
    * holder
    * 
    * @param clazz The class to be checked
    * @return {@code true} if there is such instance, {@code false} otherwise
    */
   public boolean contains(Class<?> clazz)
   {
      return cache.containsKey(clazz);
   }

   /**
    * Removes the instance from holder
    * 
    * @param clazz Key of instance which should be removed
    */
   public void remove(Class<?> clazz)
   {
      cache.remove(clazz);
   }

   /**
    * Holder for both Selenium object and its instantiator
    * 
    * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
    * 
    * @param <T> This of instance stored in pair
    */
   private static class Tuple<T>
   {

      T instance;
      Instantiator<T> instantiator;

      Tuple(T instance, Instantiator<?> instantiator)
      {
         this.instance = instance;
         this.instantiator = cast(instantiator);
      }

      @SuppressWarnings("unchecked")
      private Instantiator<T> cast(Instantiator<?> instantiator)
      {
         return (Instantiator<T>) instantiator;
      }
   }

}
