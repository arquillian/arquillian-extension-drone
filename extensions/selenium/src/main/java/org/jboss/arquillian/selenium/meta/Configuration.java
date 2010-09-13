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
package org.jboss.arquillian.selenium.meta;

/**
 * A generic way how to obtain configuration parameters from an arbitrary
 * key-value storage
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public interface Configuration
{
   /**
    * Retrieves a string mapped by key
    * 
    * @param key the key
    * @return The value or {@code null} if no such key is mapped
    */
   String getString(String key);

   /**
    * Retrieves a string mapped by key
    * 
    * @param key the key
    * @param defaultValue The value returned if no such key is mapped
    * @return The value or {@code defaultValue} is no such key is mapped
    */
   String getString(String key, String defaultValue);

   /**
    * Retrieves an integer mapped by key
    * 
    * @param key the key
    * @param defaultValue The value returned if no such key is mapped
    * @return The value or {@code defaultValue} is no such key is mapped
    * @throws NumberFormatException If value mapped by key does not represent an
    *            integer
    */
   int getInt(String key, int defaultValue);

   /**
    * Retrieves an integer mapped by key
    * 
    * @param key the key
    * @return The value or {@code -1} if no such key is mapped
    */
   int getInt(String key);

}
