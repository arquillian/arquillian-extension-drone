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

import org.jboss.arquillian.selenium.instantiator.SeleniumServerRunner;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;

/**
 * A handler which stops Selenium server. The instance is stored in
 * {@link SeleniumServerRunner} in suite context.
 * 
 * The Selenium server run is <i>disabled</i> by default, it must be allowed
 * either in Arquillian configuration or by a system property.
 * 
 * <br/>
 * <b>Imports:</b><br/> {@link SeleniumServerRunner}</br> <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see SeleniumServerRunner
 * @see SeleniumServerRunner#SERVER_ENABLE_KEY
 * 
 */
public class SeleniumServerShutdownHandler implements EventHandler<SuiteEvent>
{

   /*
    * (non-Javadoc)
    * 
    * @seeorg.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.
    * arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, SuiteEvent event) throws Exception
   {
      SeleniumServerRunner server = context.get(SeleniumServerRunner.class);
      server.stop();
   }

}
