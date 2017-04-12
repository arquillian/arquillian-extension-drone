/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.Capabilities;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPrivateAccessCompatibility {

    private ReusableRemoteWebDriver driver;

    @Before
    public void initializeReusableRemoteWebDriver() {
        driver = new ReusableRemoteWebDriver();
    }

    @Test
    public void testSettingCapabilitiesCompatibility() {
        Capabilities capabilities = mock(Capabilities.class);

        driver.setReusedCapabilities(capabilities);

        assertSame("Something must change internally in RemoteWebDriver, since capabilities cannot be set", capabilities,
            driver.getCapabilities());
    }
}
