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
package org.jboss.arquillian.drone.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.api.annotation.lifecycle.ClassLifecycle;
import org.jboss.arquillian.drone.api.annotation.lifecycle.MethodLifecycle;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.deployment.DeploymentNameKey;

// FIXME make this class not static and do the API a better way
final class InjectionPoints {

    private InjectionPoints() {
    }

    static Set<DronePoint<?>> allInClass(DroneContext context, Class<?> cls) {
        List<DronePoint<?>> dronePoints = new ArrayList<DronePoint<?>>();

        dronePoints.addAll(fieldsInClass(context, cls).values());
        for (DronePoint<?>[] methodDronePoints : parametersInClass(context, cls).values()) {
            for (DronePoint<?> dronePoint : methodDronePoints) {
                if (dronePoint == null) {
                    continue;
                }
                dronePoints.add(dronePoint);
            }
        }

        // We want no duplicates
        return new HashSet<DronePoint<?>>(dronePoints);
    }

    static Map<Field, DronePoint<?>> fieldsInClass(DroneContext context, Class<?> cls) {
        Map<Field, DronePoint<?>> injectionPoints = new HashMap<Field, DronePoint<?>>();
        List<Field> fields = SecurityActions.getFieldsWithAnnotation(cls, Drone.class);

        for (Field field : fields) {
            DronePoint<?> dronePoint = resolveInjectionPoint(context, field);

            injectionPoints.put(field, dronePoint);
        }

        return injectionPoints;
    }

    static Map<Method, DronePoint<?>[]> parametersInClass(DroneContext context, Class<?> cls) {
        Map<Method, DronePoint<?>[]> mergedInjectionPoints = new HashMap<Method, DronePoint<?>[]>();
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            DronePoint<?>[] dronePoints = parametersInMethod(context, method);

            mergedInjectionPoints.put(method, dronePoints);
        }

        return mergedInjectionPoints;
    }

    static DronePoint<?>[] parametersInMethod(DroneContext context, Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        Map<Integer, Annotation[]> droneParameters = SecurityActions.getParametersWithAnnotation(method, Drone.class);
        DronePoint<?>[] dronePoints = new DronePoint<?>[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (!droneParameters.containsKey(i)) {
                dronePoints[i] = null;
                continue;
            }

            Annotation[] parameterAnnotations = droneParameters.get(i);

            Class<?> droneType = parameters[i];

            DronePoint<?> dronePoint = resolveInjectionPoint(context, droneType, parameterAnnotations);

            dronePoints[i] = dronePoint;
        }

        return dronePoints;
    }

    static DronePoint<?> resolveInjectionPoint(DroneContext context, Field field) {

        Class<?> droneType = field.getType();
        Annotation[] annotations = SecurityActions.getAnnotations(field);

        return createInjectionPoint(context, droneType, annotations, DronePoint.Lifecycle.CLASS);
    }

    static <T> DronePoint<T> resolveInjectionPoint(DroneContext context, Class<T> droneType,
        Annotation[] parameterAnnotations) {
        return createInjectionPoint(context, droneType, parameterAnnotations, DronePoint.Lifecycle.METHOD);
    }

    // We can't instantiate class with wildcard generic parameter directly, so we delegate it through parameter <T>
    static <T> DronePoint<T> createInjectionPoint(DroneContext context, Class<T> droneType,
        Annotation[] annotations,
        DronePoint.Lifecycle defaultLifecycle) {
        Class<? extends Annotation> scopeAnnotation = SecurityActions.getScope(annotations);
        OperateOnDeployment operateOnDeployment = SecurityActions.findAnnotation(annotations,
            OperateOnDeployment.class);

        DronePoint.Lifecycle lifecycle = scopeForAnnotation(scopeAnnotation, operateOnDeployment, defaultLifecycle);

        DronePoint<T> dronePoint = new DronePointImpl<T>(droneType, lifecycle, annotations);
        // We register the drone point into context immediately
        context.get(dronePoint);
        if (lifecycle == DronePoint.Lifecycle.DEPLOYMENT) {
            String deployment = operateOnDeployment.value();
            context.get(dronePoint).setMetadata(DeploymentNameKey.class, deployment);
        }
        return dronePoint;
    }

    static DronePoint.Lifecycle scopeForAnnotation(Class<? extends Annotation> annotation,
        OperateOnDeployment deployment,
        DronePoint.Lifecycle defaultLifecycle) {
        if (annotation == ClassLifecycle.class) {
            return DronePoint.Lifecycle.CLASS;
        } else if (annotation == MethodLifecycle.class) {
            return DronePoint.Lifecycle.METHOD;
        } else {
            if (deployment != null) {
                return DronePoint.Lifecycle.DEPLOYMENT;
            } else {
                return defaultLifecycle;
            }
        }
    }
}
