/**
 * 
 */
package org.jboss.arquillian.selenium.spi;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public interface Configurator<T> extends Sortable
{  
   Object createConfiguration(ArquillianDescriptor descriptor);
}
