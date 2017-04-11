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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.spi.Sortable;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilitiesRegistry;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

public class BrowserCapabilitiesRegistrar {

    // comparator
    private static final Comparator<Sortable> SORTABLE_COMPARATOR = new Comparator<Sortable>() {
        public int compare(Sortable o1, Sortable o2) {
            return new Integer(o1.getPrecedence()).compareTo(new Integer(o2.getPrecedence()));
        }
    };
    @Inject
    @SuiteScoped
    private InstanceProducer<BrowserCapabilitiesRegistry> browserCapabilitiesRegistry;
    @Inject
    private Instance<ServiceLoader> serviceLoader;

    public void register(@Observes BeforeSuite event) {
        browserCapabilitiesRegistry.set(new BrowserCapabilitiesRegistryImpl());
        registerBrowserCapabilities();
    }

    private void registerBrowserCapabilities() {
        List<BrowserCapabilities> list =
            new ArrayList<BrowserCapabilities>(serviceLoader.get().all(BrowserCapabilities.class));
        Collections.sort(list, SORTABLE_COMPARATOR);

        for (BrowserCapabilities browser : list) {
            final String browserReadableName = browser.getReadableName();
            final String browserName = browserReadableName != null ? browserReadableName.toLowerCase() : null;
            browserCapabilitiesRegistry.get().registerBrowserCapabilitiesFor(browserName, browser);
        }
    }
}
