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
import org.jboss.arquillian.drone.webdriver.factory.remote.reusable.ReusableRemoteWebDriver;
import org.jboss.arquillian.drone.webdriver.utils.UrlUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import qualifier.Reusable;

import static org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil.assumeBrowserNotEqual;

/**
 * Tests Arquillian Selenium extension against Weld Login example.
 * <p>
 * Uses standard settings of Selenium 2.0, that is RemoteWebDriver by default, but allows user to pass another driver
 * specified
 * as a System property or in the Arquillian configuration.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 * @see org.jboss.arquillian.drone.webdriver.factory.WebDriverFactory
 */
@RunWith(Arquillian.class)
public class ReusableRemoteWebDriverTestCase {

    // sessionId is set to be static, so it can be shared between test methods
    private static SessionId sessionId;

    @BeforeClass
    public static void skipIfEdgeBrowser() {
        assumeBrowserNotEqual("edge");
    }

    private void checkIfWebdriverHubIsRunning() {
        Assert.assertTrue(
            "Remote Reusable tests require Selenium Server to be running on port 4444, please start it manually before running the tests.",
            UrlUtils.isSeleniumHubRunningOnDefaultUrl());
    }

    @Test
    @InSequence(1)
    public void testReusableSessionId1(@Drone @Reusable RemoteWebDriver d) {
        checkIfWebdriverHubIsRunning();
        Assert.assertNotNull("Browser has sessionId set up", d.getSessionId());
        sessionId = d.getSessionId();
    }

    @Test
    @InSequence(2)
    public void testReusableSessionId2(@Drone @Reusable RemoteWebDriver d) {
        checkIfWebdriverHubIsRunning();
        Assert.assertTrue("Drone instance is reusable", d instanceof ReusableRemoteWebDriver);
        Assert.assertNotNull("Browser has sessionId set up", d.getSessionId());
        Assert.assertEquals("SessionId was reused", sessionId, d.getSessionId());
        sessionId = null;
    }

    @Test
    @InSequence(3)
    public void testReusableSessionId3(@Drone @Reusable WebDriver d) {
        checkIfWebdriverHubIsRunning();
        Assert.assertNotNull("Browser has sessionId set up", ((RemoteWebDriver) d).getSessionId());
        sessionId = ((RemoteWebDriver) d).getSessionId();
    }

    @Test
    @InSequence(4)
    public void testReusableSessionId4(@Drone @Reusable WebDriver d) {
        checkIfWebdriverHubIsRunning();
        Assert.assertTrue("Drone instance is reusable", d instanceof ReusableRemoteWebDriver);
        Assert.assertNotNull("Browser has sessionId set up", ((RemoteWebDriver) d).getSessionId());
        Assert.assertEquals("SessionId was reused", sessionId, ((RemoteWebDriver) d).getSessionId());
        sessionId = null;
    }

    @Test
    @InSequence(5)
    public void testNonReusableSession1(@Drone WebDriver d) {
        checkIfWebdriverHubIsRunning();
        Assert.assertFalse("Drone instance is not reusable", d instanceof ReusableRemoteWebDriver);
    }
}
