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

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.selenium.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumServerConfiguratonCreatorTestCase extends AbstractManagerTestBase
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.selenium.impl.AbstractManagerTestBase#addExtensions(org.jboss.arquillian.impl.core.ManagerBuilder)
    */
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(SeleniumServerConfigurator.class);
   }

   @Test
   public void configurationWasCreated() throws Exception
   {
      bind(ApplicationScoped.class, ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class));
      fire(new BeforeSuite());

      SeleniumServerConfiguration selConf = getManager().getContext(SuiteContext.class).getObjectStore().get(SeleniumServerConfiguration.class);

      Assert.assertNotNull("Selenium configuration was created in context", selConf);    
   }


}
