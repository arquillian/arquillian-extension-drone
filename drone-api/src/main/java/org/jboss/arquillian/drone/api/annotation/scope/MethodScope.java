package org.jboss.arquillian.drone.api.annotation.scope;

import org.jboss.arquillian.drone.api.annotation.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tkriz on 10/02/14.
 */
@Scope
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodScope {
}
