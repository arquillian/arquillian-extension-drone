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
package org.jboss.arquillian.drone.spi.filter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointFilter;

/**
 * Filter for finding injection points by the qualifier.
 */
public class AnnotationFilter implements DronePointFilter<Object> {

    private final Set<Annotation> annotations;

    /**
     * Creates an annotation filter, which will match drone points annotated with all annotation supplies.
     */
    public AnnotationFilter(Annotation... annotations) {
        this.annotations = new HashSet<Annotation>();
        Collections.addAll(this.annotations, annotations);
    }

    @Override
    public boolean accepts(DroneContext context, DronePoint<?> dronePoint) {
        List<Annotation> droneAnnotations = Arrays.asList(dronePoint.getAnnotations());
        for (Annotation annotation : annotations) {
            if (!droneAnnotations.contains(annotation)) {
                return false;
            }
        }
        return true;
    }
}
