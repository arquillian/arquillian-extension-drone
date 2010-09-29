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

import org.jboss.arquillian.spi.ExtensionConfiguration;

/**
 * A {@link ExtensionConfiguration} for Selenium extension.
 * 
 * Allows grabbing configuration from XML configuration file as well as from System properties.
 * 
 * As for System properties, they take precedence before XML configuration and they are mapped in
 * such way, that property name is transformed using following rules:
 * <ol>
 * <li>Prefix {@code arquillian.selenium} is added</li>
 * <li>{@code get} part of name is removed</li>
 * <li>Upper case letters are replaced with dot and their lower case equivalent</li>
 * </ol>
 * 
 * Example: {@code getServerPort} is mapped to {@code arquillian.selenium.server.port} property name.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumExtensionConfiguration implements ExtensionConfiguration
{

   private int serverPort = 14444;

   private String serverHost = "localhost";

   private String serverImplementation = "org.openqa.selenium.server.SeleniumServer";

   private String serverOutput = "target/selenium-server-output.log";

   private String serverCmdline = "";

   private boolean serverEnable = false;

   private String serverToken = "Started org.openqa.jetty.jetty.Server";

   private String url = "http://localhost:8080";

   private int timeout = 60000;

   private int speed = 0;

   private String browser = "*firefoxproxy";

   private String webdriverImplementation = "org.openqa.selenium.htmlunit.HtmlUnitDriver";

   // overlay configuration
   private SystemPropertyConfiguration override = new SystemPropertyConfiguration();

   /**
    * A port where Selenium server is started/running
    * 
    * @return the serverPort
    */
   public int getServerPort()
   {
      Integer value = override.get(Integer.class, "serverPort");
      if (value != null)
      {
         return value.intValue();
      }

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
      String value = override.get(String.class, "serverHost");
      if (value != null)
      {
         return value;
      }

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
    * A class that implements Selenium server
    * 
    * @return the serverImplementation
    */
   public String getServerImplementation()
   {
      String value = override.get(String.class, "serverImplementation");
      if (value != null)
      {
         return value;
      }

      return serverImplementation;
   }

   /**
    * @param serverImplementation the serverImplementation to set
    */
   public void setServerImplementation(String serverImplementation)
   {
      this.serverImplementation = serverImplementation;
   }

   /**
    * A name of file where the output of Selenium server is written to
    * 
    * @return the serverOutput
    */
   public String getServerOutput()
   {
      String value = override.get(String.class, "serverOutput");
      if (value != null)
      {
         return value;
      }

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
    * Additional arguments which can be passed to Selenium server while it's starting
    * 
    * @return the serverCmdline
    */
   public String getServerCmdline()
   {
      String value = override.get(String.class, "serverCmdline");
      if (value != null)
      {
         return value;
      }

      return serverCmdline;
   }

   /**
    * @param serverCmdline the serverCmdline to set
    */
   public void setServerCmdline(String serverCmdline)
   {
      this.serverCmdline = serverCmdline;
   }

   /**
    * Determines if Selenium server is started and closed before test suite
    * 
    * @return the serverEnable
    */
   public boolean isServerEnable()
   {
      Boolean value = override.get(Boolean.class, "serverEnable");
      if (value != null)
      {
         return value.booleanValue();
      }

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
    * A string which, if found in server's output, determines that server was
    * properly started
    * 
    * @return the serverToken
    */
   public String getServerToken()
   {
      String value = override.get(String.class, "serverToken");
      if (value != null)
      {
         return value;
      }

      return serverToken;
   }

   /**
    * @param serverToken the serverToken to set
    */
   public void setServerToken(String serverToken)
   {
      this.serverToken = serverToken;
   }

   /**
    * The URL opened in the browser, which encapsulates the session
    * 
    * @return the url
    */
   public String getUrl()
   {
      String value = override.get(String.class, "url");
      if (value != null)
      {
         return value;
      }

      return url;
   }

   /**
    * @param url the url to set
    */
   public void setUrl(String url)
   {
      this.url = url;
   }

   /**
    * Time limit in milliseconds which determines operation failed, either for
    * executing Selenium command or starting Selenium server
    * 
    * @return the timeout
    */
   public int getTimeout()
   {
      Integer value = override.get(Integer.class, "timeout");
      if (value != null)
      {
         return value.intValue();
      }

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
      Integer value = override.get(Integer.class, "speed");
      if (value != null)
      {
         return value.intValue();
      }

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
      String value = override.get(String.class, "browser");
      if (value != null)
      {
         return value;
      }

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
    * Browser implementation for WebDriver browser, that is the name
    * of class to be instantiated
    * 
    * @return the webdriverImplementation
    */
   public String getWebdriverImplementation()
   {
      String value = override.get(String.class, "webdriverImplementation");
      if (value != null)
      {
         return value;
      }

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
    * Retrieves a configuration from System properties.
    * 
    * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
    * 
    */
   private static class SystemPropertyConfiguration
   {
      private static final String PROPERTY_PREFIX = "arquillian.selenium.";

      /**
       * Gets a property of given type from System properties
       * @param <T>
       * @param clazz The resulting class
       * @param key The name of property as named in extension configuration
       * @return Either property value converted to a given class or {@code null}
       */
      public <T> T get(Class<T> clazz, String key)
      {

         String value = SecurityActions.getProperty(keyTransform(key));
         if (value == null)
            return null;

         return convert(clazz, value);
      }

      /**
       * Maps a field name to a property.
       * 
       * Replaces camel case with a dot ('.') and lower case character,
       * adds a prefix of "arquillian.selenium"
       * e.g. {@code serverName} is transformed to {@code arquillian.selenium.server.name}
       * 
       * @param propertyName The name of field
       * @return Corresponding property name
       */
      private String keyTransform(String propertyName)
      {
         StringBuilder sb = new StringBuilder(PROPERTY_PREFIX);

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

      private <T> T convert(Class<T> clazz, String value)
      {
         if (String.class.equals(clazz))
         {
            return clazz.cast(value);
         }
         else if (Integer.class.equals(clazz) || int.class.equals(clazz))
         {
            return clazz.cast(Integer.valueOf(value));
         }
         else if (Double.class.equals(clazz) || double.class.equals(clazz))
         {
            return clazz.cast(Double.valueOf(value));
         }
         else if (Long.class.equals(clazz) || long.class.equals(clazz))
         {
            return clazz.cast(Long.valueOf(value));
         }
         else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz))
         {
            return clazz.cast(Boolean.valueOf(value));
         }

         throw new IllegalArgumentException("Unable to convert value " + value + "to a class: " + clazz.getName());
      }
   }

}
