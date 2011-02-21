/**
 * 
 */
package org.jboss.arquillian.selenium.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.event.WebTestConfigured;
import org.jboss.arquillian.selenium.spi.Configurator;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class WebTestConfigurator
{
   @Inject
   @ClassScoped
   private InstanceProducer<WebTestContext> webTestContext;

   @Inject
   private Instance<ArquillianDescriptor> arquillianDescriptor;

   @Inject
   private Instance<WebTestRegistry> registry;

   @Inject
   private Event<WebTestConfigured> afterConfiguration;

   public void configureWebTest(@Observes BeforeClass event)
   {

      // check if any field is @Selenium annotated
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(event.getTestClass().getJavaClass(), Selenium.class);
      if (fields.isEmpty())
      {
         return;
      }

      WebTestContext context = new WebTestContext();
      webTestContext.set(context);
      for (Field f : fields)
      {
         Class<?> typeClass = f.getType();
         Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);
         
         Configurator<?> configurator = registry.get().getConfigurator(typeClass);
         if (configurator == null)
         {
            throw new IllegalArgumentException("No configurator was found for object of type " + typeClass.getName());
         }

         Object configuration = configurator.createConfiguration(arquillianDescriptor.get());
         webTestContext.get().add(configuration.getClass(), qualifier, configuration);
         afterConfiguration.fire(new WebTestConfigured(f, qualifier, configuration));
      }

   }

}
