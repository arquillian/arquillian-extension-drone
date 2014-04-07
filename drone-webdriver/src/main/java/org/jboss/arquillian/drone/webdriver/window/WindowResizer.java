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

    private static final Logger log = Logger.getLogger(WindowResizer.class.getName());

    static final Pattern DIMENSIONS_PATTERN = Pattern.compile("([0-9]+)x([0-9]+)");

    @Inject
    Instance<DroneContext> droneContext;

    public void resizeBrowserWindow(@Observes AfterDroneInstantiated event) {
        // get content of event
        Object drone = event.getDrone();
        DronePoint<?> dronePoint = event.getDronePoint();

        resizeWindow(drone, dronePoint);
    }

    public void resizeBrowserWindow(@Observes AfterDroneEnhanced event) {
        // get content of event
        Object drone = event.getDrone();
        DronePoint<?> dronePoint = event.getDronePoint();

        resizeWindow(drone, dronePoint);
    }

    private void resizeWindow(Object drone, DronePoint<?> dronePoint) {
        if (!WebDriver.class.isAssignableFrom(drone.getClass())) {
            return;
        }

        DroneContext context = droneContext.get();

        WebDriver driver = (WebDriver) drone;


        // let's get browser configuration
        Validate.stateNotNull(context, "DroneContext must not be null");
        WebDriverConfiguration configuration = context.get(dronePoint).getConfigurationAs(WebDriverConfiguration.class);
        Validate.stateNotNull(configuration, "WebDriver configuration must not be null");

        String dimensions = configuration.getDimensions();

        if (dimensions != null) {
            Matcher m = DIMENSIONS_PATTERN.matcher(dimensions);
            if (m.matches()) {
                int width = Integer.valueOf(m.group(1));
                int height = Integer.valueOf(m.group(2));
                safelyResizeWindow(driver, width, height, dronePoint);
            }
        }
    }

    private void safelyResizeWindow(WebDriver driver, int width, int height, DronePoint<?> dronePoint) {
        try {
            driver.manage().window().setSize(new Dimension(width, height));
        } catch (WebDriverException e) {
            logRequestIgnored(driver, width, height, dronePoint);
        } catch (UnsupportedOperationException e) {
            logRequestIgnored(driver, width, height, dronePoint);
        }
    }

    private void logRequestIgnored(WebDriver driver, int width, int height, DronePoint<?> dronePoint) {
        log.log(Level.WARNING, "Ignoring request to resize browser window to {3}x{4} for {0} @{1}, " +
                        "not supported for {2}",
                new Object[] { dronePoint.getDroneType().getSimpleName(),
                        dronePoint.getQualifier().getSimpleName(), driver.getClass().getName(), width,
                        height }
        );
    }
}
