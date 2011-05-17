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
package org.jboss.arquillian.drone.selenium.server.impl;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerStopped;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.openqa.selenium.server.SeleniumServer;

/**
 * Destructor of Selenium Server instance
 * <p/>
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration}</li>
 * <li>{@link org.openqa.selenium.server.SeleniumServer}</li>
 * </ol>
 * <p/>
 * <p>
 * Fires:
 * </p>
 * <ol>
 * <li>{@link org.jboss.arquillian.drone.selenium.server.event.SeleniumServerStopped}</li>
 * </ol>
 * <p/>
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link org.jboss.arquillian.spi.event.suite.AfterSuite}</li>
 * </ol>
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 */
public class SeleniumServerDestructor
{
   @Inject
   private Instance<SeleniumServerConfiguration> seleniumServerConfiguration;

   @Inject
   private Instance<SeleniumServer> seleniumServer;

   @Inject
   private Event<SeleniumServerStopped> afterStop;

   public void seleniumServerShutDown(@Observes AfterSuite event)
   {
      if (!seleniumServerConfiguration.get().isEnable())
      {
         return;
      }

      seleniumServer.get().stop();
      afterStop.fire(new SeleniumServerStopped());
   }

}
