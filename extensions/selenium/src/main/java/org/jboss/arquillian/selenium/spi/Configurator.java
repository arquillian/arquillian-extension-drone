/**
 * 
 */
package org.jboss.arquillian.selenium.spi;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public interface Configurator<T,C extends WebTestConfiguration<C>> extends Sortable
{  
   C createConfiguration(ArquillianDescriptor descriptor);
}
