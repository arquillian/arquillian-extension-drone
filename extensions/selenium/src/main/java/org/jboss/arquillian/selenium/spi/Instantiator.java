/**
 * 
 */
package org.jboss.arquillian.selenium.spi;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public interface Instantiator<T> extends Sortable
{
   /**
    * Creates an instance of the driver.
    * 
    * The instance is created before execution of the first method of the test
    * class automatically by calling this method. The object is then bound to
    * the Arquillian context, where it stays until the execution of the last
    * test method is finished.
    * 
    * @param configuration the configuration object for the extension
    * @return Newly created instance of the driver
    */
   T createInstance(Object configuration);
}
