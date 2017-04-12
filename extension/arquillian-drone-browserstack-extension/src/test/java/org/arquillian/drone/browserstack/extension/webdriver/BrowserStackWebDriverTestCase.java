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
package org.arquillian.drone.browserstack.extension.webdriver;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class BrowserStackWebDriverTestCase {

    private static final By QUERY_FIELD = By.name("q");
    private static final By SEARCH_RESULTS = By.id("search");

    @Drone
    WebDriver driver;

    @Test
    @Ignore("All the BrowserStack tests are ignored, because of missing username and access key to some active account - see arquillian.xml.")
    public void browserTest() {
        runTest(driver);
    }

    @Test
    @Ignore("All the BrowserStack tests are ignored, because of missing username and access key to some active account - see arquillian.xml.")
    public void browserTest(@Drone final WebDriver driver) {
        runTest(driver);
    }

    private void runTest(WebDriver driver) {
        driver.get("http://www.google.com/");
        WebDriverUtil.checkElementIsPresent(driver, QUERY_FIELD, "The query field should be present");

        WebElement queryElement = driver.findElement(QUERY_FIELD);
        queryElement.sendKeys("browserstack");
        queryElement.submit();

        WebDriverUtil.checkElementContent(driver, SEARCH_RESULTS, "BrowserStack",
            "The search result should contain a string \"BrowserStack\"");
    }
}
