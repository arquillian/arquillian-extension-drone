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

import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class DronePointImplTest {

    @Test
    public void testClassScopeEquality() {
        // given
        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.CLASS,
            AnnotationMocks.drone());
        DronePoint<MockDrone> dronePoint1 = new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.CLASS,
            AnnotationMocks.drone());
        // then
        Assert.assertEquals("Injection points are equal", dronePoint, dronePoint1);
    }

    @Test
    public void testMethodScopeEquality() {
        // given
        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.METHOD,
            AnnotationMocks.drone());
        DronePoint<MockDrone> dronePoint1 = new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.METHOD,
            AnnotationMocks.drone());
        // then
        Assert.assertEquals("Injection points are equal", dronePoint, dronePoint1);
    }

    @Test
    public void testDeploymentScopeEquality() {
        // given
        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class,
            DronePoint.Lifecycle.DEPLOYMENT,
            AnnotationMocks.drone());
        DronePoint<MockDrone> dronePoint1 = new DronePointImpl<MockDrone>(MockDrone.class,
            DronePoint.Lifecycle.DEPLOYMENT, AnnotationMocks.drone());
        // then
        Assert.assertEquals("Injection points are equal", dronePoint, dronePoint1);
    }

    @Test
    public void testAnnotationOrderDoesNotMatter() {
        // given
        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class, null,
            AnnotationMocks.drone(), AnnotationMocks.defaultQualifier(), AnnotationMocks.differentQualifier());
        DronePoint<MockDrone> dronePoint1 = new DronePointImpl<MockDrone>(MockDrone.class, null,
            AnnotationMocks.differentQualifier(), AnnotationMocks.drone(), AnnotationMocks.defaultQualifier());

        // then
        assertThat(dronePoint, is(dronePoint1));
    }

    @Test
    public void testVariousDroneTypesInequality() {
        // given
        DronePoint<String> stringDronePoint = new DronePointImpl<String>(String.class, null);
        DronePoint<MockDrone> mockDronePoint = new DronePointImpl<MockDrone>(MockDrone.class, null);

        // then
        Assert.assertNotSame("Injection points aren't equal", stringDronePoint, mockDronePoint);
    }

    @Test
    public void testVariousQualifiersInequality() {
        // given
        DronePoint<MockDrone> defaultDronePoint = new DronePointImpl<MockDrone>(MockDrone.class, null,
            AnnotationMocks.drone(), AnnotationMocks.defaultQualifier());
        DronePoint<MockDrone> differentDronePoint = new DronePointImpl<MockDrone>(MockDrone.class, null,
            AnnotationMocks.drone(), AnnotationMocks.differentQualifier());

        // then
        Assert.assertNotSame("Injection points aren't equal", defaultDronePoint, differentDronePoint);
    }

    @Test
    public void testVariousScopeInequality() {
        // given
        DronePoint<MockDrone> classScopePoint = new DronePointImpl<MockDrone>(MockDrone.class,
            DronePoint.Lifecycle.CLASS, AnnotationMocks.drone());
        DronePoint<MockDrone> methodScopePoint = new DronePointImpl<MockDrone>(MockDrone.class,
            DronePoint.Lifecycle.METHOD, AnnotationMocks.drone());
        DronePoint<MockDrone> deploymentScopePoint = new DronePointImpl<MockDrone>(MockDrone.class,
            DronePoint.Lifecycle.DEPLOYMENT, AnnotationMocks.drone());

        // then
        Assert.assertNotSame("Injection points aren't equal", classScopePoint, methodScopePoint);
        Assert.assertNotSame("Injection points aren't equal", classScopePoint, deploymentScopePoint);
        Assert.assertNotSame("Injection points aren't equal", methodScopePoint, deploymentScopePoint);
    }

    @Test
    public void testVariousDeploymentsInequaility() {
        // given
        DronePoint<MockDrone> dronePoint = new DronePointImpl<MockDrone>(MockDrone.class, null,
            AnnotationMocks.drone(), AnnotationMocks.operateOnDeployment1());
        DronePoint<MockDrone> dronePoint1 = new DronePointImpl<MockDrone>(MockDrone.class, null,
            AnnotationMocks.drone(), AnnotationMocks.operateOnDeployment2());

        // then
        assertThat(dronePoint, is(not(dronePoint1)));
    }
}
