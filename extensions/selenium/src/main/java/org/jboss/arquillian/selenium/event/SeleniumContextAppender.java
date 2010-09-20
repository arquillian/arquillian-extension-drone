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
package org.jboss.arquillian.selenium.event;

import org.jboss.arquillian.spi.ClassContextAppender;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * A class context appender responsible for fetching Selenium browser
 * configuration from Arquillian configuration, creating its instance and
 * injecting it before each method of the test is run. After all methods in 
 * test class are run, it safely destroys Selenium browser.
 * 
 * 
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see AfterClass
 * @see BeforeClass
 * @see Before
 * 
 */
public class SeleniumContextAppender implements ClassContextAppender
{
   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.spi.ClassContextAppender#append(org.jboss.arquillian
    * .spi.Context)
    */
   public void append(Context context)
   {
      context.register(AfterClass.class, new SeleniumShutdownHandler());
      context.register(BeforeClass.class, new SeleniumStartupHandler());
      context.register(Before.class, new SeleniumRetrievalHandler());
   }
}
