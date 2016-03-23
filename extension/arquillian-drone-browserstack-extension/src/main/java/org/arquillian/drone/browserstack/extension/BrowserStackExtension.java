/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
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
package org.arquillian.drone.browserstack.extension;

import org.arquillian.drone.browserstack.extension.webdriver.BrowserStackCapabilities;
import org.arquillian.drone.browserstack.extension.webdriver.BrowserStackDriverFactory;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BrowserStackExtension implements LoadableExtension {

    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(BrowserCapabilities.class, BrowserStackCapabilities.class);
        extensionBuilder.service(Configurator.class, BrowserStackDriverFactory.class);
        extensionBuilder.service(Instantiator.class, BrowserStackDriverFactory.class);
        extensionBuilder.service(Destructor.class, BrowserStackDriverFactory.class);
    }
}

