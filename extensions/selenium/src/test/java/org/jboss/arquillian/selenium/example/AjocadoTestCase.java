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

import static org.jboss.arquillian.ajocado.Ajocado.elementPresent;
import static org.jboss.arquillian.ajocado.Ajocado.waitModel;
import static org.jboss.arquillian.ajocado.guard.request.RequestTypeGuardFactory.waitHttp;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.xp;
import static org.jboss.arquillian.api.RunModeType.AS_CLIENT;

import java.net.URL;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.IdLocator;
import org.jboss.arquillian.ajocado.locator.XpathLocator;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.selenium.annotation.ContextPath;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests Arquillian Selenium extension against Weld Login example.
 * 
 * Uses legacy Selenium driver bound to Firefox browser.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class AjocadoTestCase extends AbstractTestCase
{
   // load selenium driver
   @Selenium
   AjaxSelenium driver;

   // Load context path to the test
   @ContextPath
   URL contextPath;

   protected XpathLocator LOGGED_IN = xp("//li[contains(text(),'Welcome')]");
   protected XpathLocator LOGGED_OUT = xp("//li[contains(text(),'Goodbye')]");

   protected IdLocator USERNAME_FIELD = id("loginForm:username");
   protected IdLocator PASSWORD_FIELD = id("loginForm:password");

   protected IdLocator LOGIN_BUTTON = id("loginForm:login");
   protected IdLocator LOGOUT_BUTTON = id("loginForm:logout");

   @Test
   public void testLoginAndLogout()
   {
      driver.open(contextPath);
      waitModel.until(elementPresent.locator(USERNAME_FIELD));      
      Assert.assertFalse("User should not be logged in!", driver.isElementPresent(LOGOUT_BUTTON));
      driver.type(USERNAME_FIELD, "demo");
      driver.type(PASSWORD_FIELD, "demo");
      
      waitHttp(driver).click(LOGIN_BUTTON);
      Assert.assertTrue("User should be logged in!", driver.isElementPresent(LOGGED_IN));
      
      waitHttp(driver).click(LOGOUT_BUTTON);
      Assert.assertTrue("User should not be logged in!", driver.isElementPresent(LOGGED_OUT));
   }

}