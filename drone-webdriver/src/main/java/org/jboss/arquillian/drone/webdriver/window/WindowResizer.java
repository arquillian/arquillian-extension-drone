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

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.InstanceOrCallableInstance;
import org.jboss.arquillian.drone.spi.event.AfterDroneEnhanced;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

/**
 * Support for resizing WebDriver windows to value defined in capabilities via {@code dimensions}.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class WindowResizer {

    private static final Logger log = Logger.getLogger(WindowResizer.class.getName());

    static final String DIMENSIONS_CAPABILITY = "dimensions";
    static final Pattern DIMENSIONS_PATTERN = Pattern.compile("([0-9]+)x([0-9]+)");

    @Inject
    Instance<DroneContext> droneContext;

    public void resizeBrowserWindow(@Observes AfterDroneInstantiated event) {
        // get content of event
        InstanceOrCallableInstance instance = event.getInstance();
        Class<?> droneType = event.getDroneType();
        Class<? extends Annotation> qualifier = event.getQualifier();
        Class<?> realInstanceClass = instance.asInstance(droneType).getClass();

        resizeWindow(instance, droneType, qualifier, realInstanceClass);

    }

    public void resizeBrowserWindow(@Observes AfterDroneEnhanced event) {

        // get content of event
        InstanceOrCallableInstance instance = event.getInstance();
        Class<?> droneType = event.getDroneType();
        Class<? extends Annotation> qualifier = event.getQualifier();
        Class<?> realInstanceClass = instance.asInstance(droneType).getClass();

        resizeWindow(instance, droneType, qualifier, realInstanceClass);

    }

    private void resizeWindow(InstanceOrCallableInstance instance, Class<?> droneType, Class<? extends Annotation> qualifier,
            Class<?> realInstanceClass) {
        if (!WebDriver.class.isAssignableFrom(realInstanceClass)) {
            return;
        }

        // some browsers, like Opera, does not support window operations right now
        Object browser = instance.asInstance(droneType);
        WebDriver driver = (WebDriver) browser;
        try {
            driver.manage().window();
        } catch (UnsupportedOperationException e) {
            log.log(Level.WARNING, "Ignoring request to resize browser window for {0} @{1}, not supported for {2}",
                    new Object[] { droneType.getSimpleName(), qualifier.getSimpleName(), realInstanceClass.getName() });
            return;
        }

        // we can't rely on browser capabilities, because they are not stored in browser instance
        // instead, get capabilities from Drone configuration, e.g. desired capabilities
        Validate.stateNotNull(droneContext.get(), "DroneContext must not be null");
        InstanceOrCallableInstance configurationInstance = droneContext.get().get(WebDriverConfiguration.class, qualifier);
        Validate.stateNotNull(configurationInstance, "WebDriver configuration must not be null");
        WebDriverConfiguration configuration = configurationInstance.asInstance(WebDriverConfiguration.class);
        Validate.stateNotNull(configuration, "WebDriver configuration must not be null");
        Capabilities capabilities = configuration.getCapabilities();
        Validate.stateNotNull(capabilities, "WebDriver capabilities must not be null");

        String dimensions = (String) capabilities.getCapability(DIMENSIONS_CAPABILITY);
        if (dimensions != null) {
            Matcher m = DIMENSIONS_PATTERN.matcher(dimensions);
            if (m.matches()) {
                int width = Integer.valueOf(m.group(1)).intValue();
                int height = Integer.valueOf(m.group(2)).intValue();
                driver.manage().window().setSize(new Dimension(width, height));
            }
        }
    }
}
