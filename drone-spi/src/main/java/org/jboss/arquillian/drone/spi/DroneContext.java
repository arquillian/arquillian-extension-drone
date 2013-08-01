/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.drone.spi;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;

/**
 * Context for Drone instance, Drone callable instances and Drone configuration.
 *
 * Context allows to store both class scoped Drones and method scoped Drones.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public interface DroneContext {

    /**
     * Gets object stored under given qualifier and given key
     *
     * @param key Key used to find the object
     * @param qualifier Qualifier used to find the object
     * @return Object stored under given qualified key
     */
    InstanceOrCallableInstance get(Class<?> key, Class<? extends Annotation> qualifier);

    /**
     * Adds object under given key and given qualifier
     *
     * @param <T> Type of the object
     * @param key Key used to store the object
     * @param qualifier Qualifier used to store the object
     * @param union Object to be stored
     * @return Modified context
     */
    DroneContext add(Class<?> key, Class<? extends Annotation> qualifier, InstanceOrCallableInstance union);

    /**
     * Removes object under given key and given qualifier
     *
     * @param key Key used to find the object
     * @param qualifier Qualifier used to find the object
     * @return Modified context
     */
    DroneContext remove(Class<?> key, Class<? extends Annotation> qualifier);

    /**
     * Holder of either instance or Callable that after invoked, will result into the instance.
     *
     */
    static interface InstanceOrCallableInstance {

        /**
         * Sets value to {@code object}. This is a convenient method to modify content of {@see DroneContext} from various
         * events.
         *
         * @param object new value
         * @return modified instance
         * @throws IllegalArgumentException If new value is {@code null}
         */
        InstanceOrCallableInstance set(Object object) throws IllegalArgumentException;

        /**
         * Checks whether context contains a real instance
         *
         * @return {@code true} if instance is hold, {@code false} otherwise
         */
        boolean isInstance();

        /**
         * Checks whether context contains a real instance
         *
         * @return {@code true} if instance is hold, {@code false} otherwise
         */
        boolean isInstanceCallable();

        /**
         * Wraps as instance
         *
         * @param type Type of the instance
         * @return wrapped instance
         * @throws IllegalStateException If content is not an instance
         */
        <T> T asInstance(Class<T> type) throws IllegalStateException;

        /**
         * Wraps as callable
         *
         * @param type Type of the callable
         * @return wrapped callable
         * @throws IllegalStateException If content is not a callable
         */
        <T> Callable<T> asCallableInstance(Class<T> type) throws IllegalStateException;
    }

}
