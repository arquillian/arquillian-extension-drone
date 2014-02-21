package org.jboss.arquillian.drone.spi;

/**
 * Created by tkriz on 08/02/14.
 */
public interface Filter {

    boolean accept(InjectionPoint<?> injectionPoint);

}
