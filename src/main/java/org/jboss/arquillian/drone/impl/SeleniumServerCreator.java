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

import org.jboss.arquillian.drone.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.event.SeleniumServerConfigured;
import org.jboss.arquillian.drone.event.SeleniumServerStarted;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;

/**
 * Creator of Selenium Server instance
 * 
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link SeleniumServerConfiguration}</li>
 * </ol>
 * 
 * <p>
 * Produces:
 * </p>
 * <ol>
 * <li>{@link SeleniumServer}</li>
 * </ol>
 * 
 * <p>
 * Fires:
 * </p>
 * <ol>
 * <li>{@link SeleniumServerStarted}</li>
 * </ol>
 * 
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link SeleniumServerConfigured}</li>
 * </ol>
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class SeleniumServerCreator
{
   @Inject
   private Instance<SeleniumServerConfiguration> seleniumServerConfiguration;

   @Inject
   private Event<SeleniumServerStarted> afterStart;

   @Inject
   @SuiteScoped
   private InstanceProducer<SeleniumServer> seleniumServer;

   public void seleniumServerStartUp(@Observes SeleniumServerConfigured event) throws IOException
   {

      if (!seleniumServerConfiguration.get().isEnable())
      {
         return;
      }

      SeleniumServerConfiguration configuration = seleniumServerConfiguration.get();

      RemoteControlConfiguration rcc = new RemoteControlConfiguration();
      rcc.setPort(configuration.getPort());
      rcc.setLogOutFileName(configuration.getOutput());

      try
      {
         SeleniumServer server = new SeleniumServer(rcc);
         server.start();
         seleniumServer.set(server);
         afterStart.fire(new SeleniumServerStarted());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to start Selenium Server", e);
      }
   }
}
