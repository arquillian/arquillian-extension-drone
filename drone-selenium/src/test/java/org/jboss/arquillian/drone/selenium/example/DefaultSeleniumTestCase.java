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
package org.jboss.arquillian.drone.selenium.example;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Runs Arquillian Drone extension tests against a simple page.
 * <p/>
 * Uses legacy Selenium driver bound to Firefox browser.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(Arquillian.class)
public class DefaultSeleniumTestCase {

    @Drone
    DefaultSelenium driver;

    private static final String USERNAME = "demo";
    private static final String PASSWORD = "demo";

    private static final String LOGIN_STATUS = "id=login-status";

    private static final String USERNAME_FIELD = "id=username";
    private static final String PASSWORD_FIELD = "id=password";

    private static final String LOGIN_BUTTON = "id=login-button";
    private static final String LOGOUT_BUTTON = "id=logout-button";

    private static final String TIMEOUT = "15000";

    @ArquillianResource
    URL contextRoot;

    @Deployment(testable = false)
    public static WebArchive deploySample() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebResource(new File("src/test/resources/form.html"), "form.html")
                .addAsWebResource(new File("src/test/resources/js/jquery-1.8.2.min.js"), "js/jquery-1.8.2.min.js");
    }

    @Test
    @InSequence(1)
    public void loginPage() {
        Assert.assertNotNull("Default Selenium is not null", driver);
        Assert.assertNotNull("Context root is not null", contextRoot);

        driver.open(contextRoot.toString() + "form.html");

        driver.type(USERNAME_FIELD, USERNAME);
        driver.type(PASSWORD_FIELD, PASSWORD);
        driver.click(LOGIN_BUTTON);
        driver.waitForCondition("var value = selenium.getText(\"//span[@class='status']\"); value == \"User logged in!\";",
                TIMEOUT);

        Assert.assertEquals("User should be logged in!", "User logged in!", driver.getText(LOGIN_STATUS));
    }

    @Test
    @InSequence(2)
    public void logoutPage() {
        driver.click(LOGOUT_BUTTON);
        driver.waitForCondition("var value = selenium.getText(\"//span[@class='status']\"); value == \"Not logged in!\";",
                TIMEOUT);
        Assert.assertEquals("User should not be logged in!", "Not logged in!", driver.getText(LOGIN_STATUS));
    }
}
