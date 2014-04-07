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
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.filter.DeploymentFilter;
import org.jboss.arquillian.drone.spi.filter.LifecycleFilter;
import org.jboss.arquillian.drone.spi.filter.QualifierFilter;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DroneContextFilteringTest extends AbstractTestTestBase {

    private static final String DEPLOYMENT_DEFAULT = "deployment_default";
    private static final String DEPLOYMENT_DIFFERENT = "deployment_different";

    DroneContext context;

    DronePoint<MockDrone> defaultClassDronePoint;
    DronePoint<MockDrone> defaultMethodDronePoint;
    DronePoint<MockDrone> defaultDeploymentDronePoint;
    DronePoint<MockDrone> differentClassDronePoint;
    DronePoint<MockDrone> differentMethodDronePoint;
    DronePoint<MockDrone> differentDeploymentDronePoint;

    @Before
    public void setup() {
        context = new DroneContextImpl();
        getManager().inject(context);


        defaultClassDronePoint = new DronePointImpl<MockDrone>(MockDrone.class, Default.class,
                DronePoint.Lifecycle.CLASS);
        context.get(defaultClassDronePoint);

        defaultMethodDronePoint = new DronePointImpl<MockDrone>(MockDrone.class, Default.class,
                DronePoint.Lifecycle.METHOD);
        context.get(defaultMethodDronePoint);

        defaultDeploymentDronePoint = new DeploymentLifecycleDronePointImpl<MockDrone>(MockDrone.class,
                Default.class, DronePoint.Lifecycle.DEPLOYMENT, DEPLOYMENT_DEFAULT);
        context.get(defaultDeploymentDronePoint);

        differentClassDronePoint = new DronePointImpl<MockDrone>(MockDrone.class, Different.class,
                DronePoint.Lifecycle.CLASS);
        context.get(differentClassDronePoint);

        differentMethodDronePoint = new DronePointImpl<MockDrone>(MockDrone.class, Different.class,
                DronePoint.Lifecycle.METHOD);
        context.get(differentMethodDronePoint);

        differentDeploymentDronePoint = new DeploymentLifecycleDronePointImpl<MockDrone>(MockDrone.class,
                Different.class, DronePoint.Lifecycle.DEPLOYMENT, DEPLOYMENT_DIFFERENT);
        context.get(differentDeploymentDronePoint);
    }

    @Test
    public void testQualifierFiltering() {
        Assert.assertEquals(3, context.find(MockDrone.class, new QualifierFilter(Default.class)).size());
        Assert.assertEquals(3, context.find(MockDrone.class, new QualifierFilter(Different.class)).size());
    }

    @Test
    public void testScopeFiltering() {
        Assert.assertEquals(2, context.find(MockDrone.class, new LifecycleFilter(DronePoint.Lifecycle.CLASS)).size());
        Assert.assertEquals(2, context.find(MockDrone.class, new LifecycleFilter(DronePoint.Lifecycle.METHOD)).size());
        Assert.assertEquals(2, context.find(MockDrone.class, new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT))
                .size());
    }

    @Test
    public void testDeploymentFiltering() {
        Assert.assertEquals(2, context.find(MockDrone.class, new DeploymentFilter(".*")).size());
        Assert.assertEquals(2, context.find(MockDrone.class, new DeploymentFilter()).size());
        Assert.assertEquals(2, context.find(MockDrone.class, new DeploymentFilter("deployment_.*")).size());
        Assert.assertEquals(0, context.find(MockDrone.class, new DeploymentFilter("invalid_.*")).size());
        Assert.assertEquals(defaultDeploymentDronePoint, context.findSingle(MockDrone.class,
                new DeploymentFilter(DEPLOYMENT_DEFAULT)));
        Assert.assertEquals(differentDeploymentDronePoint, context.findSingle(MockDrone.class,
                new DeploymentFilter(DEPLOYMENT_DIFFERENT)));
    }

    @Test
    public void testCombinedFiltering() {
        Assert.assertEquals(defaultClassDronePoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Default.class),
                        new LifecycleFilter(DronePoint.Lifecycle.CLASS))
        );
        Assert.assertEquals(defaultMethodDronePoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Default.class),
                        new LifecycleFilter(DronePoint.Lifecycle.METHOD))
        );
        Assert.assertEquals(defaultDeploymentDronePoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Default.class),
                        new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT))
        );
        Assert.assertEquals(differentClassDronePoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Different.class),
                        new LifecycleFilter(DronePoint.Lifecycle.CLASS))
        );
        Assert.assertEquals(differentMethodDronePoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Different.class),
                        new LifecycleFilter(DronePoint.Lifecycle.METHOD))
        );
        Assert.assertEquals(differentDeploymentDronePoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Different.class),
                        new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT))
        );
    }

}
