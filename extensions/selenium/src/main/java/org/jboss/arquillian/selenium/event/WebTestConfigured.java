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
package org.jboss.arquillian.selenium.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.jboss.arquillian.selenium.spi.WebTestConfiguration;

/**
 * An event to inform other components that a Web Test instance was configured
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class WebTestConfigured
{
   private Class<? extends Annotation> qualifier;
   private Field injected;
   private WebTestConfiguration<?> configuration;

   /**
    * Creates the event
    * @param injected A field of test which caused creation of the configuration
    * @param qualifier Qualifier for current Web Test instance
    * @param configuration Configuration for Web Test instance
    */
   public WebTestConfigured(Field injected, Class<? extends Annotation> qualifier, WebTestConfiguration<?> configuration)
   {
      this.injected = injected;
      this.qualifier = qualifier;
      this.configuration = configuration;
   }

   /**
    * @return the qualifier
    */
   public Class<? extends Annotation> getQualifier()
   {
      return qualifier;
   }

   /**
    * @param qualifier the qualifier to set
    */
   public void setQualifier(Class<? extends Annotation> qualifier)
   {
      this.qualifier = qualifier;
   }

   /**
    * @return the injected
    */
   public Field getInjected()
   {
      return injected;
   }

   /**
    * @param injected the injected to set
    */
   public void setInjected(Field injected)
   {
      this.injected = injected;
   }

   /**
    * @param configuration the configuration to set
    */
   public void setConfiguration(WebTestConfiguration<?> configuration)
   {
      this.configuration = configuration;
   }

   /**
    * @return the configuration
    */
   public WebTestConfiguration<?> getConfiguration()
   {
      return configuration;
   }

}
