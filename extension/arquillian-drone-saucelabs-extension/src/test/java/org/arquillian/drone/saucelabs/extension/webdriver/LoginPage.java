/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
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
package org.arquillian.drone.saucelabs.extension.webdriver;

import java.net.URL;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * A wrapper for login page to act as a component
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class LoginPage {

    private static final String USERNAME = "demo";
    private static final String PASSWORD = "demo";

    private static final By LOGIN_STATUS = By.id("login-status");

    private static final By USERNAME_FIELD = By.id("username");
    private static final By PASSWORD_FIELD = By.id("password");

    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By LOGOUT_BUTTON = By.id("logout-button");

    private final WebDriver driver;
    private final URL contextPath;

    public LoginPage(WebDriver driver, URL contextPath) {
        this.driver = driver;
        this.contextPath = contextPath;
    }

    public void login(String name, String password) {
        Assert.assertNotNull("Path is not null", contextPath);
        Assert.assertNotNull("WebDriver is not null", driver);

        driver.get(contextPath.toString() + "form.html");
        driver.findElement(USERNAME_FIELD).sendKeys(USERNAME);
        driver.findElement(PASSWORD_FIELD).sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        WebDriverUtil.checkElementContent(driver, LOGIN_STATUS, "User logged in!", "User should be logged in!");
    }

    public void logout() {
        driver.findElement(LOGOUT_BUTTON).click();
        WebDriverUtil.checkElementContent(driver, LOGIN_STATUS, "Not logged in!", "User should not be logged in!");
    }
}
