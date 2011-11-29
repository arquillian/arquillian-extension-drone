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
package org.jboss.arquillian.drone.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * Enriches test with drone instance and context path. Injects existing instance into every field annotated with {@link Drone}.
 * Handles enrichment for method arguments as well.
 *
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link DroneContext}</li>
 * <li>{@link MethodContext}</li>
 * <li>{@link ArquillianDescriptor}</li>
 * <li>{@link DroneRegistry}</li>
 * </ol>
 *
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class DroneTestEnricher implements TestEnricher {
    private static final Logger log = Logger.getLogger(DroneTestEnricher.class.getName());

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Instance<MethodContext> droneMethodContext;

    public void enrich(Object testCase) {
        List<Field> droneEnrichements = SecurityActions.getFieldsWithAnnotation(testCase.getClass(), Drone.class);
        if (!droneEnrichements.isEmpty()) {
            Validate.notNull(droneContext.get(), "Drone Test context should not be null");
            droneEnrichement(testCase, droneEnrichements);
        }

    }

    public Object[] resolve(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] resolution = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            if (SecurityActions.isAnnotationPresent(parameterAnnotations[i], Drone.class)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Resolving method " + method.getName() + " argument at position " + i);
                }

                Validate.notNull(droneMethodContext.get(), "Method context should not be null");
                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations[i]);

                Object value = droneMethodContext.get().get(parameterTypes[i], qualifier);
                Validate.notNull(value, "Retrieved a null from context, which is not a valid Drone browser object");

                resolution[i] = value;
            }
        }

        return resolution;
    }

    private void droneEnrichement(Object testCase, List<Field> fields) {
        try {
            for (Field f : fields) {
                // omit setting if already set
                if (f.get(testCase) != null) {
                    return;
                }

                Class<?> typeClass = f.getType();
                Class<? extends Annotation> qualifier = SecurityActions.getQualifier(f);

                Object value = droneContext.get().get(typeClass, qualifier);
                Validate.notNull(value, "Retrieved a null from context, which is not a valid Drone browser object");

                f.set(testCase, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not enrich test with Drone members", e);
        }
    }
}
