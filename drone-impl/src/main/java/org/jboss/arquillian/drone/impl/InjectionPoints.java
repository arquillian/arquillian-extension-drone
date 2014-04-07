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

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.api.annotation.lifecycle.ClassLifecycle;
import org.jboss.arquillian.drone.api.annotation.lifecycle.MethodLifecycle;
import org.jboss.arquillian.drone.spi.DronePoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InjectionPoints {

    private InjectionPoints() {
    }

    static Set<DronePoint<?>> allInClass(Class<?> cls) {
        List<DronePoint<?>> dronePoints = new ArrayList<DronePoint<?>>();

        dronePoints.addAll(fieldsInClass(cls).values());
        for (DronePoint<?>[] methodDronePoints : parametersInClass(cls).values()) {
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

    static Map<Field, DronePoint<?>> fieldsInClass(Class<?> cls) {
        Map<Field, DronePoint<?>> injectionPoints = new HashMap<Field, DronePoint<?>>();
        List<Field> fields = SecurityActions.getFieldsWithAnnotation(cls, Drone.class);

        for (Field field : fields) {
            DronePoint<?> dronePoint = resolveInjectionPoint(field);

            injectionPoints.put(field, dronePoint);
        }

        return injectionPoints;
    }

    static Map<Method, DronePoint<?>[]> parametersInClass(Class<?> cls) {
        Map<Method, DronePoint<?>[]> mergedInjectionPoints = new HashMap<Method, DronePoint<?>[]>();
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            DronePoint<?>[] dronePoints = parametersInMethod(method);

            mergedInjectionPoints.put(method, dronePoints);
        }

        return mergedInjectionPoints;

    }

    static DronePoint<?>[] parametersInMethod(Method method) {
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

            DronePoint<?> dronePoint = resolveInjectionPoint(droneType, parameterAnnotations);

            dronePoints[i] = dronePoint;
        }

        return dronePoints;
    }

    static DronePoint<?> resolveInjectionPoint(Field field) {
        Class<?> droneType = field.getType();
        Class<? extends Annotation> qualifier = SecurityActions.getQualifier(field);
        Class<? extends Annotation> scopeAnnotation = SecurityActions.getScope(field);
        OperateOnDeployment operateOnDeployment = SecurityActions.getAnnotation(field, OperateOnDeployment.class);

        return createInjectionPoint(droneType, qualifier, scopeAnnotation, DronePoint.Lifecycle.CLASS,
                operateOnDeployment);
    }

    static <T> DronePoint<T> resolveInjectionPoint(Class<T> droneType, Annotation[] parameterAnnotations) {
        Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations);
        Class<? extends Annotation> scopeAnnotation = SecurityActions.getScope(parameterAnnotations);
        OperateOnDeployment operateOnDeployment = SecurityActions.findAnnotation(parameterAnnotations,
                OperateOnDeployment.class);

        return createInjectionPoint(droneType, qualifier, scopeAnnotation, DronePoint.Lifecycle.METHOD,
                operateOnDeployment);
    }

    // We can't instantiate class with wildcard generic parameter directly, so we delegate it through parameter <T>
    // FIXME it's ugly to have so many parameters
    static <T> DronePoint<T> createInjectionPoint(Class<T> droneType,
                                                      Class<? extends Annotation> qualifier,
                                                      Class<? extends Annotation> scopeAnnotation,
                                                      DronePoint.Lifecycle defaultLifecycle,
                                                      OperateOnDeployment operateOnDeployment) {
        DronePoint.Lifecycle lifecycle = scopeForAnnotation(scopeAnnotation, operateOnDeployment, defaultLifecycle);
        if (lifecycle == DronePoint.Lifecycle.DEPLOYMENT) {
            String deployment = operateOnDeployment.value();
            return new DeploymentLifecycleDronePointImpl<T>(droneType, qualifier, lifecycle, deployment);
        } else {
            return new DronePointImpl<T>(droneType, qualifier, lifecycle);
        }
    }

    static DronePoint.Lifecycle scopeForAnnotation(Class<? extends Annotation> annotation,
                                                   OperateOnDeployment deployment, DronePoint.Lifecycle defaultLifecycle) {
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
