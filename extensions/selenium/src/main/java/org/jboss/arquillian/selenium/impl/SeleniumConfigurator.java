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
package org.jboss.arquillian.selenium.impl;

import java.io.IOException;

import org.jboss.arquillian.selenium.SeleniumConfiguration;
import org.jboss.arquillian.selenium.event.SeleniumConfigured;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;

/**
 * A handler which starts Selenium server and binds it the suite scope context. The
 * server instance is stored in {@link SeleniumServerRunner}.
 * 
 * The Selenium server run is <i>disabled</i> by default, it must be allowed
 * either in the Arquillian Selenium Extension configuration or by a system property.
 * 
 * <br/>
 * <b>Imports:</b><br/> {@link Configuration}</br/>
 * <b>Exports:</b><br/> {@link SeleniumServerRunner}<br/>
 * <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see SeleniumServerRunner
 * 
 */
public class SeleniumConfigurator
{
   @Inject
   @SuiteScoped
   private InstanceProducer<SeleniumConfiguration> seleniumConfiguration;

   @Inject
   private Event<SeleniumConfigured> afterConfiguration;

   public void seleniumServerStartUp(@Observes BeforeSuite event) throws IOException
   {
      // TODO replace with SW descriptors
      SeleniumConfiguration selConf = new SeleniumConfiguration();

      seleniumConfiguration.set(selConf);

      afterConfiguration.fire(new SeleniumConfigured());
   }
}
