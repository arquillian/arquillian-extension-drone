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

import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;

/**
 * Configuration for Selenium Server. This configuration can be fetched from
 * Arquillian Descriptor and overridden by System properties.
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see ArquillianDescriptor
 * @see ConfigurationMapper
 * 
 */
public class SeleniumServerConfiguration implements DroneConfiguration<SeleniumServerConfiguration>
{
   public static final String CONFIGURATION_NAME = "selenium-server";

   private int port = 14444;

   private String host = "localhost";

   private String output = "target/selenium-server-output.log";

   private boolean enable = false;

   /**
    * Creates default Selenium Server Configuration
    */
   public SeleniumServerConfiguration()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.WebTestConfiguration#configure(org.jboss
    * .arquillian.impl.configuration.api.ArquillianDescriptor, java.lang.Class)
    */
   public SeleniumServerConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier)
   {
      ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
      return ConfigurationMapper.fromSystemConfiguration(this, qualifier);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.WebTestConfiguration#getConfigurationName
    * ()
    */
   public String getConfigurationName()
   {
      return CONFIGURATION_NAME;
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
