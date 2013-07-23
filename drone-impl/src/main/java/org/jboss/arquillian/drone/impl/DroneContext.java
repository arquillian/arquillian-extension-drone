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
package org.jboss.arquillian.drone.impl;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.drone.api.annotation.Default;

/**
 * Holder of Drones.
 *
 * Allows storing following types:
 *
 * <ul>
 * <li>Drone instance</li>
 * <li>Drone callables</li>
 * <li>Drone configurations</li>
 * </ul>
 *
 * All instances are distinguished by Class type and optional qualifier annotation. If a callable is about to be retrieved, it
 * is automatically converted into a real instance using {@link DroneInstanceCreator} service.
 *
 * The implementation allows to store both class and method scoped Drones.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneContext {

    // cache holder
    private final Map<QualifiedKey, DroneInstanceContext> cache;

    // instance creator service
    private final DroneInstanceCreator instanceCreator;

    public DroneContext(DroneInstanceCreator instanceCreator) {
        this.cache = new ConcurrentHashMap<QualifiedKey, DroneInstanceContext>();
        this.instanceCreator = instanceCreator;
    }

    /**
     * Gets object stored under {@link Default} qualifier and given key
     *
     * @param <T> Type of the object
     * @param key Key used to find the object
     * @return Object stored under given qualified key
     */
    public <T> T get(Class<T> key) {
        return get(key, Default.class);
    }

    /**
     * Gets object stored under given qualifier and given key
     *
     * @param <T> Type of the object
     * @param key Key used to find the object
     * @param qualifier Qualifier used to find the object
     * @return Object stored under given qualified key
     */
    public <T> T get(Class<T> key, Class<? extends Annotation> qualifier) {

        DroneInstanceContext context = cache.get(new QualifiedKey(key, qualifier));
        if (context == null) {
            return null;
        }

        return key.cast(getOrExecuteCallable(context, key, qualifier));
    }

    /**
     * Adds object under given key and {@link Default} qualifier
     *
     * @param <T> Type of the object
     * @param key Key used to store the object
     * @param instance Object to be stored
     * @return Modified context
     */
    public <T> DroneContext add(Class<T> key, T instance) {
        return add(key, Default.class, instance);
    }

    /**
     * Adds object under given key and given qualifier
     *
     * @param <T> Type of the object
     * @param key Key used to store the object
     * @param qualifier Qualifier used to store the object
     * @param instance Object to be stored
     * @return Modified context
     */
    public <T> DroneContext add(Class<?> key, Class<? extends Annotation> qualifier, T instance) {
        QualifiedKey k = new QualifiedKey(key, qualifier);
        DroneInstanceContext context = cache.get(k);
        if (context == null) {
            context = new DroneInstanceContext();
            cache.put(k, context);
        }

        context.push(instance);
        return this;
    }

    /**
     * Replaces entry under key and qualifier if it exists, adds otherwise
     *
     * @param <T> Type of the object
     * @param key Key used to store the object
     * @param qualifier Qualifier used to store the object
     * @param instance Object to be stored
     * @return Modified context
     */
    public <T> DroneContext replace(Class<?> key, Class<? extends Annotation> qualifier, T instance) {
        return remove(key, qualifier).add(key, qualifier, instance);
    }

    public <T> DroneContext replace(Class<?> key, T instance) {
        return replace(key, Default.class, instance);
    }

    /**
     * Removes object under given key and {@link Default} qualifier
     *
     * @param key Key used to find the object
     * @return Modified context
     */
    public DroneContext remove(Class<?> key) {
        return remove(key, Default.class);
    }

    /**
     * Removes object under given key and given qualifier
     *
     * @param key Key used to find the object
     * @param qualifier Qualifier used to find the object
     * @return Modified context
     */
    public DroneContext remove(Class<?> key, Class<? extends Annotation> qualifier) {
        QualifiedKey k = new QualifiedKey(key, qualifier);
        DroneInstanceContext context = cache.get(k);
        if (context == null) {
            // already removed, do nothing
            return this;
        }

        // remove instance
        context.pop();
        // remove DroneInstanceContext if empty
        if (context.isEmpty()) {
            cache.remove(k);
        }

        return this;

    }

    /**
     * Retrieves an instance from current context. This co
     *
     * @param context
     * @param droneType
     * @param qualifier
     * @return
     */
    private Object getOrExecuteCallable(DroneInstanceContext context, Class<?> droneType, Class<? extends Annotation> qualifier) {
        if (context.isEmpty()) {
            throw new IllegalStateException("Unable to retrieve Drone instance, it was not initialized yet");
        }

        Object candidate = context.peek();
        // here we execute Callable to get real instance
        if (candidate instanceof Callable<?>) {
            Object instance = instanceCreator.createDroneInstance((Callable<?>) candidate, droneType, qualifier, 600,
                    TimeUnit.SECONDS);
            return instance;
        }
        return candidate;

    }

    static class QualifiedKey {
        private final Class<?> key;
        private final Class<? extends Annotation> qualifier;

        public QualifiedKey(Class<?> key, Class<? extends Annotation> qualifier) {
            this.key = key;
            this.qualifier = qualifier;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((qualifier == null) ? 0 : qualifier.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            QualifiedKey other = (QualifiedKey) obj;
            if (qualifier == null) {
                if (other.qualifier != null)
                    return false;
            } else if (!qualifier.equals(other.qualifier))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return key.getName() + "/" + qualifier.getSimpleName();
        }

    }

    static class DroneInstanceContext {
        private final Stack<Object> stack;

        public DroneInstanceContext() {
            this.stack = new Stack<Object>();
        }

        public DroneInstanceContext push(Object object) {
            stack.push(object);
            return this;
        }

        public Object peek() {
            if (isEmpty()) {
                throw new IllegalStateException("Unable to retrieve Drone instance, it was not initialized yet");
            }
            return stack.peek();
        }

        public Object pop() {
            if (isEmpty()) {
                throw new IllegalStateException("Unable to retrieve Drone instance, it was not initialized yet");
            }
            return stack.pop();
        }

        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }

}
