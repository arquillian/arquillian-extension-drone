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
import org.jboss.arquillian.drone.spi.deployment.DeploymentNameKey;
import org.jboss.arquillian.drone.spi.filter.DeploymentFilter;
import org.jboss.arquillian.drone.spi.filter.LifecycleFilter;
import org.jboss.arquillian.drone.spi.filter.QualifierFilter;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.annotation.Annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


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


        defaultClassDronePoint = createDronePoint(Default.class, DronePoint.Lifecycle.CLASS);
        context.get(defaultClassDronePoint);

        defaultMethodDronePoint = createDronePoint(Default.class, DronePoint.Lifecycle.METHOD);
        context.get(defaultMethodDronePoint);

        defaultDeploymentDronePoint = createDronePoint(Default.class, DronePoint.Lifecycle.DEPLOYMENT);
        context.get(defaultDeploymentDronePoint).setMetadata(DeploymentNameKey.class, DEPLOYMENT_DEFAULT);

        differentClassDronePoint = createDronePoint(Different.class, DronePoint.Lifecycle.CLASS);
        context.get(differentClassDronePoint).setMetadata(DeploymentNameKey.class, DEPLOYMENT_DIFFERENT);

        differentMethodDronePoint = createDronePoint(Different.class, DronePoint.Lifecycle.METHOD);
        context.get(differentMethodDronePoint);

        differentDeploymentDronePoint = createDronePoint(Different.class, DronePoint.Lifecycle.DEPLOYMENT);
        context.get(differentDeploymentDronePoint).setMetadata(DeploymentNameKey.class, DEPLOYMENT_DIFFERENT);
    }

    private DronePoint<MockDrone> createDronePoint(Class<? extends Annotation> qualifier,
                                                   DronePoint.Lifecycle lifecycle) {
        return new DronePointImpl<MockDrone>(MockDrone.class, qualifier, lifecycle);
    }

    @Test
    public void testQualifierFiltering() {
        assertThat(context.find(MockDrone.class, new QualifierFilter(Default.class)).size(), is(3));
        assertThat(context.find(MockDrone.class, new QualifierFilter(Different.class)).size(), is(3));
    }

    @Test
    public void testScopeFiltering() {
        assertThat(context.find(MockDrone.class, new LifecycleFilter(DronePoint.Lifecycle.CLASS)).size(), is(2));
        assertThat(context.find(MockDrone.class, new LifecycleFilter(DronePoint.Lifecycle.METHOD)).size(), is(2));
        assertThat(context.find(MockDrone.class, new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT)).size(), is(2));
    }

    @Test
    public void testDeploymentFiltering() {
        LifecycleFilter lifecycleFilter = new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT);

        assertThat(context.find(MockDrone.class, new DeploymentFilter(".*")).size(), is(3));
        assertThat(context.find(MockDrone.class, new DeploymentFilter(".*"), lifecycleFilter).size(), is(2));

        assertThat(context.find(MockDrone.class, new DeploymentFilter()).size(), is(3));
        assertThat(context.find(MockDrone.class, new DeploymentFilter(), lifecycleFilter).size(), is(2));

        assertThat(context.find(MockDrone.class, new DeploymentFilter("deployment_.*")).size(), is(3));
        assertThat(context.find(MockDrone.class, new DeploymentFilter("deployment_.*"), lifecycleFilter).size(), is(2));

        assertThat(context.find(MockDrone.class, new DeploymentFilter("invalid_.*")).size(), is(0));

        assertThat(context.findSingle(MockDrone.class, new DeploymentFilter(DEPLOYMENT_DEFAULT)),
                is(defaultDeploymentDronePoint));
        assertThat(context.findSingle(MockDrone.class, new DeploymentFilter(DEPLOYMENT_DIFFERENT), lifecycleFilter),
                is(differentDeploymentDronePoint));
    }

    @Test
    public void testCombinedFiltering() {

        assertThat(context.findSingle(MockDrone.class,
                        new QualifierFilter(Default.class), new LifecycleFilter(DronePoint.Lifecycle.CLASS)),
                is(defaultClassDronePoint)
        );

        assertThat(context.findSingle(MockDrone.class,
                        new QualifierFilter(Default.class), new LifecycleFilter(DronePoint.Lifecycle.METHOD)),
                is(defaultMethodDronePoint)
        );

        assertThat(context.findSingle(MockDrone.class,
                        new QualifierFilter(Default.class), new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT)),
                is(defaultDeploymentDronePoint)
        );

        assertThat(context.findSingle(MockDrone.class,
                        new QualifierFilter(Different.class), new LifecycleFilter(DronePoint.Lifecycle.CLASS)),
                is(differentClassDronePoint)
        );

        assertThat(context.findSingle(MockDrone.class,
                        new QualifierFilter(Different.class), new LifecycleFilter(DronePoint.Lifecycle.METHOD)),
                is(differentMethodDronePoint)
        );

        assertThat(context.findSingle(MockDrone.class,
                        new QualifierFilter(Different.class), new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT)),
                is(differentDeploymentDronePoint)
        );
    }

}
