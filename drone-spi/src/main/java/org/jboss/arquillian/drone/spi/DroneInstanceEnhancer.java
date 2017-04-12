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
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author Lukas Fryc
 * @deprecated
 */
@Deprecated
public interface DroneInstanceEnhancer<T> extends Sortable {

    /**
     * Returns {@code true} when this {@link DroneInstanceEnhancer} is able to enhance or deenhance given type.
     *
     * @param instance
     *     instance of Drone to be enhanced / deenhanced
     * @param droneType
     *     the field or parameter type defined in test
     * @param qualifier
     *     the qualifier associated with the injected type
     *
     * @return {@code true} when this {@link DroneInstanceEnhancer} is able to enhance or deenhance given type, {@code
     * false}
     * otherwise
     */
    boolean canEnhance(InstanceOrCallableInstance instance, Class<?> droneType, Class<? extends Annotation> qualifier);

    /**
     * <p>
     * Takes the instance instantiated by Drone and returns its enhanced version.
     * </p>
     *
     * @param instance
     *     the instance to be enhanced
     * @param qualifier
     *     the qualifier associated with an instance
     *
     * @return the enhanced instance
     */
    T enhance(T instance, Class<? extends Annotation> qualifier);

    /**
     * <p>
     * Takes the instance which was previously enhanced by {@link #enhance(Object, Class)} method and cancels the
     * enhancement.
     * </p>
     *
     * @param enhancedInstance
     *     the instance which was previously enhanced by {@link #enhance(Object)}.
     * @param qualifier
     *     the qualifier associated with an instance
     *
     * @return the deenhanced instance
     */
    T deenhance(T enhancedInstance, Class<? extends Annotation> qualifier);
}
