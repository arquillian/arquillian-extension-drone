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
package org.jboss.arquillian.selenium.instantiator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.selenium.spi.Instantiator;

/**
 * Utility to check and sort instantiators according to different conditions
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see Instantiator
 */
public class InstantiatorUtil
{
   private static final Logger log = Logger.getLogger(InstantiatorUtil.class.getName());

   /**
    * Checks what instantiators in the collection can instantiate {@code needle} and return a list with their instance
    * @param <T> Type of needle
    * @param collection The collection to be filtered
    * @param needle The class type which we want to instantiate
    * @return A list of instantiators which can instantiate given class
    */
   @SuppressWarnings("unchecked")
   public static <T> List<Instantiator<T>> filter(Collection<Instantiator> collection, Class<T> needle)
   {

      List<Instantiator<T>> list = new ArrayList<Instantiator<T>>();
      for (Instantiator i : collection)
      {
         for (Type type : i.getClass().getGenericInterfaces())
         {
            if (type instanceof ParameterizedType)
            {
               ParameterizedType ptype = (ParameterizedType) type;
               if (Instantiator.class.equals(ptype.getRawType()))
               {
                  // instantiator interface has only one parameter type possible
                  if (needle.isAssignableFrom((Class<?>) ptype.getActualTypeArguments()[0]))
                  {
                     // this cast is unchecked
                     list.add((Instantiator<T>) i);
                  }
               }
            }
         }
      }

      if (log.isLoggable(Level.FINE))
      {
         log.fine("There were totally " + collection.size() + " Selenium Instantiator registered, after filtering for '" + needle.getName() + "' were " + list.size() + " left.");
      }

      return list;
   }

   /**
    * Sorts a list of instantiators and returns the one with highest precedence
    * @param <T> A type to be instantiated
    * @param list A list of instantiators to sort
    * @return The instantiator with highest priority or {@code null} if list was empty
    */
   public static <T> Instantiator<T> highest(List<Instantiator<T>> list)
   {

      if (list.isEmpty())
         return null;

      // sort and return last one, because we want the highest precedence
      Collections.sort(list, INSTANTIATOR_COMPARATOR);

      Instantiator<T> candidate = list.get(list.size() - 1);

      if (log.isLoggable(Level.FINE))
      {
         log.fine("The implementation " + candidate.getClass() + " was chosen between " + list.size() + " instantiators/destroyers because of precedence " + candidate.getPrecedence() + ".");
      }

      return list.get(list.size() - 1);
   }

   /**
    * Checks what instantiators can instantiate {@code needle}, sorts them and returns
    * the one which the highest precedence
    * @param <T> A type to be instantiated
    * @param collection A list of instantiators to sort
    * @param needle The class type which we want to instantiate
    * @return The instantiator with highest priority or {@code null} if list was empty
    * @return
    */
   @SuppressWarnings("unchecked")
   public static <T> Instantiator<T> highest(Collection<Instantiator> collection, Class<T> needle)
   {
      return highest(filter(collection, needle));
   }

   // comparator
   private static final Comparator<Instantiator<?>> INSTANTIATOR_COMPARATOR = new Comparator<Instantiator<?>>()
   {
      public int compare(Instantiator<?> o1, Instantiator<?> o2)
      {
         return new Integer(o1.getPrecedence()).compareTo(new Integer(o2.getPrecedence()));
      }
   };

}
