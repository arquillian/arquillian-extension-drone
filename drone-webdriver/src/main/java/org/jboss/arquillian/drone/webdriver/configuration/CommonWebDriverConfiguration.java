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
package org.jboss.arquillian.drone.webdriver.configuration;

import org.openqa.selenium.Capabilities;

/**
 * Encapsulation of configuration properties shared among all WebDriver types
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public interface CommonWebDriverConfiguration extends WebDriverConfigurationType {

    /**
     * Gets capabilities which can be
     *
     * @return
     */
    String getBrowserCapabilities();

    /**
     * Sets browser capabilities
     *
     * @param browserCapabilities
     */
    void setBrowserCapabilities(String browserCapabilities);

    /**
     * Returns a set of capabilities for underlying browser
     *
     * @return Capabilities for given browser
     */
    Capabilities getCapabilities();

    /**
     * Gets class which points to the implementation of the driver
     *
     * @return the class
     */
    String getImplementationClass();

    /**
     * Sets class which points to the implementation of the driver.
     *
     * Do not use this method directly, rather set desired capabilities.
     *
     * @param implementationClass the class which implements the driver
     * @see CommonWebDriverConfiguration#setBrowserCapabilities(String)
     */
    @Deprecated
    void setImplementationClass(String implementationClass);
}
