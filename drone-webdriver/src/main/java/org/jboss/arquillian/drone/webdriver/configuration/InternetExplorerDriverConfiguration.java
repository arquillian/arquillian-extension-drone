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

import org.jboss.arquillian.drone.webdriver.factory.InternetExplorerDriverFactory;

/**
 * Configuration for Internet Explorer Driver
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class InternetExplorerDriverConfiguration extends AbstractWebDriverConfiguration<InternetExplorerDriverConfiguration> {

    private int iePort = InternetExplorerDriverFactory.DEFAULT_INTERNET_EXPLORER_PORT;

    /**
     * Creates a Internet Explorer Driver configuration
     */
    public InternetExplorerDriverConfiguration() {
        this.implementationClass = "org.openqa.selenium.ie.InternetExplorerDriver";
    }

    /**
     * @param iePort the iePort to set
     */
    public void setIePort(int iePort) {
        this.iePort = iePort;
    }

    /**
     * @return the iePort
     */
    public int getIePort() {
        return iePort;
    }

}
