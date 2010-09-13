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

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.SuiteContextAppender;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;

/**
 * A suite context appender responsible for starting Selenium server. Selenium
 * server is started, if configured to do so, before the testsuite and
 * automatically killed after the suite run is finished.
 * 
 * 
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see AfterSuite
 * @see BeforeSuite
 * 
 */
public class SeleniumServerContextAppender implements SuiteContextAppender
{

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.spi.SuiteContextAppender#append(org.jboss.arquillian
    * .spi.Context)
    */
   public void append(Context context)
   {
      context.register(BeforeSuite.class, new SeleniumServerStartupHandler());
      context.register(AfterSuite.class, new SeleniumServerShutdownHandler());
   }

}
