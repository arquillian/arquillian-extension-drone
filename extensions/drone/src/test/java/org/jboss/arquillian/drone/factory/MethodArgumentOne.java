package org.jboss.arquillian.drone.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.arquillian.drone.spi.Qualifier;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Qualifier
public @interface MethodArgumentOne
{

}
