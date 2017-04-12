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
package org.jboss.arquillian.drone.webdriver.spi;

import java.util.Collection;

/**
 * A registry that holds all browser mappable types
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public interface BrowserCapabilitiesRegistry {

    /**
     * Checks if the registry has an entry for given key
     *
     * @param key
     *     the human readable name for the browser, e.g. firefox
     *
     * @return The BrowserCapabilities object for given key or {@code null} if key is {@code null}, empty or it is not in
     * the
     * registry
     */
    BrowserCapabilities getEntryFor(String key) throws IllegalStateException;

    /**
     * Checks if the registry has an entry for given implementation class name.
     * <p>
     * This is a legacy method allowing backwards compatibility with Drone 1.0.0
     *
     * @param className
     *     Implementation class for appropriate WebDriver type
     *
     * @return The {@link BrowserCapabilities} object for given implementation class or {@code null} if className is
     * {@code null}, empty or it is not in the registry
     */
    @Deprecated
    BrowserCapabilities getEntryByImplementationClassName(String className);

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
    BrowserCapabilitiesRegistry registerBrowserCapabilitiesFor(String key, BrowserCapabilities browserCapabilities);

    /**
     * Returns an immutable collection of all {@link BrowserCapabilities} available
     *
     * @return the collection of currently registered {@link BrowserCapabilities}
     */
    Collection<BrowserCapabilities> getAllBrowserCapabilities();
}
