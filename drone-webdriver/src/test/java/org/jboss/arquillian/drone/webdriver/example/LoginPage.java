/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import java.net.URL;
import java.util.function.Function;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        checkElementContent(LOGIN_STATUS, "User logged in!", "User should be logged in!");
    }

    public void logout() {
        driver.findElement(LOGOUT_BUTTON).click();
        checkElementContent(LOGIN_STATUS, "Not logged in!", "User should not be logged in!");
    }

    // check is element is presence on page, fails otherwise
    protected void checkElementContent(final By by, final String expectedContent, final String errorMsg) {
        new WebDriverWaitWithMessage(driver, 10).failWith(errorMsg).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                try {
                    return expectedContent.equals(driver.findElement(by).getText());
                } catch (NoSuchElementException ignored) {
                    return false;
                } catch (StaleElementReferenceException ignored) {
                    return false;
                }
            }
        });
    }

    protected static class WebDriverWaitWithMessage extends WebDriverWait {

        private String message;

        public WebDriverWaitWithMessage(WebDriver driver, long timeOutInSeconds) {
            super(driver, timeOutInSeconds);
        }

        public WebDriverWait failWith(String message) {
            if (message == null || message.length() == 0) {
                throw new IllegalArgumentException("Error message must not be null nor empty");
            }
            this.message = message;
            return this;
        }

        @Override
        public <V> V until(Function<? super WebDriver, V> isTrue) {
            if (message == null) {
                return super.until(isTrue);
            } else {
                try {
                    return super.until(isTrue);
                } catch (TimeoutException e) {
                    throw new TimeoutException(message, e);
                }
            }
        }
    }
}
