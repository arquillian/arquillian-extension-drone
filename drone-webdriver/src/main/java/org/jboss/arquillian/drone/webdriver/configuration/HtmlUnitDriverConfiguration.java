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
 * Configuration for HtmlUnit driver
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class HtmlUnitDriverConfiguration extends AbstractWebDriverConfiguration<HtmlUnitDriverConfiguration> {

    private String applicationName;
    private String applicationVersion;
    private String userAgent;
    private float browserVersionNumeric;
    private boolean useJavaScript = false;

    /**
     * Creates a HtmlUnit Driver configuration
     */
    public HtmlUnitDriverConfiguration() {
        this.implementationClass = "org.openqa.selenium.htmlunit.HtmlUnitDriver";
    }

    /**
     * @return the applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @param applicationName the applicationName to set
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * @return the applicationVersion
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * @param applicationVersion the applicationVersion to set
     */
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    /**
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * @param userAgent the userAgent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * @return the browserVersionNumeric
     */
    public float getBrowserVersionNumeric() {
        return browserVersionNumeric;
    }

    /**
     * @param browserVersionNumeric the browserVersionNumeric to set
     */
    public void setBrowserVersionNumeric(float browserVersionNumeric) {
        this.browserVersionNumeric = browserVersionNumeric;
    }

    /**
     * @return the useJavaScript
     */
    public boolean isUseJavaScript() {
        return useJavaScript;
    }

    /**
     * @param useJavaScript the useJavaScript to set
     */
    public void setUseJavaScript(boolean useJavaScript) {
        this.useJavaScript = useJavaScript;
    }

}
