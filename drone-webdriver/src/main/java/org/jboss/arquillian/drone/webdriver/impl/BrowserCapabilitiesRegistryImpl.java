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
package org.jboss.arquillian.drone.webdriver.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilitiesRegistry;

public class BrowserCapabilitiesRegistryImpl implements BrowserCapabilitiesRegistry {

    private Map<String, BrowserCapabilities> registry = new HashMap<String, BrowserCapabilities>();

    @Override
    public BrowserCapabilities getEntryFor(String key) {
        if (key == null || key.length() == 0) {
            return null;
        }

        return registry.get(key);
    }

    @Override
    @Deprecated
    public BrowserCapabilities getEntryByImplementationClassName(String className) {
        if (className == null || className.length() == 0) {
            return null;
        }

        for (BrowserCapabilities browserCapabilities : registry.values()) {
            if (className.equals(browserCapabilities.getImplementationClassName())) {
                return browserCapabilities;
            }
        }

        return null;
    }

    /**
     * Registers a browser capability object for given key
     *
     * @param key
     *     Type to be registered
     * @param browserCapabilities
     *     {@link BrowserCapabilities} to be stored
     *
     * @return Modified registry
     */
    @Override
    public BrowserCapabilitiesRegistryImpl registerBrowserCapabilitiesFor(String key,
        BrowserCapabilities browserCapabilities) {
        registry.put(key, browserCapabilities);
        return this;
    }

    @Override
    public Collection<BrowserCapabilities> getAllBrowserCapabilities() {
        return Collections.unmodifiableCollection(registry.values());
    }
}
