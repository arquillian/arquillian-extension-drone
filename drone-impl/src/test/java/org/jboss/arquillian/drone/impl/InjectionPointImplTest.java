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

import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.junit.Assert;
import org.junit.Test;

public class InjectionPointImplTest {

    @Test
    public void testClassScopeEquality() {
        // given
        InjectionPoint<MockDrone> injectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Scope.CLASS);
        InjectionPoint<MockDrone> injectionPoint1 = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Scope.CLASS);
        // then
        Assert.assertEquals("Injection points are equal", injectionPoint, injectionPoint1);
    }

    @Test
    public void testMethodScopeEquality() {
        // given
        InjectionPoint<MockDrone> injectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Scope.METHOD);
        InjectionPoint<MockDrone> injectionPoint1 = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Scope.METHOD);
        // then
        Assert.assertEquals("Injection points are equal", injectionPoint, injectionPoint1);
    }

    @Test
    public void testDeploymentScopeEquality() {
        // given
        InjectionPoint<MockDrone> injectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Scope.DEPLOYMENT);
        InjectionPoint<MockDrone> injectionPoint1 = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Scope.DEPLOYMENT);
        // then
        Assert.assertEquals("Injection points are equal", injectionPoint, injectionPoint1);
    }

    @Test
    public void testVariousDroneTypesInequality() {
        // given
        InjectionPoint<String> stringInjectionPoint = new InjectionPointImpl<String>(String.class, null, null);
        InjectionPoint<MockDrone> mockInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class, null, null);

        // then
        Assert.assertNotSame("Injection points aren't equal", stringInjectionPoint, mockInjectionPoint);
    }

    @Test
    public void testVariousQualifiersInequality() {
        // given
        InjectionPoint<MockDrone> defaultInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, null);
        InjectionPoint<MockDrone> differentInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                Different.class, null);

        // then
        Assert.assertNotSame("Injection points aren't equal", defaultInjectionPoint, differentInjectionPoint);
    }

    @Test
    public void testVariousScopeInequality() {
        // given
        InjectionPoint<MockDrone> classScopePoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                null, InjectionPoint.Scope.CLASS);
        InjectionPoint<MockDrone> methodScopePoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                null, InjectionPoint.Scope.METHOD);
        InjectionPoint<MockDrone> deploymentScopePoint = new InjectionPointImpl<MockDrone>(MockDrone.class,
                null, InjectionPoint.Scope.DEPLOYMENT);

        // then
        Assert.assertNotSame("Injection points aren't equal", classScopePoint, methodScopePoint);
        Assert.assertNotSame("Injection points aren't equal", classScopePoint, deploymentScopePoint);
        Assert.assertNotSame("Injection points aren't equal", methodScopePoint, deploymentScopePoint);
    }

}
