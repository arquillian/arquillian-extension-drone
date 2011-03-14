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
package org.jboss.arquillian.drone.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.configuration.api.ExtensionDef;

/**
 * Utility which maps Arquillian Descriptor and System Properties to a
 * configuration.
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see DroneConfiguration
 */
public class ConfigurationMapper
{
   private static final Logger log = Logger.getLogger(ConfigurationMapper.class.getName());

   private Map<String, String> nameValuePairs;

   private ConfigurationMapper()
   {
      this(Collections.<String, String> emptyMap());
   }

   private ConfigurationMapper(Map<String, String> nameValuePairs)
   {
      this.nameValuePairs = nameValuePairs;
   }

   /**
    * Maps a configuration using Arquillian Descriptor file
    * 
    * @param <T> Type of the configuration
    * @param descriptor Arquillian Descriptor
    * @param configuration Configuration object
    * @param qualifier Qualifier annotation
    * @return Configured configuration
    */
   public static <T extends DroneConfiguration<T>> T fromArquillianDescriptor(ArquillianDescriptor descriptor, T configuration, Class<? extends Annotation> qualifier)
   {
      String descriptorQualifier = configuration.getConfigurationName();
      String qualifierName = qualifier.getSimpleName().toLowerCase();

      ConfigurationMapper mapper = new ConfigurationMapper();
      mapper.setNameValuePairs(descriptor, descriptorQualifier, qualifierName);

      return mapper.mapFromArquillianDescriptor(configuration);
   }

   /**
    * Maps a configuration using System Properties
    * 
    * @param <T> Type of the configuration
    * @param configuration Configuration object
    * @param qualifier Qualifier annotation
    * @return Configured configuration
    */
   public static <T extends DroneConfiguration<T>> T fromSystemConfiguration(T configuration, Class<? extends Annotation> qualifier)
   {

      String descriptorQualifier = configuration.getConfigurationName();
      String qualifierName = qualifier.getSimpleName().toLowerCase();

      ConfigurationMapper mapper = new ConfigurationMapper();
      return mapper.mapFromSystemProperties(configuration, descriptorQualifier, qualifierName);
   }

   /**
    * Maps configuration values from Arquillian Descriptor
    * 
    * @param <T> A type of configuration
    * @param configuration Configuration object
    * @return Configured configuration of given type
    */
   private <T extends DroneConfiguration<T>> T mapFromArquillianDescriptor(T configuration)
   {
      List<Field> fields = SecurityActions.getAccessableFields(configuration.getClass());
      for (Field f : fields)
      {
         if (nameValuePairs.containsKey(f.getName()))
         {
            try
            {
               f.set(configuration, convert(box(f.getType()), nameValuePairs.get(f.getName())));
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName() + ") for " + configuration.getClass().getName() + " from Arquillan Descriptor", e);
            }
         }
      }
      return configuration;
   }

   /**
    * Maps configuration values from System properties
    * 
    * @param <T> A type of configuration
    * @param configuration Configuration object
    * @param descriptorQualifier A qualifier used for extension configuration in
    *           the descriptor
    * @param qualifierName Name of the qualifier passed
    * @return Configured configuration of given type
    */
   private <T extends DroneConfiguration<T>> T mapFromSystemProperties(T configuration, String descriptorQualifier, String qualifierName)
   {
      List<Field> fields = SecurityActions.getAccessableFields(configuration.getClass());

      String fullQualifiedPrefix = new StringBuilder("arquillian.").append(descriptorQualifier).append("-").append(qualifierName).append(".").toString();

      // get fields with qualifier included
      Map<Field, String> fieldValuePairs = getFieldValuePairs(fields, fullQualifiedPrefix);
      // get fields without qualifier included
      if (fieldValuePairs.isEmpty())
      {
         String prefix = new StringBuilder("arquillian.").append(descriptorQualifier).append(".").toString();
         fieldValuePairs = getFieldValuePairs(fields, prefix);
      }

      for (Map.Entry<Field, String> entry : fieldValuePairs.entrySet())
      {
         try
         {
            entry.getKey().set(configuration, convert(box(entry.getKey().getType()), entry.getValue()));
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName() + ") for " + configuration.getClass().getName() + " from System properties", e);
         }
      }

      return configuration;

   }

   /**
    * Maps fields to values using System configuration properties
    * 
    * @param fields Fields to be mapped
    * @param prefix System property prefix
    * @return Mapped values
    */
   private Map<Field, String> getFieldValuePairs(Collection<Field> fields, String prefix)
   {
      Map<Field, String> fieldValuePairs = new HashMap<Field, String>();
      for (Field f : fields)
      {
         String fieldName = keyTransform(new StringBuilder(prefix).append(f.getName()));
         String value = SecurityActions.getProperty(fieldName);
         if (value != null)
         {
            fieldValuePairs.put(f, value);
         }
      }
      return fieldValuePairs;

   }

   /**
    * Parses Arquillian Descriptor into property name - value pairs
    * 
    * @param descriptor An Arquillian Descriptor
    * @param descriptorQualifier A qualifier used for extension configuration in
    *           the descriptor
    * @param qualifierName Name of the qualifier passed
    */
   private void setNameValuePairs(ArquillianDescriptor descriptor, String descriptorQualifier, String qualifierName)
   {
      String fullDescriptorQualifier = new StringBuilder(descriptorQualifier).append("-").append(qualifierName).toString();

      ExtensionDef match = null;
      for (ExtensionDef extension : descriptor.getExtensions())
      {
         if (fullDescriptorQualifier.equals(extension.getExtensionName()))
         {
            this.nameValuePairs = extension.getExtensionProperties();
            if (log.isLoggable(Level.FINE))
            {
               log.fine("Using <extension qualifier=\"" + extension.getExtensionProperties() + "\"> for Drone Configuration");
            }
            return;
         }
         else if (descriptorQualifier.equals(extension.getExtensionName()))
         {
            match = extension;
         }
      }

      // found generic only
      if (match != null)
      {
         this.nameValuePairs = match.getExtensionProperties();
         if (log.isLoggable(Level.FINE))
         {
            log.fine("Using <extension qualifier=\"" + match.getExtensionProperties() + "\"> for Drone Configuration");
         }
         return;
      }
   }

   /**
    * Maps a field name to a property.
    * 
    * Replaces camel case with a dot ('.') and lower case character, replaces
    * other non digit and non letter characters with a dot (').
    * 
    * @param fieldName The name of field
    * @return Corresponding property name
    */
   private String keyTransform(String fieldName)
   {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < fieldName.length(); i++)
      {
         char c = fieldName.charAt(i);
         if (Character.isUpperCase(c))
         {
            sb.append('.').append(Character.toLowerCase(c));
         }
         else if (!Character.isLetterOrDigit(c))
         {
            sb.append('.');
         }
         else
         {
            sb.append(c);
         }
      }

      return sb.toString();
   }

   private String keyTransform(StringBuilder fieldName)
   {
      return keyTransform(fieldName.toString());
   }

   /**
    * A helper boxing method. Returns boxed class for a primitive class
    * 
    * @param primitive A primitive class
    * @return Boxed class if class was primitive, unchanged class in other cases
    */
   private Class<?> box(Class<?> primitive)
   {
      if (!primitive.isPrimitive())
      {
         return primitive;
      }

      if (int.class.equals(primitive))
      {
         return Integer.class;
      }
      else if (long.class.equals(primitive))
      {
         return Long.class;
      }
      else if (float.class.equals(primitive))
      {
         return Float.class;
      }
      else if (double.class.equals(primitive))
      {
         return Double.class;
      }
      else if (short.class.equals(primitive))
      {
         return Short.class;
      }
      else if (boolean.class.equals(primitive))
      {
         return Boolean.class;
      }
      else if (char.class.equals(primitive))
      {
         return Character.class;
      }
      else if (byte.class.equals(primitive))
      {
         return Byte.class;
      }

      throw new IllegalArgumentException("Unknown primitive type " + primitive);
   }

   /**
    * A helper converting method.
    * 
    * Converts string to a class of given type
    * 
    * @param <T> Type of returned value
    * @param clazz Type of desired value
    * @param value String value to be converted
    * @return Value converted to a appropriate type
    */
   private <T> T convert(Class<T> clazz, String value)
   {
      if (String.class.equals(clazz))
      {
         return clazz.cast(value);
      }
      else if (Integer.class.equals(clazz))
      {
         return clazz.cast(Integer.valueOf(value));
      }
      else if (Double.class.equals(clazz))
      {
         return clazz.cast(Double.valueOf(value));
      }
      else if (Long.class.equals(clazz))
      {
         return clazz.cast(Long.valueOf(value));
      }
      else if (Boolean.class.equals(clazz))
      {
         return clazz.cast(Boolean.valueOf(value));
      }
      else if (URL.class.equals(clazz))
      {
         try
         {
            return clazz.cast(new URI(value).toURL());
         }
         catch (MalformedURLException e)
         {
            throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
         }
         catch (URISyntaxException e)
         {
            throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
         }
      }
      else if (URI.class.equals(clazz))
      {
         try
         {
            return clazz.cast(new URI(value));
         }
         catch (URISyntaxException e)
         {
            throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
         }
      }

      throw new IllegalArgumentException("Unable to convert value " + value + "to a class: " + clazz.getName());
   }
}