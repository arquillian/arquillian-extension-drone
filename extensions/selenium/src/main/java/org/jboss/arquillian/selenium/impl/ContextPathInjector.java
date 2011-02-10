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

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.jboss.arquillian.selenium.annotation.ContextPath;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.Before;

/**
 * A handler which sets a cached instance of Selenium browser for fields
 * annotated with {@link Selenium}. <br/>
 * <b>Imports:</b><br/> {@link Selenium} <br/> {@link SeleniumHolder} <br/>
 * <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see SeleniumHolder
 * @see Selenium
 */
public class ContextPathInjector
{
   @Inject
   private Instance<ProtocolMetaData> protocol;

   public void injectContextPath(@Observes Before event)
   {
      Class<?> clazz = event.getTestClass().getJavaClass();
      Object testInstance = event.getTestInstance();

      // check if any field is ContextPath annotated
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(clazz, ContextPath.class);
      if (fields.isEmpty())
      {
         return;
      }

      if (!fields.isEmpty() && !protocol.get().hasContext(HTTPContext.class))
      {
         throw new IllegalStateException("Unable to retrieve context path for current deployment");
      }

      URI uri = getSingularContextPath(protocol.get().getContext(HTTPContext.class));
      try
      {
         for (Field f : fields)
         {
            f.setAccessible(true);

            // omit setting if already set
            if (f.get(testInstance) != null)
            {
               return;
            }

            if (String.class.isAssignableFrom(f.getType()))
            {
               f.set(testInstance, uri.toString());
            }
            else if (URL.class.isAssignableFrom(f.getType()))
            {
               f.set(testInstance, uri.toURL());
            }
            else if (URI.class.isAssignableFrom(f.getType()))
            {
               f.set(testInstance, uri);
            }
            else
            {
               throw new IllegalStateException("Unable to inject context path to a object of type " + f.getType() + ", it can be injected only to String, URL and URI based fields");
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not inject Selenium members", e);
      }

   }

   private URI getSingularContextPath(HTTPContext context) throws IllegalStateException
   {
      if (context == null || context.getServlets() == null || context.getServlets().size() == 0)
      {
         throw new IllegalStateException("Unable to retrieve context path for current deployment, no context or servlets found");
      }

      List<Servlet> servlets = context.getServlets();
      String candidate = servlets.get(0).getContextRoot();
      for (Servlet servlet : servlets)
      {
         if (!candidate.equals(servlet.getContextRoot()))
         {
            throw new IllegalStateException("Unable to determine context path for current deployment, multiple servlets present in the deployment");
         }
      }

      return servlets.get(0).getBaseURI();
   }

}
