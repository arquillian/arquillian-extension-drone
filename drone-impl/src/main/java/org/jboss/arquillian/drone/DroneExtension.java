/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.drone.impl.DroneConfigurator;
import org.jboss.arquillian.drone.impl.DroneDestructor;
import org.jboss.arquillian.drone.impl.DroneEnhancer;
import org.jboss.arquillian.drone.impl.DroneLifecycleManager;
import org.jboss.arquillian.drone.impl.DroneRegistrar;
import org.jboss.arquillian.drone.impl.DroneTestEnrichBeforeClassObserver;
import org.jboss.arquillian.drone.impl.DroneTestEnricher;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class DroneExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(TestEnricher.class, DroneTestEnricher.class);

        builder.observer(DroneLifecycleManager.class);
        builder.observer(DroneRegistrar.class);
        builder.observer(DroneConfigurator.class);
        builder.observer(DroneEnhancer.class);
        builder.observer(DroneDestructor.class);
        builder.observer(DroneTestEnrichBeforeClassObserver.class);
    }
}
