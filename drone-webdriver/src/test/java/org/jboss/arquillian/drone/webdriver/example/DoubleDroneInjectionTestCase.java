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

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil.assumeBrowserNotEqual;

/**
 * Test for ARQ-1543. Checks that everything works fine if user misconfigures Drone by injecting same instance twice in a
 * single
 * scope.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(Arquillian.class)
public class DoubleDroneInjectionTestCase {

    @Drone
    WebDriver webdriver1;

    // ARQ-1543
    // this driver is not used
    @Drone
    WebDriver webdriver2;

    @BeforeClass
    public static void skipIfEdgeBrowser() {
        assumeBrowserNotEqual("edge");
    }

    @Test
    public void doubleMethodWebDrivers(@Drone WebDriver webdriver1, @Drone WebDriver webdriver2) {

        Assert.assertNotNull("Class scoped webdriver1 was instantiated", this.webdriver1);
        Assert.assertNotNull("Class scoped webdriver2 was instantiated", this.webdriver2);
        Assert.assertNotNull("Method scoped webdriver1 was instantiated", webdriver1);
        Assert.assertNotNull("Method scoped webdriver2 was instantiated", webdriver2);
    }
}
