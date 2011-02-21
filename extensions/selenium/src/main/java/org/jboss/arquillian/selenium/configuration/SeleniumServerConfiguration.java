/**
 * 
 */
package org.jboss.arquillian.selenium.configuration;

import java.util.Collections;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class SeleniumServerConfiguration
{
   public static final String EXTENSION_QUALIFIER = "selenium-server";

   public static final String PROPERTY_PREFIX = "arquillian.selenium.server.";

   private int port = 14444;

   private String host = "localhost";

   private String output = "target/selenium-server-output.log";

   private boolean enable = false;

   /**
    * 
    */
   public SeleniumServerConfiguration()
   {
      new ConfigurationMapper(Collections.<String, String> emptyMap(), PROPERTY_PREFIX).map(this);
   }

   public SeleniumServerConfiguration(ArquillianDescriptor descriptor)
   {
      new ConfigurationMapper(descriptor, EXTENSION_QUALIFIER, PROPERTY_PREFIX).map(this);
   }

   /**
    * @return the port
    */
   public int getPort()
   {
      return port;
   }

   /**
    * @param port the port to set
    */
   public void setPort(int port)
   {
      this.port = port;
   }

   /**
    * @return the host
    */
   public String getHost()
   {
      return host;
   }

   /**
    * @param host the host to set
    */
   public void setHost(String host)
   {
      this.host = host;
   }

   /**
    * @return the output
    */
   public String getOutput()
   {
      return output;
   }

   /**
    * @param output the output to set
    */
   public void setOutput(String output)
   {
      this.output = output;
   }

   /**
    * @return the enable
    */
   public boolean isEnable()
   {
      return enable;
   }

   /**
    * @param enable the enable to set
    */
   public void setEnable(boolean enable)
   {
      this.enable = enable;
   }

}
