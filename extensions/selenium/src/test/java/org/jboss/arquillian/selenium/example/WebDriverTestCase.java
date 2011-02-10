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
package org.jboss.arquillian.selenium.example;

import static org.jboss.arquillian.api.RunModeType.AS_CLIENT;

import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.instantiator.WebDriverInstantiator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

/**
 * Tests Arquillian Selenium extension against Weld Login example.
 * 
 * Uses standard settings of Selenium 2.0, that is HtmlUnitDriver by default,
 * but allows user to pass another driver specified as a System property or in the Arquillian configuration.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see WebDriverInstantiator
 */
@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class WebDriverTestCase extends AbstractTestCase
{
   // create WebDriver
   @Selenium WebDriver driver;
   
   private static final String USERNAME = "demo";
   private static final String PASSWORD = "demo";

   private static final By LOGGED_IN = By.xpath("//li[contains(text(),'Welcome')]");
   private static final By LOGGED_OUT = By.xpath("//li[contains(text(),'Goodbye')]");

   private static final By USERNAME_FIELD = By.id("loginForm:username");
   private static final By PASSWORD_FIELD = By.id("loginForm:password");

   private static final By LOGIN_BUTTON = By.id("loginForm:login");
   private static final By LOGOUT_BUTTON = By.id("loginForm:logout");

   @Test
   public void testLoginAndLogout()
   {
      driver.get("http://localhost:8080/weld-login/home.jsf");

      driver.findElement(USERNAME_FIELD).sendKeys(USERNAME);
      driver.findElement(PASSWORD_FIELD).sendKeys(PASSWORD);
      driver.findElement(LOGIN_BUTTON).click();
      checkElementPresence(LOGGED_IN, "User should be logged in!");

      driver.findElement(LOGOUT_BUTTON).click();
      checkElementPresence(LOGGED_OUT, "User should not be logged in!");

   }

   // check is element is presence on page, fails otherwise
   private void checkElementPresence(By by, String errorMsg)
   {
      try
      {
         Assert.assertTrue(errorMsg, driver.findElement(by) != null);
      }
      catch (NoSuchElementException e)
      {
         Assert.fail(errorMsg);
      }

   }

}
