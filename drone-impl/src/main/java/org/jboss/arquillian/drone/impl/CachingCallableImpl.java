/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.arquillian.drone.spi.CachingCallable;

public abstract class CachingCallableImpl<V> implements CachingCallable<V> {

    private final AtomicBoolean valueCached;
    private V cachedValue;

    public CachingCallableImpl() {
        valueCached = new AtomicBoolean();
    }

    @Override
    public boolean isValueCached() {
        return valueCached.get();
    }

    @Override
    public V call() throws Exception {
        if (isValueCached()) {
            return cachedValue;
        }

        synchronized (valueCached) {
            if (!valueCached.get()) {
                cachedValue = createInstance();
                valueCached.set(true);
            }
        }

        return cachedValue;
    }

    protected abstract V createInstance() throws Exception;
}
