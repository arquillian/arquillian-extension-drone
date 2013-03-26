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

import java.lang.annotation.Annotation;

/**
 * Enhances Drone instance in order to give it additional capabilities.
 *
 * @author Lukas Fryc
 */
public interface Enhancer<T> extends Sortable {

    /**
     * Returns true when this {@link Enhancer} is able to enhance given type.
     *
     * @param type the type which can be enhanced
     * @param qualifier the qualifier associated with the injected type
     * @return true when this {@link Enhancer} is able to enhance given type.
     */
    boolean canEnhance(Class<?> type, Class<? extends Annotation> qualifier);

    /**
     * <p>
     * Takes the instance instantiated by Drone and returns its enhanced version.
     * </p>
     *
     * <p>
     * Note: No enhancement can be done in this method.
     * </p>
     *
     * @param instance the instance to be enhanced
     * @param qualifier the qualifier associated with an instance
     * @return the enhanced instance
     */
    T enhance(T instance, Class<? extends Annotation> qualifier);

    /**
     * <p>
     * Takes the instance which was previously enhanced by {@link #enhance(Object)} method and cancels the enhancement.
     * </p>
     *
     * <p>
     * No: No de-enhancement can be done in this method.
     * </p>
     *
     * @param enhancedInstance the instance which was previously enhanced by {@link #enhance(Object)}.
     * @param qualifier the qualifier associated with an instance
     * @return the de-enhanced instance
     */
    T deenhance(T enhancedInstance, Class<? extends Annotation> qualifier);
}
