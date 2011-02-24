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
package org.jboss.arquillian.selenium.configuration;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.selenium.spi.WebTestConfiguration;

/**
 * Configuration for Selenium WebDriver. This configuration can be fetched from
 * Arquillian Descriptor and overridden by System properties.
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see ArquillianDescriptor
 * @see ConfigurationMapper
 * 
 */
public class WebDriverConfiguration implements WebTestConfiguration<WebDriverConfiguration>
{
   public static final String CONFIGURATION_NAME = "webdriver";

   private String implementationClass = "org.openqa.selenium.htmlunit.HtmlUnitDriver";

   /**
    * Creates default Selenium WebDriver Configuration
    */
   public WebDriverConfiguration()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.WebTestConfiguration#configure(org.jboss
    * .arquillian.impl.configuration.api.ArquillianDescriptor, java.lang.Class)
    */
   public WebDriverConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier)
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
    * @return the implementationClass
    */
   public String getImplementationClass()
   {
      return implementationClass;
   }

   /**
    * @param implementationClass the implementationClass to set
    */
   public void setImplementationClass(String implementationClass)
   {
      this.implementationClass = implementationClass;
   }

}
