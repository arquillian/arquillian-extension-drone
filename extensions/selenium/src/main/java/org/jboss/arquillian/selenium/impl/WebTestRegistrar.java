/**
 * 
 */
package org.jboss.arquillian.selenium.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.arquillian.selenium.spi.Configurator;
import org.jboss.arquillian.selenium.spi.Destructor;
import org.jboss.arquillian.selenium.spi.Instantiator;
import org.jboss.arquillian.selenium.spi.Sortable;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.core.annotation.SuiteScoped;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class WebTestRegistrar
{
   @Inject
   @SuiteScoped
   private InstanceProducer<WebTestRegistry> webTestRegistry;

   @Inject
   private Instance<ServiceLoader> serviceLoader;

   public void register(@Observes BeforeSuite event)
   {
      WebTestRegistry registry = new WebTestRegistry();
      webTestRegistry.set(registry);
      registerConfigurators();
      registerInstantiators();
      registerDestructors();
   }

   private void registerConfigurators()
   {
      @SuppressWarnings("rawtypes")
      List<Configurator> list = new ArrayList<Configurator>(serviceLoader.get().all(Configurator.class));
      Collections.sort(list, SORTABLE_COMPARATOR);

      for (Configurator<?> configurator : list)
      {
         Class<?> type = getFirstGenericParameterType(configurator.getClass(), Configurator.class);
         if (type != null)
         {
            webTestRegistry.get().registerConfigurator(type, configurator);
         }
      }
   }

   public void registerInstantiators()
   {
      @SuppressWarnings("rawtypes")
      List<Instantiator> list = new ArrayList<Instantiator>(serviceLoader.get().all(Instantiator.class));
      Collections.sort(list, SORTABLE_COMPARATOR);

      for (Instantiator<?> instantiator : list)
      {
         Class<?> type = getFirstGenericParameterType(instantiator.getClass(), Instantiator.class);
         if (type != null)
         {
            webTestRegistry.get().registerInstantiator(type, instantiator);
         }
      }
   }

   public void registerDestructors()
   {
      @SuppressWarnings("rawtypes")
      List<Destructor> list = new ArrayList<Destructor>(serviceLoader.get().all(Destructor.class));
      Collections.sort(list, SORTABLE_COMPARATOR);

      for (Destructor<?> destructor : list)
      {
         Class<?> type = getFirstGenericParameterType(destructor.getClass(), Destructor.class);
         if (type != null)
         {
            webTestRegistry.get().registerDestructor(type, destructor);
         }
      }
   }

   private static Class<?> getFirstGenericParameterType(Class<?> clazz, Class<?> rawType)
   {
      for (Type interfaceType : clazz.getGenericInterfaces())
      {
         if (interfaceType instanceof ParameterizedType)
         {  
            ParameterizedType ptype = (ParameterizedType) interfaceType;
            if (rawType.isAssignableFrom((Class<?>)ptype.getRawType()))
            {
               return (Class<?>) ptype.getActualTypeArguments()[0];
            }
         }
      }
      return null;
   }

   // comparator
   private static final Comparator<Sortable> SORTABLE_COMPARATOR = new Comparator<Sortable>()
   {
      public int compare(Sortable o1, Sortable o2)
      {
         return new Integer(o1.getPrecedence()).compareTo(new Integer(o2.getPrecedence()));
      }
   };

}
