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

import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/**
 * Runs Arquillian Drone extension tests against a simple page.
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

    @Test
    @InSequence(1)
    public void login() {
        LoginPage page = new LoginPage(driver());
        page.login(USERNAME, PASSWORD);
    }

    @Test
    @InSequence(2)
    public void logout() {
        LoginPage page = new LoginPage(driver());
        page.logout();
    }

    protected abstract WebDriver driver();

}
