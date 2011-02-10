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
package org.jboss.arquillian.selenium;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.configuration.api.ExtensionDef;
import org.jboss.arquillian.spi.ExtensionConfiguration;

/**
 * A {@link ExtensionConfiguration} for Selenium extension.
 * 
 * Allows grabbing configuration from XML configuration file as well as from
 * System properties.
 * 
 * As for System properties, they take precedence before XML configuration and
 * they are mapped in such way, that property name is transformed using
 * following rules:
 * <ol>
 * <li>Prefix {@code arquillian.selenium} is added</li>
 * <li>the {@code get} of method name is removed</li>
 * <li>Upper case letters are replaced with dot and their lower case equivalent</li>
 * </ol>
 * 
 * Example: {@code getServerPort} is mapped to
 * {@code arquillian.selenium.server.port} property name.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumConfiguration
{
   private static final String EXTENSION_QUALIFIER = "selenium";

   private static final String PROPERTY_PREFIX = "arquillian.selenium.";

   private int serverPort = 14444;

   private String serverHost = "localhost";

   private String serverOutput = "target/selenium-server-output.log";

   private boolean serverEnable = false;

   private String url = "http://localhost:8080";

   private int timeout = 60000;

   private int speed = 0;
   
   private boolean maximize = false;
   
   private boolean captureNetworkTraffic = false;

   private String browser = "*firefox";

   private String webdriverImplementation = "org.openqa.selenium.htmlunit.HtmlUnitDriver";

   /**
    * Creates a default Selenium configuration
    */
   public SeleniumConfiguration()
   {
      new ConfigurationMapper(PROPERTY_PREFIX, Collections.<String, String> emptyMap()).map(this);
   }

   /**
    * Creates a Selenium configuration using properties available in
    * {@link ArquillianDescriptor}. Still, system based properties have priority
    * over these.
    * 
    * @param descriptorConfiguration
    * @see ArquillianDescriptor
    */
   public SeleniumConfiguration(ArquillianDescriptor arquillianDescriptor)
   {
      Map<String, String> nameValuePairs = Collections.emptyMap();
      for (ExtensionDef extension : arquillianDescriptor.getExtensions())
      {
         if (EXTENSION_QUALIFIER.equals(extension.getExtensionName()))
         {
            nameValuePairs = extension.getExtensionProperties();
            break;
         }
      }

      new ConfigurationMapper(PROPERTY_PREFIX, nameValuePairs).map(this);
   }

   /**
    * A port where Selenium server is started/running
    * 
    * @return the serverPort
    */
   public int getServerPort()
   {
      return serverPort;
   }

   /**
    * @param serverPort the serverPort to set
    */
   public void setServerPort(int serverPort)
   {
      this.serverPort = serverPort;
   }

   /**
    * The name of machine where Selenium server is started/running
    * 
    * @return the serverHost
    */
   public String getServerHost()
   {
      return serverHost;
   }

   /**
    * @param serverHost the serverHost to set
    */
   public void setServerHost(String serverHost)
   {
      this.serverHost = serverHost;
   }

   /**
    * A name of file where the output of Selenium server is written to
    * 
    * @return the serverOutput
    */
   public String getServerOutput()
   {
      return serverOutput;
   }

   /**
    * @param serverOutput the serverOutput to set
    */
   public void setServerOutput(String serverOutput)
   {
      this.serverOutput = serverOutput;
   }

   /**
    * Determines if Selenium server is started and closed before test suite
    * 
    * @return the serverEnable
    */
   public boolean isServerEnable()
   {
      return serverEnable;
   }

   /**
    * @param serverEnable the serverEnable to set
    */
   public void setServerEnable(boolean serverEnable)
   {
      this.serverEnable = serverEnable;
   }

   /**
    * Time limit in milliseconds which determines operation failed, either for
    * executing Selenium command or starting Selenium server
    * 
    * @return the timeout
    */
   public int getTimeout()
   {
      return timeout;
   }

   /**
    * @param timeout the timeout to set
    */
   public void setTimeout(int timeout)
   {
      this.timeout = timeout;
   }

   /**
    * Time delay in milliseconds before each Selenium command is sent
    * 
    * @return the speed
    */
   public int getSpeed()
   {
      return speed;
   }

   /**
    * @param speed the speed to set
    */
   public void setSpeed(int speed)
   {
      this.speed = speed;
   }

   /**
    * Identification of the browser for needs of Selenium.
    * 
    * Use can use variants including path to binary, such as: <i>*firefoxproxy
    * /opt/firefox-3.0/firefox</i>
    * 
    * @return the browser
    */
   public String getBrowser()
   {
      return browser;
   }

   /**
    * @param browser the browser to set
    */
   public void setBrowser(String browser)
   {
      this.browser = browser;
   }

   /**
    * Browser implementation for WebDriver browser, that is the name of class to
    * be instantiated
    * 
    * @return the webdriverImplementation
    */
   public String getWebdriverImplementation()
   {
      return webdriverImplementation;
   }

   /**
    * @param webdriverImplementation the webdriverImplementation to set
    */
   public void setWebdriverImplementation(String webdriverImplementation)
   {
      this.webdriverImplementation = webdriverImplementation;
   }

   /**
    * @param url the url to set
    */
   public void setUrl(String url)
   {
      this.url = url;
   }

   /**
    * @return the url
    */
   public String getUrl()
   {
      return url;
   }

   /**
    * @return the maximize
    */
   public boolean isMaximize()
   {
      return maximize;
   }

   /**
    * @param maximize the maximize to set
    */
   public void setMaximize(boolean maximize)
   {
      this.maximize = maximize;
   }

   /**
    * @return the captureNetworkTraffic
    */
   public boolean isCaptureNetworkTraffic()
   {
      return captureNetworkTraffic;
   }

   /**
    * @param captureNetworkTraffic the captureNetworkTraffic to set
    */
   public void setCaptureNetworkTraffic(boolean captureNetworkTraffic)
   {
      this.captureNetworkTraffic = captureNetworkTraffic;
   }


   
   // UTILITY HELPER

   private static final class ConfigurationMapper
   {
      private final String systemPropertyPrefix;
      private final Map<String, String> nameValuePairs;

      public ConfigurationMapper(String systemPropertyPrefix, Map<String, String> nameValuePairs)
      {
         this.systemPropertyPrefix = systemPropertyPrefix;
         this.nameValuePairs = nameValuePairs;
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

      /**
       * Maps a field name to a property.
       * 
       * Replaces camel case with a dot ('.') and lower case character, adds a
       * prefix of "arquillian.selenium" e.g. {@code serverName} is transformed
       * to {@code arquillian.selenium.server.name}
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

         throw new IllegalArgumentException("Unable to convert value " + value + "to a class: " + clazz.getName());
      }
   }
}
