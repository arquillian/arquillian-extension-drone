/**
 * 
 */
package org.jboss.arquillian.selenium.factory;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.selenium.configuration.SeleniumConfiguration;
import org.jboss.arquillian.selenium.spi.Configurator;
import org.jboss.arquillian.selenium.spi.Destructor;
import org.jboss.arquillian.selenium.spi.Instantiator;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class DefaultSeleniumFactory implements Configurator<DefaultSelenium>, Instantiator<DefaultSelenium>, Destructor<DefaultSelenium>
{

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.selenium.spi.Sortable#getPrecedence()
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
   public void destroyInstance(DefaultSelenium instance)
   {
      instance.close();
      instance.stop();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.Instantiator#createInstance(java.lang
    * .Object)
    */
   public DefaultSelenium createInstance(Object conf)
   {
      Validate.isInstanceOf(conf, SeleniumConfiguration.class, "DefaultSelenium expects SeleniumConfiguration class, see: " + SeleniumConfiguration.class.getName());
      SeleniumConfiguration configuration = (SeleniumConfiguration) conf;

      DefaultSelenium selenium = new DefaultSelenium(configuration.getServerHost(), configuration.getServerPort(), configuration.getBrowser(), configuration.getUrl());
      selenium.start();
      selenium.setSpeed(String.valueOf(configuration.getSpeed()));
      selenium.setTimeout(String.valueOf(configuration.getTimeout()));

      return selenium;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.spi.Configurator#createConfiguration(org
    * .jboss.arquillian.impl.configuration.api.ArquillianDescriptor)
    */
   public SeleniumConfiguration createConfiguration(ArquillianDescriptor descriptor)
   {
      return new SeleniumConfiguration(descriptor);
   }
}
