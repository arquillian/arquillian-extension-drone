/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import java.util.concurrent.Callable;

import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;

/**
 * Default implementation of {@link InstanceOrCallableInstance}
 *
 * @author <a href="mailto:kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class InstanceOrCallableInstanceImpl implements InstanceOrCallableInstance {

    private Object holder;

    public InstanceOrCallableInstanceImpl(Object object) {
        this.holder = object;
    }

    @Override
    public InstanceOrCallableInstance set(Object object) throws IllegalArgumentException {
        Validate.notNull(object, "InstanceOrCallableInstance can't set be null");
        this.holder = object;
        return this;
    }

    @Override
    public boolean isInstance() {
        return !(holder instanceof Callable<?>);
    }

    @Override
    public boolean isInstanceCallable() {
        return holder instanceof Callable<?>;
    }

    @Override
    public <T> T asInstance(Class<T> type) throws IllegalStateException {
        if (holder instanceof Callable<?>) {
            throw new IllegalStateException(
                    "Unexpected callable present in Drone Context, should be already instantiated at this moment.");
        }
        return type.cast(holder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Callable<T> asCallableInstance(Class<T> type) throws IllegalStateException {
        if (!(holder instanceof Callable<?>)) {
            throw new IllegalStateException(
                    "Unexpected object present in Drone Context, should not be instantiated at this moment yet.");
        }

        return (Callable<T>) holder;
    }

    @Override
    public String toString() {
        return "InstanceOrCallableInstance[" + (isInstanceCallable() ? "callable" : holder.getClass().getSimpleName()) + "]";
    }

}
