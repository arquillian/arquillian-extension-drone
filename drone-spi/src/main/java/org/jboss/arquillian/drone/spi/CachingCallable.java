package org.jboss.arquillian.drone.spi;

import java.util.concurrent.Callable;

/**
 * Created by tkriz on 08/02/14.
 */
public interface CachingCallable<V> extends Callable<V> {

    boolean isValueCached();

}
