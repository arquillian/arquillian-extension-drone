/**
 * 
 */
package org.jboss.arquillian.selenium.factory;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.ajocado.encapsulated.JavaScript;
import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.framework.AjaxSeleniumContext;
import org.jboss.arquillian.ajocado.framework.AjaxSeleniumImpl;
import org.jboss.arquillian.ajocado.framework.AjocadoConfigurationContext;
import org.jboss.arquillian.ajocado.locator.ElementLocationStrategy;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.selenium.configuration.ArquillianAjocadoConfiguration;
import org.jboss.arquillian.selenium.spi.Configurator;
import org.jboss.arquillian.selenium.spi.Destructor;
import org.jboss.arquillian.selenium.spi.Instantiator;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class AjocadoFactory implements Configurator<AjaxSelenium>, Instantiator<AjaxSelenium>, Destructor<AjaxSelenium>
{

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.selenium.spi.Instantiator#getPrecedence()
    */
   public int getPrecedence()
   {
      return 0;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.Destructor#destroyInstance(java.lang
    * .Object)
    */
   public void destroyInstance(AjaxSelenium instance)
   {
      AjaxSeleniumContext.set(null);
      instance.close();
      instance.stop();
      instance = null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.Instantiator#createInstance(java.lang
    * .Object)
    */
   public AjaxSelenium createInstance(Object conf)
   {
      Validate.isInstanceOf(conf, ArquillianAjocadoConfiguration.class, "AjaxSelenium expects ArquillianAjocadoConfiguration class, see: " + ArquillianAjocadoConfiguration.class.getName());
      ArquillianAjocadoConfiguration configuration = (ArquillianAjocadoConfiguration) conf;

      AjaxSelenium selenium = new AjaxSeleniumImpl(configuration.getSeleniumHost(), configuration.getSeleniumPort(), configuration.getBrowser(), configuration.getContextRoot());
      AjaxSeleniumContext.set(selenium);

      selenium.enableNetworkTrafficCapturing(configuration.isSeleniumNetworkTrafficEnabled());
      selenium.start();

      loadCustomLocationStrategies(selenium);
      initializeExtensions(selenium);

      selenium.setSpeed(configuration.getSeleniumSpeed());

      if (configuration.isSeleniumMaximize())
      {
         selenium.windowFocus();
         selenium.windowMaximize();
      }

      return selenium;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.Configurator#createConfiguration(org
    * .jboss.arquillian.impl.configuration.api.ArquillianDescriptor)
    */
   public Object createConfiguration(ArquillianDescriptor descriptor)
   {
      ArquillianAjocadoConfiguration configuration = new ArquillianAjocadoConfiguration(descriptor);
      AjocadoConfigurationContext.set(configuration);
      return configuration;
   }

   private void loadCustomLocationStrategies(AjaxSelenium selenium)
   {
      // jQuery location strategy
      JavaScript strategySource = JavaScript.fromResource("javascript/selenium-location-strategies/jquery-location-strategy.js");
      selenium.addLocationStrategy(ElementLocationStrategy.JQUERY, strategySource);
   }

   private void initializeExtensions(AjaxSelenium selenium)
   {

      List<String> seleniumExtensions = getExtensionsListFromResource("javascript/selenium-extensions-order.txt");
      List<String> pageExtensions = getExtensionsListFromResource("javascript/page-extensions-order.txt");

      // loads the extensions to the selenium
      selenium.getSeleniumExtensions().requireResources(seleniumExtensions);
      // register the handlers for newly loaded extensions
      selenium.getSeleniumExtensions().registerCustomHandlers();
      // prepares the resources to load into page
      selenium.getPageExtensions().loadFromResources(pageExtensions);
   }

   @SuppressWarnings("unchecked")
   private List<String> getExtensionsListFromResource(String resourceName)
   {
      try
      {
         return IOUtils.readLines(ClassLoader.getSystemResourceAsStream(resourceName));
      }
      catch (IOException e)
      {
         throw new IllegalStateException(e);
      }
   }

}
