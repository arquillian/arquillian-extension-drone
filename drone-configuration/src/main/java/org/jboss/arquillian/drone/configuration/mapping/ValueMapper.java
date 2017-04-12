/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.configuration.mapping;

/**
 * Transformer for a string to an object of given type
 *
 * @param <T>
 *     Given type
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public interface ValueMapper<T> {

    /**
     * Checks if given type is supported by this handler
     *
     * @param type
     *     Type to be supported
     * @param parameters
     *     Types that have to match in case type is generic type
     *
     * @return {@code true} if this is supported, {@code false} otherwise
     */
    boolean handles(Class<?> type, Class<?>... parameters);

    /**
     * Returns converted value from a string
     *
     * @param value
     *     String value to be converted
     *
     * @return Converted object
     *
     * @throws IllegalArgumentException
     *     If conversion was not possible
     */
    T transform(String value) throws IllegalArgumentException;
}
