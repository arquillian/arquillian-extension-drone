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
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ftest;

import java.io.Serializable;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import qualifier.Reusable;

import static org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil.assumeBrowserNotEqual;
import static org.junit.Assert.assertTrue;

/**
 * @author Lukas Fryc
 */
@RunWith(Arquillian.class)
public class TestCapabilitiesSerialization extends AbstractInBrowserTest {

    @Drone
    @Reusable
    RemoteWebDriver driver;

    @BeforeClass
    public static void skipIfEdgeBrowser() {
        assumeBrowserNotEqual("edge");
    }

    @Test
    public void whenGetCapabilitiesFromRunningSessionThenItShouldBeSerializable() {
        Capabilities initializedCapabilities = driver.getCapabilities();

        assertTrue("Capabilities obtained from running session should be serializable",
            initializedCapabilities instanceof Serializable);
    }
}
