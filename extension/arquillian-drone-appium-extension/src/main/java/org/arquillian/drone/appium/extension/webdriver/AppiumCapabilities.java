/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package org.arquillian.drone.appium.extension.webdriver;

import io.appium.java_client.AppiumDriver;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class AppiumCapabilities implements BrowserCapabilities {
    public static final String READABLE_NAME = "appium";

    @Override
    public String getImplementationClassName() {
        return AppiumDriver.class.getName();
    }

    @Override
    public Map<String, ?> getRawCapabilities() {
        return new DesiredCapabilities().asMap();
    }

    @Override
    public String getReadableName() {
        return READABLE_NAME;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }
}
