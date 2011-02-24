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
package org.jboss.arquillian.selenium.factory;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.selenium.configuration.WebDriverConfiguration;
import org.jboss.arquillian.selenium.spi.Configurator;
import org.jboss.arquillian.selenium.spi.Destructor;
import org.jboss.arquillian.selenium.spi.Instantiator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Instantiates Selenium WebDriver implementation. Allows user to specify driver
 * class which be used during testing either by Arquillian configuration or a
 * system property.
 * 
 * Default implementation is HtmlUnitDriver.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see HtmlUnitDriver
 * @see WebDriver
 */
public class WebDriverFactory implements Configurator<WebDriver, WebDriverConfiguration>, Instantiator<WebDriver, WebDriverConfiguration>, Destructor<WebDriver>
{
   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.instantiator.Instantiator#getPrecedence()
    */
   public int getPrecedence()
   {
      return 0;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.Destructor#destroyInstance(java.lang
    * .Object)
    */
   public void destroyInstance(WebDriver instance)
   {
      instance.quit();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.Instantiator#createInstance(java.lang
    * .Object)
    */
   public WebDriver createInstance(WebDriverConfiguration configuration)
   {
      WebDriver driver = SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[0], new Object[0], WebDriver.class);
      return driver;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.Configurator#createConfiguration(org
    * .jboss.arquillian.impl.configuration.api.ArquillianDescriptor,
    * java.lang.Class)
    */
   public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier)
   {
      return new WebDriverConfiguration().configure(descriptor, qualifier);
   }

}
