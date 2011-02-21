/**
 * 
 */
package org.jboss.arquillian.selenium.factory;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class Validate
{
   public static final void isInstanceOf(Object object, Class<?> clazz, String message)
   {
      if (!clazz.isAssignableFrom(object.getClass()))
      {
         throw new IllegalArgumentException(message);
      }
   }

}
