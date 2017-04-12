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

/**
 * Utility interface for performing filter operation over a set of drone points.
 */
public interface FilterableResult<DRONE> extends Iterable<DronePoint<DRONE>> {

    /**
     * Returns a new instance of {@link FilterableResult} with contents of this instance filtered by given filter.
     */
    FilterableResult<DRONE> filter(DronePointFilter<? super DRONE> filter);

    /**
     * Returns a single injection point that get matched by all of specified filters.
     *
     * @throws IllegalStateException
     *     if matched injection points count is not exactly one
     */
    DronePoint<DRONE> single();

    /**
     * Returns number of drone points in this result.
     */
    int size();
}
