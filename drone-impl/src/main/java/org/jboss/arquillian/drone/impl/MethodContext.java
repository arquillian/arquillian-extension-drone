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

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder of Drone context for method based life cycle. It is able to store different instances of drone instances as well as
 * their configurations and to retrieve them during testing.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class MethodContext {
    private ConcurrentHashMap<Method, DroneContext> cache = new ConcurrentHashMap<Method, DroneContext>();

    /**
     * Gets context with is bound to a test method
     *
     * @param key The test method
     * @return Drone context
     */
    public DroneContext get(Method key) {
        return cache.get(key);
    }

    /**
     * Puts value into context if it doesn't exist already
     *
     * @param key The test method
     * @param value Context for method
     * @return Actual context for method
     */
    public DroneContext getOrCreate(Method key) {
        DroneContext newContext = new DroneContext();
        DroneContext dc = cache.putIfAbsent(key, new DroneContext());
        return dc == null ? newContext : dc;
    }

    /**
     * Removes context bound to a method
     *
     * @param key The test method
     * @return Modified instance
     */
    public MethodContext remove(Method key) {
        cache.remove(key);
        return this;
    }

}
