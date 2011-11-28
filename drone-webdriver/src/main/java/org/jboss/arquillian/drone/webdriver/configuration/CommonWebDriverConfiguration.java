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

/**
 * Encapsulation of configuration properties shared among all WebDriver types
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public interface CommonWebDriverConfiguration extends WebDriverConfigurationType {

    /**
     * Gets class which points to the implementation of the driver
     *
     * @return the class
     */
    String getImplementationClass();

    /**
     * Sets class which points to the implementation of the driver
     *
     * @param implementationClass the class which implements the driver
     */
    void setImplementationClass(String implementationClass);

}
