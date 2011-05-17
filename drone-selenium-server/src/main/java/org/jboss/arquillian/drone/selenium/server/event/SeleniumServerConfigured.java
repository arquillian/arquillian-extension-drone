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
package org.jboss.arquillian.drone.selenium.server.event;

import org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration;

/**
 * An event which is send to inform other components that Selenium Server was configured
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumServerConfigured
{
   private SeleniumServerConfiguration configuration;

   
   /**
    * Creates an event with Selenium Server configuration
    * @param configuration the configuration
    */
   public SeleniumServerConfigured(SeleniumServerConfiguration configuration)
   {
      this.setConfiguration(configuration);
   }

   /**
    * @param configuration the configuration to set
    */
   public void setConfiguration(SeleniumServerConfiguration configuration)
   {
      this.configuration = configuration;
   }

   /**
    * @return the configuration
    */
   public SeleniumServerConfiguration getConfiguration()
   {
      return configuration;
   }

}
