/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.webdriver.window;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.event.AfterDroneEnhanced;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.DroneEvent;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support for resizing WebDriver windows to value defined in capabilities via {@code dimensions}.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class WindowResizer {

    public static final Pattern DIMENSIONS_PATTERN = Pattern.compile("([0-9]+)x([0-9]+)");
    private static final Logger log = Logger.getLogger(WindowResizer.class.getName());
    @Inject
    Instance<DroneContext> droneContext;

    public void resizeBrowserWindow(@Observes AfterDroneInstantiated event) {
        resizeWindow(event);
    }

    public void resizeBrowserWindow(@Observes AfterDroneEnhanced event) {
        resizeWindow(event);
    }

    private void resizeWindow(DroneEvent event) {
        DronePoint<?> dronePoint = event.getDronePoint();

        if (!dronePoint.conformsTo(WebDriver.class)) {
            // This Drone is not instance of WebDriver, we will not resize the window
            return;
        }
        DroneContext context = droneContext.get();

        WebDriver driver = context.get(dronePoint).getInstanceAs(WebDriver.class);

        // let's get browser configuration
        Validate.stateNotNull(context, "DroneContext must not be null");
        WebDriverConfiguration configuration = context.get(dronePoint).getConfigurationAs(WebDriverConfiguration.class);
        Validate.stateNotNull(configuration, "WebDriver configuration must not be null");

        String browser = configuration.getBrowser().toLowerCase();
        if(!browser.equals("chromeheadless")) {
            if (configuration.getDimensions() != null) {
                String dimensions = configuration.getDimensions().toLowerCase().trim();
                Matcher m = DIMENSIONS_PATTERN.matcher(dimensions);

                if (m.matches()) {
                    int width = Integer.valueOf(m.group(1));
                    int height = Integer.valueOf(m.group(2));
                    safelyResizeWindow(driver, width, height, dronePoint);
                } else if (dimensions.equals("full") || dimensions.equals("fullscreen") || dimensions.equals("max")) {
                    safelyMaximizeWindow(driver, dronePoint);
                }
            }
        }
    }

    private void safelyResizeWindow(WebDriver driver, int width, int height, DronePoint<?> dronePoint) {
        try {
            driver.manage().window().setSize(new Dimension(width, height));
        } catch (WebDriverException | UnsupportedOperationException e) {
            log.log(Level.WARNING,"Ignoring request to resize browser window to {2}x{3} for {0}, not supported for {1}",
                    new Object[] { dronePoint, driver.getClass().getName(), width, height });
        }
    }

    private void safelyMaximizeWindow(WebDriver driver, DronePoint<?> dronePoint) {
        try {
            driver.manage().window().maximize();
        } catch (WebDriverException | UnsupportedOperationException e) {
            log.log(Level.INFO, "Drone cannot automatically maximize browser window for {0}, not supported for {1}",
                    new Object[] { dronePoint, driver.getClass().getName() });
        }
    }
}
