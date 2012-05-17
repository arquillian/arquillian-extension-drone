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
package org.jboss.arquillian.drone.webdriver.example;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

/**
 * Tests Arquillian Selenium extension against Weld Login example.
 *
 * Uses standard settings of Selenium 2.0, that is RemoteWebDriver by default, but allows user to pass another driver specified
 * as a System property or in the Arquillian configuration.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 *
 * @see org.jboss.arquillian.drone.webdriver.factory.WebDriverFactory
 */
@RunWith(Arquillian.class)
public class RemoteWebDriverTestCase extends AbstractWebDriver {

    @Drone
    RemoteWebDriver driver;

    private SessionId sessionId;

    @BeforeClass
    public static void prepareSystemProperties() {
        System.setProperty("arquillian.webdriver.implementation.class", "org.openqa.selenium.remote.RemoteWebDriver");
    }

    @AfterClass
    public static void cleanSystemProperties() {
        System.clearProperty("arquillian.webdriver.implementationClass");
    }

    @Test
    public void testReusableSessionId1(@Drone RemoteWebDriver d) {
        testReusableSessionId(d);
    }

    @Test
    public void testReusableSessionId2(@Drone RemoteWebDriver d) {
        testReusableSessionId(d);
    }

    @Test
    public void testReusableSessionId3(@Drone WebDriver d) {
        testReusableSessionId(d);
    }

    @Test
    public void testReusableSessionId4(@Drone WebDriver d) {
        testReusableSessionId(d);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.webdriver.example.AbstractWebDriverTestCase#driver()
     */
    @Override
    protected WebDriver driver() {
        return driver;
    }

    private void testReusableSessionId(WebDriver d) {
        RemoteWebDriver rd = (RemoteWebDriver) d;
        if (sessionId == null) {
            sessionId = rd.getSessionId();
        } else {
            assertEquals(sessionId, rd.getSessionId());
        }
    }

}
