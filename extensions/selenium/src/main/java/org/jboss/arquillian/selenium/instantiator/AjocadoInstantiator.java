/**
 * 
 */
package org.jboss.arquillian.selenium.instantiator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.ajocado.browser.Browser;
import org.jboss.arquillian.ajocado.encapsulated.JavaScript;
import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.framework.AjaxSeleniumImpl;
import org.jboss.arquillian.ajocado.framework.AjaxSeleniumProxy;
import org.jboss.arquillian.ajocado.locator.ElementLocationStrategy;
import org.jboss.arquillian.selenium.SeleniumConfiguration;
import org.jboss.arquillian.selenium.spi.Instantiator;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class AjocadoInstantiator implements Instantiator<AjaxSelenium>
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
    * org.jboss.arquillian.selenium.spi.Instantiator#create(org.jboss.arquillian
    * .selenium.SeleniumConfiguration)
    */
   public AjaxSelenium create(SeleniumConfiguration configuration)
   {
      URL url = convertStringToURL(configuration.getUrl());

      AjaxSelenium selenium = new AjaxSeleniumImpl(configuration.getServerHost(), configuration.getServerPort(), new Browser(configuration.getBrowser()), url);
      AjaxSeleniumProxy.setCurrentContext(selenium);

      selenium.enableNetworkTrafficCapturing(configuration.isCaptureNetworkTraffic());
      selenium.start();

      loadCustomLocationStrategies(selenium);
      initializeExtensions(selenium);

      selenium.setSpeed(configuration.getSpeed());
      selenium.setTimeout(configuration.getTimeout());

      if (configuration.isMaximize())
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
    * org.jboss.arquillian.selenium.spi.Instantiator#destroy(java.lang.Object)
    */
   public void destroy(AjaxSelenium instance)
   {
      AjaxSeleniumProxy.setCurrentContext(null);
      instance.close();
      instance.stop();
      instance = null;
   }

   private URL convertStringToURL(String url)
   {
      try
      {
         return new URI(url).toURL();
      }
      catch (MalformedURLException e)
      {
         throw new IllegalArgumentException("Unable to spawn Ajocado's AjaxSelenium on a malformed URL", e);
      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException("Unable to spawn Ajocado's AjaxSelenium on malformed URL", e);
      }
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
