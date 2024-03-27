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
package org.arquillian.drone.browserstack.extension.webdriver;

import java.util.Map;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.openqa.selenium.ImmutableCapabilities;

/**
 * An internal mapping between browser capabilities property, implementation class and Capabilities. This class
 * also
 * supports implementationClass property which is now legacy configuration value.
 *
 * @see ImmutableCapabilities
 */
public class BrowserStackCapabilities implements BrowserCapabilities {

    public static final String READABLE_NAME = "browserstack";
    public static final String USERNAME = "username";
    public static final String ACCESS_KEY = "access.key";
    public static final String URL = "url";
    public static final String BROWSERSTACK_LOCAL_MANAGED = "browserstack.local.managed";
    public static final String BROWSERSTACK_LOCAL_BINARY = "browserstack.local.binary";
    public static final String BROWSERSTACK_LOCAL_ARGS = "browserstack.local.args";

    public static final String BROWSERSTACK_LOCAL = "browserstack.local";

    public String getImplementationClassName() {
        return BrowserStackDriver.class.getName();
    }

    public Map<String, ?> getRawCapabilities() {
        return new ImmutableCapabilities().asMap();
    }

    public String getReadableName() {
        return READABLE_NAME;
    }

    public int getPrecedence() {
        return 0;
    }
}
