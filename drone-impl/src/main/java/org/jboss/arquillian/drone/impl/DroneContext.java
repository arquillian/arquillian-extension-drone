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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.drone.annotation.Default;

/**
 * Holder of Drone context. It is able to store different instances of
 * drone instances as well as their configurations and to retrieve them during testing.
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class DroneContext
{
   // cache holder
   private Map<QualifiedKey, Object> cache = new ConcurrentHashMap<QualifiedKey, Object>();

   /**
    * Gets object stored under {@link Default} qualifier and given key
    * 
    * @param <T> Type of the object
    * @param key Key used to find the object
    * @return Object stored under given qualified key
    */
   public <T> T get(Class<T> key)
   {
      return key.cast(cache.get(new QualifiedKey(key, Default.class)));
   }

   /**
    * Gets object stored under given qualifier and given key
    * 
    * @param <T> Type of the object
    * @param key Key used to find the object
    * @param qualifier Qualifier used to find the object
    * @return Object stored under given qualified key
    */
   public <T> T get(Class<T> key, Class<? extends Annotation> qualifier)
   {
      return key.cast(cache.get(new QualifiedKey(key, qualifier)));
   }

   /**
    * Adds object under given key and {@link Default} qualifier
    * 
    * @param <T> Type of the object
    * @param key Key used to store the object
    * @param instance Object to be stored
    * @return Modified context
    */
   public <T> DroneContext add(Class<T> key, T instance)
   {
      cache.put(new QualifiedKey(key, Default.class), instance);
      return this;
   }

   /**
    * Adds object under given key and given qualifier
    * 
    * @param <T> Type of the object
    * @param key Key used to store the object
    * @param qualifier Qualifier used to store the object
    * @param instance Object to be stored
    * @return Modified context
    */
   public <T> DroneContext add(Class<?> key, Class<? extends Annotation> qualifier, T instance)
   {
      cache.put(new QualifiedKey(key, qualifier), instance);
      return this;
   }

   /**
    * Removes object under given key and {@link Default} qualifier
    * 
    * @param key Key used to find the object
    * @return Modified context
    */
   public DroneContext remove(Class<?> key)
   {
      cache.remove(new QualifiedKey(key, Default.class));
      return this;
   }

   /**
    * Removes object under given key and given qualifier
    * 
    * @param key Key used to find the object
    * @param qualifier Qualifier used to find the object
    * @return Modified context
    */
   public DroneContext remove(Class<?> key, Class<? extends Annotation> qualifier)
   {
      cache.remove(new QualifiedKey(key, qualifier));
      return this;
   }

   private static class QualifiedKey
   {
      private Class<?> key;
      private Class<? extends Annotation> qualifier;

      public QualifiedKey(Class<?> key, Class<? extends Annotation> qualifier)
      {
         this.key = key;
         this.qualifier = qualifier;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((qualifier == null) ? 0 : qualifier.hashCode());
         result = prime * result + ((key == null) ? 0 : key.hashCode());
         return result;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         QualifiedKey other = (QualifiedKey) obj;
         if (qualifier == null)
         {
            if (other.qualifier != null)
               return false;
         }
         else if (!qualifier.equals(other.qualifier))
            return false;
         if (key == null)
         {
            if (other.key != null)
               return false;
         }
         else if (!key.equals(other.key))
            return false;
         return true;
      }

   }

}
