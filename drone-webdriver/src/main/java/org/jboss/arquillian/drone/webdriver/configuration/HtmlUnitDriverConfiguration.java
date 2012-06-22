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

import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Configuration for HtmlUnit driver.
 *
 * Note that we allow HtmlUnit to run in Remote mode even if it is not a direct implementation of {@link RemoteWebDriver}
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public interface HtmlUnitDriverConfiguration extends CommonWebDriverConfiguration {

    /**
     * @return the applicationName
     */
    String getApplicationName();

    /**
     * @param applicationName the applicationName to set
     */
    void setApplicationName(String applicationName);

    /**
     * @return the applicationVersion
     */
    String getApplicationVersion();

    /**
     * @param applicationVersion the applicationVersion to set
     */
    void setApplicationVersion(String applicationVersion);

    /**
     * @return the userAgent
     */
    String getUserAgent();

    /**
     * @param userAgent the userAgent to set
     */
    void setUserAgent(String userAgent);

    /**
     * @return the browserVersionNumeric
     */
    float getBrowserVersionNumeric();

    /**
     * @param browserVersionNumeric the browserVersionNumeric to set
     */
    void setBrowserVersionNumeric(float browserVersionNumeric);

    /**
     * @return the useJavaScript
     */
    boolean isUseJavaScript();

    /**
     * @param useJavaScript the useJavaScript to set
     */
    void setUseJavaScript(boolean useJavaScript);
}
