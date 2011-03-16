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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.drone.annotation.ContextPath;
import org.jboss.arquillian.drone.annotation.Drone;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.util.Validate;

/**
 * Enriches test with drone instance and context path. Injects existing instance
 * into every field annotated with {@link Drone}. Handles enrichements for method
 * arguments as well.
 * 
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link DroneContext}</li>
 * <li>{@link MethodContext}</li>
 * <li>{@link ProtocolMetaData}</li>
 * <li>{@link ArquillianDescriptor}</li>
 * <li>{@link DroneRegistry}</li>
 * </ol>
 * 
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class DroneTestEnricher implements TestEnricher
{
   private static final Logger log = Logger.getLogger(DroneTestEnricher.class.getName());

   @Inject
   private Instance<DroneContext> droneContext;

   @Inject
   private Instance<MethodContext> methodContext;

   @Inject
   private Instance<ProtocolMetaData> protocol;

   @Inject
   private Instance<ArquillianDescriptor> arquillianDescriptor;

   @Inject
   private Instance<DroneRegistry> registry;

   public void enrich(Object testCase)
   {
      List<Field> droneEnrichements = SecurityActions.getFieldsWithAnnotation(testCase.getClass(), Drone.class);
      if (!droneEnrichements.isEmpty())
      {
         Validate.notNull(droneContext.get(), "Drone Test context should not be null");
         droneEnrichement(testCase, droneEnrichements);
      }

      List<Field> contextPathEnrichements = SecurityActions.getFieldsWithAnnotation(testCase.getClass(), ContextPath.class);
      if (!contextPathEnrichements.isEmpty())
      {
         Validate.notNull(protocol.get(), "Protocol Meta Data should not be null");
         contextPathEnrichement(testCase, droneEnrichements);
      }

   }

   public Object[] resolve(Method method)
   {
      Class<?>[] parameterTypes = method.getParameterTypes();
      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      Object[] resolution = new Object[parameterTypes.length];

      for (int i = 0; i < parameterTypes.length; i++)
      {
         if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], Drone.class))
         {
            if (log.isLoggable(Level.FINE))
            {
               log.fine("Resolving method " + method.getName() + " argument at position " + i);
            }

            Validate.notNull(registry.get(), "Drone registry should not be null");
            Validate.notNull(arquillianDescriptor.get(), "ArquillianDescriptor should not be null");
            Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations[i]);
            resolution[i] = constructDrone(method, parameterTypes[i], qualifier);
         }
         else if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], ContextPath.class))
         {
            Validate.notNull(protocol.get(), "Protocol Meta data should not be null");
            try
            {
               resolution[i] = getSingularContextPath(protocol.get().getContext(HTTPContext.class), parameterTypes[i]);
            }
            catch (MalformedURLException e)
            {
               throw new IllegalStateException("Could not enrich method " + method.getName() + " with ContextPath", e);
            }
         }
      }

      return resolution;
   }

   private void contextPathEnrichement(Object testCase, List<Field> fields)
   {
      try
      {
         for (Field f : fields)
         {
            // omit setting if already set
            if (f.get(testCase) != null)
            {
               return;
            }

            f.set(testCase, getSingularContextPath(protocol.get().getContext(HTTPContext.class), f.getType()));
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not inject Drone ContextPath members", e);
      }
   }

   private void droneEnrichement(Object testCase, List<Field> fields)
   {
      try
      {
         for (Field f : fields)
         {
            // omit setting if already set
            if (f.get(testCase) != null)
            {
               return;
            }

            Class<?> typeClass = f.getType();
            Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

            Object value = droneContext.get().get(typeClass, qualifier);
            if (value == null)
            {
               throw new IllegalArgumentException("Retrieved a null from context, which is not a valid Drone browser object");
            }

            f.set(testCase, value);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not enrich test with Drone members", e);
      }
   }

   private <T> T getSingularContextPath(HTTPContext context, Class<T> returnType) throws IllegalStateException, MalformedURLException
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

      // convert URI
      URI uri = servlets.get(0).getBaseURI();
      if (String.class.isAssignableFrom(returnType))
      {
         return returnType.cast(uri.toString());
      }
      else if (URL.class.isAssignableFrom(returnType))
      {
         return returnType.cast(uri.toURL());
      }
      else if (URI.class.isAssignableFrom(returnType))
      {
         return returnType.cast(uri);
      }
      else
      {
         throw new IllegalStateException("Unable to convert URI to " + returnType.getName() + ", it can be injected only to String, URL and URI based fields");
      }

   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   private Object constructDrone(Method method, Class<?> type, Class<? extends Annotation> qualifier)
   {
      DroneRegistry regs = registry.get();
      ArquillianDescriptor desc = arquillianDescriptor.get();

      Configurator configurator = regs.getConfiguratorFor(type);
      Instantiator instantiator = regs.getInstantiatorFor(type);

      // store in map if not stored already
      DroneContext dc = methodContext.get().getOrCreate(method);

      DroneConfiguration configuration = configurator.createConfiguration(desc, qualifier);
      dc.add(configuration.getClass(), qualifier, configuration);

      Object instance = instantiator.createInstance(configuration);
      dc.add(type, qualifier, instance);

      if (log.isLoggable(Level.FINE))
      {
         log.fine("Stored method lifecycle based Drone instance, type: " + type.getName() + ", qualifier: " + qualifier.getName() + ", instanceClass: " + instance.getClass());
      }

      return instance;
   }
}
