/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.impl;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * Observer listening to {@link BeforeClass} event - enriches test with drone instance and context path. Injects
 * existing instance into every static field annotated with {@link Drone}. See: ARQ-1340
 * <p>
 * This enricher is indirectly responsible for firing chain of events that transform a callable into real instance by
 * firing {@link BeforeDroneInstantiated} event.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class DroneTestEnrichBeforeClassObserver {

    @Inject
    private Instance<Injector> injector;

    public void enrich(@Observes BeforeClass event) {
        DroneTestEnricher droneTestEnricher = injector.get().inject(new DroneTestEnricher());
        droneTestEnricher.enrichTestClass(event.getTestClass().getJavaClass(), event.getTestClass(), true);
    }
}
