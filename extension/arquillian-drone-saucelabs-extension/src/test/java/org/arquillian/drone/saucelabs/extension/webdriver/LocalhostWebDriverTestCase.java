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

import java.io.File;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Tests Arquillian Drone WebDriver against currently used browser
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(Arquillian.class)
public class LocalhostWebDriverTestCase {

    protected static final String USERNAME = "demo";
    protected static final String PASSWORD = "demo";

    @Drone
    WebDriver driver;

    @ArquillianResource
    URL contextPath;

    @Deployment(testable = false)
    public static WebArchive deploySample() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addAsWebResource(new File("../../drone-webdriver/src/test/resources/form.html"), "form.html")
            .addAsWebResource(new File("../../drone-webdriver/src/test/resources/js/jquery-1.8.2.min.js"),
                "js/jquery-1.8.2.min.js");
    }

    @Test
    @InSequence(1)
    @Ignore("All the Sauce Labs tests are ignored, because of missing username and access key to some active account - see arquillian.xml.")
    public void login() throws InterruptedException {
        LoginPage page = new LoginPage(driver, contextPath);
        page.login(USERNAME, PASSWORD);
    }

    @Test
    @InSequence(2)
    @Ignore("All the Sauce Labs tests are ignored, because of missing username and access key to some active account - see arquillian.xml.")
    public void logout() {
        LoginPage page = new LoginPage(driver, contextPath);
        page.logout();
    }
}
