/**
 * 
 */
package org.jboss.arquillian.drone.impl;

import org.jboss.arquillian.drone.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class SystemPropertyPrecedenceTestCase extends AbstractManagerTestBase
{

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.impl.AbstractManagerTestBase#addExtensions(org.jboss.arquillian.impl.core.ManagerBuilder)
    */
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(SeleniumServerConfigurator.class);
   }

   @Test
   public void systemPropertyPrecedence() throws Exception
   {
      System.setProperty("arquillian.selenium.server.port", "54321");
      
      bind(ApplicationScoped.class, ArquillianDescriptor.class, Descriptors.create(ArquillianDescriptor.class)
            .extension("selenium-server")
               .property("port", "12345"));
      fire(new BeforeSuite());

      SeleniumServerConfiguration selConf = getManager().getContext(SuiteContext.class).getObjectStore().get(SeleniumServerConfiguration.class);

      Assert.assertNotNull("Selenium Server configuration was created in context", selConf);
      Assert.assertEquals("Selenium Server configuration is configured with port 54321, from System properties", 54321, selConf.getPort());
      
   }

}
