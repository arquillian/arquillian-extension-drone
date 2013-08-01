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
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.spi.DroneContext;

/**
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneContextImpl implements DroneContext {

    // cache holder
    private final Map<QualifiedKey, DroneInstanceContext> cache;

    public DroneContextImpl() {
        this.cache = new ConcurrentHashMap<QualifiedKey, DroneInstanceContext>();
    }

    /**
     * Gets object stored under {@link Default} qualifier and given key
     *
     * @param key Key used to find the object
     * @return Object stored under given qualified key
     */
    public InstanceOrCallableInstance get(Class<?> key) {
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
    @Override
    public InstanceOrCallableInstance get(Class<?> key, Class<? extends Annotation> qualifier) {

        DroneInstanceContext context = cache.get(new QualifiedKey(key, qualifier));
        if (context == null) {
            return null;
        }

        if (context.isEmpty()) {
            throw new IllegalStateException("Unable to retrieve Drone instance, it was not initialized yet");
        }

        return context.peek();
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
    @Override
    public DroneContext add(Class<?> key, Class<? extends Annotation> qualifier, InstanceOrCallableInstance instance) {
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
     * Removes object under given key and given qualifier
     *
     * @param key Key used to find the object
     * @param qualifier Qualifier used to find the object
     * @return Modified context
     */
    @Override
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
        private final Stack<InstanceOrCallableInstance> stack;

        public DroneInstanceContext() {
            this.stack = new Stack<InstanceOrCallableInstance>();
        }

        public DroneInstanceContext push(InstanceOrCallableInstance object) {
            stack.push(object);
            return this;
        }

        public InstanceOrCallableInstance peek() {
            if (isEmpty()) {
                throw new IllegalStateException("Unable to retrieve Drone instance, it was not initialized yet");
            }
            return stack.peek();
        }

        public InstanceOrCallableInstance pop() {
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
