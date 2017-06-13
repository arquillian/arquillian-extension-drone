/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import java.util.Date;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import qualifier.Reusable;
import qualifier.ReuseCookies;

import static org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil.assumeBrowserNotEqual;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(Arquillian.class)
public class TestReusingCookies extends AbstractInBrowserTest {

    @BeforeClass
    public static void skipIfEdgeBrowser() {
        assumeBrowserNotEqual("edge");
    }

    @Test
    @InSequence(1)
    public void testCookieWasNotThere(@Drone @Reusable WebDriver driver) {
        driver.get(HUB_URL.toString());
        Assert.assertNull("Cookie is not there", driver.manage().getCookieNamed("foo"));
    }

    @Test
    @InSequence(2)
    public void testCookieWasStored(@Drone @Reusable WebDriver driver) {
        driver.get(HUB_URL.toString());
        driver.manage().addCookie(new Cookie("foo", "bar", DOMAIN, "/", (Date) null));
        Assert.assertNotNull("Cookie was stored", driver.manage().getCookieNamed("foo"));
        Assert.assertEquals("Cookie was stored", "bar", driver.manage().getCookieNamed("foo").getValue());
    }

    @Test
    @InSequence(3)
    public void testCookieWasDeleted(@Drone @Reusable WebDriver driver) {
        driver.get(HUB_URL.toString());
        Assert.assertNull("Cookie is not there", driver.manage().getCookieNamed("foo"));
    }

    @Test
    @InSequence(4)
    public void testCookieWasNotThereAgain(@Drone @ReuseCookies WebDriver driver) {
        driver.get(HUB_URL.toString());
        Assert.assertNull("Cookie is not there", driver.manage().getCookieNamed("foo"));
    }

    @Test
    @InSequence(5)
    public void testCookieWasStoredAgain(@Drone @ReuseCookies WebDriver driver) {
        driver.get(HUB_URL.toString());
        driver.manage().addCookie(new Cookie("foo", "bar", DOMAIN, "/", (Date) null));
        Assert.assertNotNull("Cookie was stored", driver.manage().getCookieNamed("foo"));
        Assert.assertEquals("Cookie was stored", "bar", driver.manage().getCookieNamed("foo").getValue());
    }

    @Test
    @InSequence(6)
    public void testCookieWasNotDeleted(@Drone @ReuseCookies WebDriver driver) {
        driver.get(HUB_URL.toString());
        Assert.assertNotNull("Cookie was stored", driver.manage().getCookieNamed("foo"));
        Assert.assertEquals("Cookie was stored", "bar", driver.manage().getCookieNamed("foo").getValue());
    }
}
