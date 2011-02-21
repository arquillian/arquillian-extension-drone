/**
 * 
 */
package org.jboss.arquillian.selenium.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.arquillian.selenium.spi.Qualifier;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Qualifier
public @interface Different
{

}
