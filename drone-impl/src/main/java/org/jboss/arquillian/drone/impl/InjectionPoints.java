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
import org.jboss.arquillian.drone.api.annotation.scope.ClassScope;
import org.jboss.arquillian.drone.api.annotation.scope.DeploymentScope;
import org.jboss.arquillian.drone.api.annotation.scope.MethodScope;
import org.jboss.arquillian.drone.spi.InjectionPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InjectionPoints {

    private InjectionPoints() { }

    static List<InjectionPoint<?>> allInClass(Class<?> cls) {
        List<InjectionPoint<?>> injectionPoints = new ArrayList<InjectionPoint<?>>();

        injectionPoints.addAll(fieldsInClass(cls).values());
        for (InjectionPoint<?>[] methodInjectionPoints : parametersInClass(cls).values()) {
            for(InjectionPoint<?> injectionPoint : methodInjectionPoints) {
                if(injectionPoint == null) {
                    continue;
                }
                injectionPoints.add(injectionPoint);
            }
        }

        return injectionPoints;
    }

    static Map<Field, InjectionPoint<?>> fieldsInClass(Class<?> cls) {
        Map<Field, InjectionPoint<?>> injectionPoints = new HashMap<Field, InjectionPoint<?>>();
        List<Field> fields = SecurityActions.getFieldsWithAnnotation(cls, Drone.class);

        for (Field field : fields) {
            InjectionPoint<?> injectionPoint = resolveInjectionPoint(field);

            injectionPoints.put(field, injectionPoint);
        }

        return injectionPoints;
    }

    static Map<Method, InjectionPoint<?>[]> parametersInClass(Class<?> cls) {
        Map<Method, InjectionPoint<?>[]> mergedInjectionPoints = new HashMap<Method, InjectionPoint<?>[]>();
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            InjectionPoint<?>[] injectionPoints = parametersInMethod(method);

            mergedInjectionPoints.put(method, injectionPoints);
        }

        return mergedInjectionPoints;

    }

    static InjectionPoint<?>[] parametersInMethod(Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        Map<Integer, Annotation[]> droneParameters = SecurityActions.getParametersWithAnnotation(method, Drone.class);
        InjectionPoint<?>[] injectionPoints = new InjectionPoint<?>[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (!droneParameters.containsKey(i)) {
                injectionPoints[i] = null;
                continue;
            }

            Annotation[] parameterAnnotations = droneParameters.get(i);

            Class<?> droneType = parameters[i];

            InjectionPoint<?> injectionPoint = resolveInjectionPoint(droneType, parameterAnnotations);

            injectionPoints[i] = injectionPoint;
        }

        return injectionPoints;
    }

    static InjectionPoint<?> resolveInjectionPoint(Field field) {
        Class<?> droneType = field.getType();
        Class<? extends Annotation> qualifier = SecurityActions.getQualifier(field);
        Class<? extends Annotation> scopeAnnotation = SecurityActions.getScope(field);
        OperateOnDeployment operateOnDeployment = SecurityActions.getAnnotation(field, OperateOnDeployment.class);

        return createInjectionPoint(droneType, qualifier, scopeAnnotation, InjectionPoint.Scope.CLASS,
                operateOnDeployment);
    }

    static <T> InjectionPoint<T> resolveInjectionPoint(Class<T> droneType, Annotation[] parameterAnnotations) {
        Class<? extends Annotation> qualifier = SecurityActions.getQualifier(parameterAnnotations);
        Class<? extends Annotation> scopeAnnotation = SecurityActions.getScope(parameterAnnotations);
        OperateOnDeployment operateOnDeployment = SecurityActions.findAnnotation(parameterAnnotations,
                OperateOnDeployment.class);

        return createInjectionPoint(droneType, qualifier, scopeAnnotation, InjectionPoint.Scope.METHOD,
                operateOnDeployment);
    }

    // We can't instantiate class with wildcard generic parameter directly, so we delegate it through parameter <T>
    // FIXME it's ugly to have so many parameters
    static <T> InjectionPoint<T> createInjectionPoint(Class<T> droneType,
                                                      Class<? extends Annotation> qualifier,
                                                      Class<? extends Annotation> scopeAnnotation,
                                                      InjectionPoint.Scope defaultScope,
                                                      OperateOnDeployment operateOnDeployment) {
        InjectionPoint.Scope scope = scopeForAnnotation(scopeAnnotation, operateOnDeployment, defaultScope);
        if (scope == InjectionPoint.Scope.DEPLOYMENT) {
            String deployment = operateOnDeployment.value();
            return new DeploymentScopedInjectionPointImpl<T>(droneType, qualifier, scope, deployment);
        } else {
            return new InjectionPointImpl<T>(droneType, qualifier, scope);
        }
    }

    static InjectionPoint.Scope scopeForAnnotation(Class<? extends Annotation> annotation,
                                                   OperateOnDeployment deployment, InjectionPoint.Scope defaultScope) {
        if (annotation == ClassScope.class) {
            return InjectionPoint.Scope.CLASS;
        } else if (annotation == MethodScope.class) {
            return InjectionPoint.Scope.METHOD;
        } else if (annotation == DeploymentScope.class) {
            if (deployment == null) {
                throw new IllegalStateException("Drone marked as Deployment scoped, " +
                        "but no @OperateOnDeployment found!");
            }
            return InjectionPoint.Scope.DEPLOYMENT;
        } else {
            if (deployment != null) {
                return InjectionPoint.Scope.DEPLOYMENT;
            } else {
                return defaultScope;
            }
        }
    }

}
