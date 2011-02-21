/**
 * 
 */
package org.jboss.arquillian.selenium.spi;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public interface Destructor<T> extends Sortable
{
   /**
    * Destroys an instance of the driver.
    * 
    * After the last method is run, the driver instance is destroyed. This means
    * browser windows, if any, are closed and used resources are freed.
    * 
    * @param instance The instance to be destroyed
    */
   void destroyInstance(T instance);
}
