/**
 * 
 */
package org.jboss.arquillian.selenium.configuration;

import java.util.Collections;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class WebDriverConfiguration
{
   private static final String EXTENSION_QUALIFIER = "webdriver";

   private static final String PROPERTY_PREFIX = "arquillian.selenium.webdriver.";

   private String implementationClass = "org.openqa.selenium.htmlunit.HtmlUnitDriver";

   /**
    * 
    */
   public WebDriverConfiguration()
   {
      new ConfigurationMapper(Collections.<String, String> emptyMap(), PROPERTY_PREFIX).map(this);
   }

   public WebDriverConfiguration(ArquillianDescriptor descriptor)
   {
      new ConfigurationMapper(descriptor, EXTENSION_QUALIFIER, PROPERTY_PREFIX).map(this);
   }

   /**
    * @return the implementationClass
    */
   public String getImplementationClass()
   {
      return implementationClass;
   }

   /**
    * @param implementationClass the implementationClass to set
    */
   public void setImplementationClass(String implementationClass)
   {
      this.implementationClass = implementationClass;
   }

}
