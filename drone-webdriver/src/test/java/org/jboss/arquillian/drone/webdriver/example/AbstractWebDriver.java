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

import java.net.URL;

import org.apache.commons.lang.Validate;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

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
        return Deployments.createDeployment();
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
    protected void checkElementPresence(final WebDriver driver, final By by, String errorMsg) {
        new WebDriverWaitWithMessage(driver(), 10).failWith(errorMsg).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                try {
                    return driver.findElement(by).isDisplayed();
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
            Validate.notNull(message);
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
