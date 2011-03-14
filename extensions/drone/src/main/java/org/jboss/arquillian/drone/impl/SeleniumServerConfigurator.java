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
package org.jboss.arquillian.drone.impl;

import java.io.IOException;

import org.jboss.arquillian.drone.annotation.Default;
import org.jboss.arquillian.drone.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.event.SeleniumServerConfigured;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;

/**
 * Configurator of Selenium Server Configuration
 * 
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link ArquillianDescriptor}</li>
 * </ol>
 * 
 * <p>
 * Produces:
 * </p>
 * <ol>
 * <li>{@link SeleniumServerConfiguration}</li>
 * </ol>
 * 
 * <p>
 * Fires:
 * </p>
 * <ol>
 * <li>{@link SeleniumServerConfigured}</li>
 * </ol>
 * 
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link BeforeSuite}</li>
 * </ol>
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class SeleniumServerConfigurator
{
   @Inject
   @SuiteScoped
   private InstanceProducer<SeleniumServerConfiguration> seleniumServerConfiguration;

   @Inject
   private Instance<ArquillianDescriptor> arquillianDesc;

   @Inject
   private Event<SeleniumServerConfigured> afterConfiguration;

   public void seleniumServerStartUp(@Observes BeforeSuite event) throws IOException
   {
      SeleniumServerConfiguration configuration = new SeleniumServerConfiguration();
      configuration.configure(arquillianDesc.get(), Default.class);

      seleniumServerConfiguration.set(configuration);

      afterConfiguration.fire(new SeleniumServerConfigured(configuration));
   }
}
