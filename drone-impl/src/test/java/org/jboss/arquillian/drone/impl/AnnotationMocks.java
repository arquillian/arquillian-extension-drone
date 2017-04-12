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
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.api.annotation.Drone;

public class AnnotationMocks {
    public static final String DEPLOYMENT_1 = "deployment_1";
    public static final String DEPLOYMENT_2 = "deployment_2";
    @Drone
    private static Object drone;
    @Default
    private static Object aDefault;
    @Different
    private static Object different;
    @MethodArgumentOne
    private static Object methodArgumentOne;
    @OperateOnDeployment(DEPLOYMENT_1)
    private static Object operateOnDeployment1;
    @OperateOnDeployment(DEPLOYMENT_2)
    private static Object operateOnDeployment2;

    private AnnotationMocks() {
    }

    public static Drone drone() {
        return getFieldAnnotation(Drone.class, "drone");
    }

    public static Default defaultQualifier() {
        return getFieldAnnotation(Default.class, "aDefault");
    }

    public static Different differentQualifier() {
        return getFieldAnnotation(Different.class, "different");
    }

    public static MethodArgumentOne methodArgumentOneQualifier() {
        return getFieldAnnotation(MethodArgumentOne.class, "methodArgumentOne");
    }

    public static OperateOnDeployment operateOnDeployment1() {
        return getFieldAnnotation(OperateOnDeployment.class, "operateOnDeployment1");
    }

    public static OperateOnDeployment operateOnDeployment2() {
        return getFieldAnnotation(OperateOnDeployment.class, "operateOnDeployment2");
    }

    private static <T> T getFieldAnnotation(Class<T> annotationClass, String fieldName) {
        try {
            return annotationClass.cast(AnnotationMocks.class.getDeclaredField(fieldName).getAnnotations()[0]);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
