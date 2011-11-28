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
import java.net.URI;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.selenium.example.webapp.Credentials;
import org.jboss.arquillian.drone.selenium.example.webapp.LoggedIn;
import org.jboss.arquillian.drone.selenium.example.webapp.Login;
import org.jboss.arquillian.drone.selenium.example.webapp.User;
import org.jboss.arquillian.drone.selenium.example.webapp.Users;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Tests Arquillian Drone extension against Weld Login example.
 * <p/>
 * Uses legacy Selenium driver bound to Firefox browser.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(Arquillian.class)
public class DefaultSeleniumTestCase {
    // load selenium driver
    @Drone
    DefaultSelenium driver;

    // Load context path to the test
    @ArquillianResource
    URI contextPath;

    private static final String USERNAME = "demo";
    private static final String PASSWORD = "demo";

    private static final String LOGGED_IN = "xpath=//li[contains(text(),'Welcome')]";
    private static final String LOGGED_OUT = "xpath=//li[contains(text(),'Goodbye')]";

    private static final String USERNAME_FIELD = "id=loginForm:username";
    private static final String PASSWORD_FIELD = "id=loginForm:password";

    private static final String LOGIN_BUTTON = "id=loginForm:login";
    private static final String LOGOUT_BUTTON = "id=loginForm:logout";

    private static final String TIMEOUT = "15000";

    /**
     * Creates a WAR of a Weld based application using ShrinkWrap
     *
     * @return WebArchive to be tested
     */
    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        boolean isRunningOnAS7 = System.getProperty("jbossHome", "target/jboss-as-7.0.2.Final").contains("7.0.2.Final");

        WebArchive war = ShrinkWrap
                .create(WebArchive.class, "weld-login.war")
                .addClasses(Credentials.class, LoggedIn.class, Login.class, User.class, Users.class)
                .addAsResource(new File("src/test/resources/import.sql"))
                .addAsWebInfResource(new File("src/test/webapp/WEB-INF/beans.xml"))
                .addAsWebInfResource(new File("src/test/webapp/WEB-INF/faces-config.xml"))
                .addAsWebResource(new File("src/test/webapp/index.html"))
                .addAsWebResource(new File("src/test/webapp/home.xhtml"))
                .addAsWebResource(new File("src/test/webapp/template.xhtml"))
                .addAsWebResource(new File("src/test/webapp/users.xhtml"))
                .addAsResource(
                        isRunningOnAS7 ? new File("src/test/resources/META-INF/persistence.xml") : new File(
                                "src/test/resources/META-INF/persistence-as6.xml"),
                        ArchivePaths.create("META-INF/persistence.xml")).setWebXML(new File("src/test/webapp/WEB-INF/web.xml"));

        return war;
    }

    @Test
    public void testLoginAndLogout() {
        Assert.assertNotNull("Path is not null", contextPath);
        Assert.assertNotNull("Default Selenium is not null", driver);

        driver.open(contextPath + "/home.jsf");

        driver.type(USERNAME_FIELD, USERNAME);
        driver.type(PASSWORD_FIELD, PASSWORD);
        driver.click(LOGIN_BUTTON);
        driver.waitForPageToLoad(TIMEOUT);

        Assert.assertTrue("User should be logged in!", driver.isElementPresent(LOGGED_IN));

        driver.click(LOGOUT_BUTTON);
        driver.waitForPageToLoad(TIMEOUT);
        Assert.assertTrue("User should not be logged in!", driver.isElementPresent(LOGGED_OUT));

    }

}
