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

import com.google.gson.Gson;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class AppiumDriverFactoryTest {
    private static final String PLATFORM_NAME = "platformName";
    private static final String BROWSER_NAME = "browserName";
    private static final String CHROME_OPTIONS_VALUE = "--disable-translate";

    @Test
    public void testCapabilities() throws IOException {
        MutableCapabilities originalCapabilities = new MutableCapabilities();
        originalCapabilities.setCapability(PLATFORM_NAME, "android");
        originalCapabilities.setCapability(BROWSER_NAME, "chrome");
        originalCapabilities.setCapability("chromeArguments", CHROME_OPTIONS_VALUE);

        AppiumDriverFactory factory = new AppiumDriverFactory();

        Capabilities newCapabilities = factory.getCapabilities(getMockedConfiguration(originalCapabilities));

        assertEquals(originalCapabilities.getCapability(PLATFORM_NAME), newCapabilities.getCapability(PLATFORM_NAME));
        assertEquals(originalCapabilities.getCapability(BROWSER_NAME), newCapabilities.getCapability(BROWSER_NAME));

        Object chromeOptions = newCapabilities.getCapability(AndroidMobileCapabilityType.CHROME_OPTIONS);
        assertTrue(new Gson().toJson(chromeOptions).contains(CHROME_OPTIONS_VALUE));
    }

    private WebDriverConfiguration getMockedConfiguration(MutableCapabilities capabilities) {
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);

        when(configuration.getCapabilities()).thenReturn(capabilities);

        return configuration;
    }

}
