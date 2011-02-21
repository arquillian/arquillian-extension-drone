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

import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.selenium.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.selenium.event.SeleniumServerConfigured;
import org.jboss.arquillian.selenium.event.SeleniumServerStarted;
import org.jboss.arquillian.selenium.event.SeleniumServerStopped;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.server.SeleniumServer;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumServerTestCase extends AbstractManagerTestBase
{
   @Spy
   SeleniumServerConfiguration configuration = new SeleniumServerConfiguration();
   
   @Test
   public void serverCreatedAndDestroyed() throws Exception
   {
      bind(SuiteScoped.class, SeleniumServerConfiguration.class, configuration);
      Mockito.when(configuration.isEnable()).thenReturn(true);

      fire(new SeleniumServerConfigured(configuration));

      SeleniumServer server = getManager().getContext(SuiteContext.class).getObjectStore().get(SeleniumServer.class);

      Assert.assertNotNull("Selenium server object is present in context", server);
      assertEventFired(SeleniumServerStarted.class, 1);

      fire(new AfterSuite());

      assertEventFired(SeleniumServerStopped.class, 1);
   }

   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extensions(SeleniumServerCreator.class, SeleniumServerDestructor.class);
   }
}
