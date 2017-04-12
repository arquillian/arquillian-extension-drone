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
 * Defines a contract for destroying instances of the Drone Driver
 *
 * @param <T>
 *     Type of the driver that the destructor is able to destroy
 */
public interface Destructor<T> extends Sortable {
    /**
     * Destroys an instance of the driver.
     * <p>
     * After the last method is run, the driver instance is destroyed. This means browser windows, if any, are closed and
     * used
     * resources are freed.
     *
     * @param instance
     *     The instance to be destroyed
     */
    void destroyInstance(T instance);
}
