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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.selenium.annotation.Default;

/**
 * Holds Selenium object in cache. It is used to store Selenium context between
 * test method calls in Arquillian testing context. Holds Instantiator of given
 * object as well.
 * 
 * Generic approach allows to have an arbitrary implementation of Selenium,
 * varying from Selenium WebDriver to Ajocado.
 * 
 * Current implementation limits occurrence of the testing browser to one per
 * class. For instance, you can have {#link DefaultSelenium} and {#link
 * WebDriver} browsers in your test class, but you can't add another WebDriver.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class WebTestContext
{
   // cache holder
   private Map<QualifiedKey, Object> cache = new ConcurrentHashMap<QualifiedKey, Object>();

   public <T> T get(Class<T> type)
   {
      return type.cast(cache.get(new QualifiedKey(type, Default.class)));
   }

   public <T> T get(Class<T> type, Class<? extends Annotation> qualifier)
   {
      return type.cast(cache.get(new QualifiedKey(type, qualifier)));
   }

   public <T> WebTestContext add(Class<T> type, T instance)
   {
      cache.put(new QualifiedKey(type, Default.class), instance);
      return this;
   }

   public <T> WebTestContext add(Class<?> type, Class<? extends Annotation> qualifier, T instance)
   {
      cache.put(new QualifiedKey(type, qualifier), instance);
      return this;
   }

   public WebTestContext remove(Class<?> type)
   {
      cache.remove(new QualifiedKey(type, Default.class));
      return this;
   }

   public WebTestContext remove(Class<?> type, Class<? extends Annotation> qualifier)
   {
      cache.remove(new QualifiedKey(type, qualifier));
      return this;
   }

   
   private static class QualifiedKey
   {
      private Class<?> type;
      private Class<? extends Annotation> qualifier;

      public QualifiedKey(Class<?> type, Class<? extends Annotation> qualifier)
      {
         this.type = type;
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
         result = prime * result + ((type == null) ? 0 : type.hashCode());
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
         if (type == null)
         {
            if (other.type != null)
               return false;
         }
         else if (!type.equals(other.type))
            return false;
         return true;
      }

   }

 

}
