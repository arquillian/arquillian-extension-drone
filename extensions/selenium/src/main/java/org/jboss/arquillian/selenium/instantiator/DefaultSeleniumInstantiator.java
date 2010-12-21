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

import org.jboss.arquillian.selenium.SeleniumConfiguration;
import org.jboss.arquillian.selenium.spi.Instantiator;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Instantiator of the legacy Selenium driver. This driver requires Selenium
 * server to be running to connect to the browser.
 * 
 * By default, it launches Firefox driver and opens {@code
 * http://localhost:8080}. It expects Selenium server running on port {@code
 * 14444} on current machine.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class DefaultSeleniumInstantiator implements Instantiator<DefaultSelenium>
{

   public DefaultSeleniumInstantiator()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.selenium.spi.Instantiator#getPrecedence()
    */
   public int getPrecedence()
   {
      return 0;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.selenium.instantiator.Instantiator#create()
    */
   public DefaultSelenium create(SeleniumConfiguration configuration)
   {

      DefaultSelenium selenium = new DefaultSelenium(configuration.getServerHost(), configuration.getServerPort(), configuration.getBrowser(), configuration.getUrl());
      selenium.start();
      selenium.setSpeed(String.valueOf(configuration.getSpeed()));
      selenium.setTimeout(String.valueOf(configuration.getTimeout()));

      return selenium;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.instantiator.Instantiator#destroy(java.lang
    * .Object)
    */
   public void destroy(DefaultSelenium instance)
   {
      instance.close();
      instance.stop();
   }

}
