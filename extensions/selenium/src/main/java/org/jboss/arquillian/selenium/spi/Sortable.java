/**
 * 
 */
package org.jboss.arquillian.selenium.spi;


/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public interface Sortable
{
   /**
    * Returns precedence of this implementation
    * 
    * @return the precedence for current instantiator
    */
   int getPrecedence();
}

