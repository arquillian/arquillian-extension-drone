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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import qualifier.MethodSpecific;

/**
 * Test for non-remote driver with browserCapability properties specified in the configuration
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
@RunWith(Arquillian.class)
public class MethodSpecificDriverTestCase {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.createDeployment();
    }

    @ArquillianResource
    URL contextPath;

    @Test
    @InSequence(1)
    public void simpleWebdriverTest(@Drone @MethodSpecific FirefoxDriver webdriver) {
        LoginPage page = new LoginPage(webdriver, contextPath);
        page.login("demo", "demo");
        page.logout();
    }

    @Test
    @InSequence(2)
    public void simpleWebdriverChromeTest(@Drone @MethodSpecific ChromeDriver webdriver) {
        LoginPage page = new LoginPage(webdriver, contextPath);
        page.login("demo", "demo");
        page.logout();
    }
}
