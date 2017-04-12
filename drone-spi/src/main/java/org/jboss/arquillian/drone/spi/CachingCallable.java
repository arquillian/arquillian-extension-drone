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
package org.jboss.arquillian.drone.spi;

import java.util.concurrent.Callable;

/**
 * Utility interface that should cache the return value of {@link java.util.concurrent.Callable#call()} and each next
 * call of the method, it should return the cached value.
 *
 * @param <V>
 *     type of cached value
 */
public interface CachingCallable<V> extends Callable<V> {

    /**
     * Returns true if the value has already been cached.
     */
    boolean isValueCached();
}
