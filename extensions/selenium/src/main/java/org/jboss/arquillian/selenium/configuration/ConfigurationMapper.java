/**
 * 
 */
package org.jboss.arquillian.selenium.configuration;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.configuration.api.ExtensionDef;

public class ConfigurationMapper
{
   private String systemPropertyPrefix;
   private Map<String, String> nameValuePairs;

   public ConfigurationMapper(ArquillianDescriptor descriptor, String extensionQualifier, String systemPropertyPrefix)
   {
      this.nameValuePairs = getNameValuePairs(descriptor, extensionQualifier);
      this.systemPropertyPrefix = systemPropertyPrefix;
   }

   public ConfigurationMapper(Map<String, String> nameValuePairs, String systemPropertyPrefix)
   {
      this.nameValuePairs = nameValuePairs;
      this.systemPropertyPrefix = systemPropertyPrefix;
   }

   public void map(Object object)
   {
      mapFromArquillianDescriptor(object);
      mapFromSystemProperties(object);
   }

   /**
    * Fills configuration using properties available in ArquillianDescriptor
    * 
    * @param descriptorConfiguration Map of properties
    */
   private void mapFromArquillianDescriptor(Object object)
   {
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(object.getClass());
      for (Field f : fields)
      {
         if (nameValuePairs.containsKey(f.getName()))
         {
            try
            {
               f.set(object, convert(box(f.getType()), nameValuePairs.get(f.getName())));
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not map Arquillian Selenium extension configuration from ArquillianDescriptor: ", e);
            }
         }
      }
   }

   /**
    * Fills configuration using System properties
    */
   private void mapFromSystemProperties(Object object)
   {
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(object.getClass());
      for (Field f : fields)
      {
         String value = SecurityActions.getProperty(keyTransform(f.getName()));
         if (value != null)
         {
            try
            {
               f.set(object, convert(box(f.getType()), value));
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not map Arquillian Selenium extension configuration from System properties", e);
            }
         }
      }
   }

   private Map<String, String> getNameValuePairs(ArquillianDescriptor descriptor, String extensionQualifier)
   {
      Map<String, String> nameValuePairs = Collections.emptyMap();
      for (ExtensionDef extension : descriptor.getExtensions())
      {
         if (extensionQualifier.equals(extension.getExtensionName()))
         {
            nameValuePairs = extension.getExtensionProperties();
            break;
         }
      }
      return nameValuePairs;
   }

   /**
    * Maps a field name to a property.
    * 
    * Replaces camel case with a dot ('.') and lower case character, adds a
    * prefix of "arquillian.selenium" e.g. {@code serverName} is transformed to
    * {@code arquillian.selenium.server.name}
    * 
    * @param propertyName The name of field
    * @return Corresponding property name
    */
   private String keyTransform(String propertyName)
   {
      StringBuilder sb = new StringBuilder(systemPropertyPrefix);

      for (int i = 0; i < propertyName.length(); i++)
      {
         char c = propertyName.charAt(i);
         if (Character.isUpperCase(c))
         {
            sb.append('.').append(Character.toLowerCase(c));
         }
         else
         {
            sb.append(c);
         }
      }

      return sb.toString();
   }

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