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

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.webdriver.example.webapp.Credentials;
import org.jboss.arquillian.drone.webdriver.example.webapp.LoggedIn;
import org.jboss.arquillian.drone.webdriver.example.webapp.Login;
import org.jboss.arquillian.drone.webdriver.example.webapp.User;
import org.jboss.arquillian.drone.webdriver.example.webapp.Users;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

/**
 * Tests Arquillian Selenium extension against Weld Login example.
 *
 * Uses standard settings of Selenium 2.0, that is HtmlUnitDriver by default, but allows user to pass another driver specified
 * as a System property or in the Arquillian configuration.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 * @see org.jboss.arquillian.drone.webdriver.factory.WebDriverFactory
 */
public abstract class AbstractWebDriver {
    protected static final String USERNAME = "demo";
    protected static final String PASSWORD = "demo";

    protected static final By LOGGED_IN = By.xpath("//li[contains(text(),'Welcome')]");
    protected static final By LOGGED_OUT = By.xpath("//li[contains(text(),'Goodbye')]");

    protected static final By USERNAME_FIELD = By.id("loginForm:username");
    protected static final By PASSWORD_FIELD = By.id("loginForm:password");

    protected static final By LOGIN_BUTTON = By.id("loginForm:login");
    protected static final By LOGOUT_BUTTON = By.id("loginForm:logout");

    @ArquillianResource
    URL contextPath;

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
        Assert.assertNotNull("WebDriver is not null", driver());

        driver().get(contextPath + "home.jsf");

        driver().findElement(USERNAME_FIELD).sendKeys(USERNAME);
        driver().findElement(PASSWORD_FIELD).sendKeys(PASSWORD);
        driver().findElement(LOGIN_BUTTON).click();
        checkElementPresence(driver(), LOGGED_IN, "User should be logged in!");

        driver().findElement(LOGOUT_BUTTON).click();
        checkElementPresence(driver(), LOGGED_OUT, "User should not be logged in!");

    }

    protected abstract WebDriver driver();

    // check is element is presence on page, fails otherwise
    protected void checkElementPresence(WebDriver driver, By by, String errorMsg) {
        try {
            Assert.assertTrue(errorMsg, driver.findElement(by) != null);
        } catch (NoSuchElementException e) {
            Assert.fail(errorMsg);
        }

    }

}
