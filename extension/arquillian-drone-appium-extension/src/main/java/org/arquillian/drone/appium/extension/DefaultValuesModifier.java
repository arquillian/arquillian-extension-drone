/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package org.arquillian.drone.appium.extension;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.impl.DroneLifecycleManager.GlobalDroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * Modifies default WebDriver/Drone properties
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class DefaultValuesModifier {
    public static final int DEFAULT_INSTANTIATION_TIMEOUT = 240;

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Instance<ArquillianDescriptor> arquillianDescriptor;

    /**
     * Sets the default instantiationTimeoutInSeconds Drone property
     * @param event
     */
    public void modifyDefaultTimeout(@Observes(precedence = -100) BeforeSuite event) {
        if (arquillianDescriptor.get()
            .extension(GlobalDroneConfiguration.CONFIGURATION_NAME)
            .getExtensionProperties()
            .get("instantiationTimeoutInSeconds") != null) {
            return;
        }

        GlobalDroneConfiguration globalDroneConfiguration =
            droneContext.get().getGlobalDroneConfiguration(GlobalDroneConfiguration.class);
        globalDroneConfiguration.setInstantiationTimeoutInSeconds(DEFAULT_INSTANTIATION_TIMEOUT);
    }
}
