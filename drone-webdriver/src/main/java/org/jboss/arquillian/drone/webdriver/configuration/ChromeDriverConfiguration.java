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

import org.jboss.arquillian.drone.webdriver.factory.ChromeDriverFactory;

/**
 * Configuration for Chrome Driver
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class ChromeDriverConfiguration extends AbstractWebDriverConfiguration<ChromeDriverConfiguration> {

    private String chromeBinary;

    private String chromeDriverBinary;

    private String chromeSwitches;

    /**
     * Creates a Chrome Driver configuration
     */
    public ChromeDriverConfiguration() {
        this.implementationClass = "org.openqa.selenium.chrome.ChromeDriver";
    }

    /**
     * @return the chromeBinary
     */
    public String getChromeBinary() {
        return chromeBinary;
    }

    /**
     * @param chromeBinary the chromeBinary to set
     */
    public void setChromeBinary(String chromeBinary) {
        this.chromeBinary = chromeBinary;
    }

    /**
     * @param chromeDriverBinary the chromeDriverBinary to set
     */
    public void setChromeDriverBinary(String chromeDriverBinary) {
        this.chromeDriverBinary = chromeDriverBinary;
    }

    /**
     * @return the chromeDriverBinary
     */
    public String getChromeDriverBinary() {
        return chromeDriverBinary;
    }

    /**
     * @param chromeSwitches the chromeSwitches to set
     */
    public void setChromeSwitches(String chromeSwitches) {
        this.chromeSwitches = chromeSwitches;
    }

    /**
     * @return the chromeSwitches
     */
    public String getChromeSwitches() {
        return chromeSwitches;
    }

}
