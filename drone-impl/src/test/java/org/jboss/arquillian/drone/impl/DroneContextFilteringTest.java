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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DroneContextFilteringTest extends AbstractTestTestBase {

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

        defaultClassDronePoint = createDefaultDronePoint(DronePoint.Lifecycle.CLASS);
        context.get(defaultClassDronePoint);

        defaultMethodDronePoint = createDefaultDronePoint(DronePoint.Lifecycle.METHOD);
        context.get(defaultMethodDronePoint);

        defaultDeploymentDronePoint = createDefaultDronePoint(DronePoint.Lifecycle.DEPLOYMENT);
        context.get(defaultDeploymentDronePoint).setMetadata(DeploymentNameKey.class, AnnotationMocks.DEPLOYMENT_1);

        differentClassDronePoint = createDifferentDronePoint(DronePoint.Lifecycle.CLASS);
        context.get(differentClassDronePoint).setMetadata(DeploymentNameKey.class, AnnotationMocks.DEPLOYMENT_2);

        differentMethodDronePoint = createDifferentDronePoint(DronePoint.Lifecycle.METHOD);
        context.get(differentMethodDronePoint);

        differentDeploymentDronePoint = createDifferentDronePoint(DronePoint.Lifecycle.DEPLOYMENT);
        context.get(differentDeploymentDronePoint).setMetadata(DeploymentNameKey.class, AnnotationMocks.DEPLOYMENT_2);
    }

    private DronePoint<MockDrone> createDefaultDronePoint(DronePoint.Lifecycle lifecycle) {
        return createDronePoint(lifecycle, AnnotationMocks.defaultQualifier());
    }

    private DronePoint<MockDrone> createDifferentDronePoint(DronePoint.Lifecycle lifecycle) {
        return createDronePoint(lifecycle, AnnotationMocks.differentQualifier());
    }

    private DronePoint<MockDrone> createDronePoint(DronePoint.Lifecycle lifecycle, Annotation... annotations) {
        return new DronePointImpl<MockDrone>(MockDrone.class, lifecycle, annotations);
    }

    @Test
    public void testQualifierFiltering() {
        assertThat(context.find(MockDrone.class).filter(new QualifierFilter(Default.class)).size(), is(3));
        assertThat(context.find(MockDrone.class).filter(new QualifierFilter(Different.class)).size(), is(3));
    }

    @Test
    public void testScopeFiltering() {
        assertThat(context.find(MockDrone.class)
            .filter(new LifecycleFilter(DronePoint.Lifecycle.CLASS)).size(), is(2));
        assertThat(context.find(MockDrone.class)
            .filter(new LifecycleFilter(DronePoint.Lifecycle.METHOD)).size(), is(2));
        assertThat(context.find(MockDrone.class)
            .filter(new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT)).size(), is(2));
    }

    @Test
    public void testDeploymentFiltering() {
        LifecycleFilter lifecycleFilter = new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT);

        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter(".*"))
            .size(), is(3));
        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter(".*"))
            .filter(lifecycleFilter)
            .size(), is(2));

        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter())
            .size(), is(3));
        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter())
            .filter(lifecycleFilter)
            .size(), is(2));

        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter("deployment_.*"))
            .size(), is(3));
        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter("deployment_.*"))
            .filter(lifecycleFilter)
            .size(), is(2));

        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter("invalid_.*"))
            .size(), is(0));

        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter(AnnotationMocks.DEPLOYMENT_1))
            .single(), is(defaultDeploymentDronePoint));
        assertThat(context.find(MockDrone.class)
            .filter(new DeploymentFilter(AnnotationMocks.DEPLOYMENT_2))
            .filter(lifecycleFilter)
            .single(), is(differentDeploymentDronePoint));
    }

    @Test
    public void testCombinedFiltering() {

        assertThat(context.find(MockDrone.class)
            .filter(new QualifierFilter(Default.class))
            .filter(new LifecycleFilter(DronePoint.Lifecycle.CLASS))
            .single(), is(defaultClassDronePoint));

        assertThat(context.find(MockDrone.class)
            .filter(new QualifierFilter(Default.class))
            .filter(new LifecycleFilter(DronePoint.Lifecycle.METHOD))
            .single(), is(defaultMethodDronePoint));

        assertThat(context.find(MockDrone.class)
            .filter(new QualifierFilter(Default.class))
            .filter(new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT))
            .single(), is(defaultDeploymentDronePoint));

        assertThat(context.find(MockDrone.class)
            .filter(new QualifierFilter(Different.class))
            .filter(new LifecycleFilter(DronePoint.Lifecycle.CLASS))
            .single(), is(differentClassDronePoint));

        assertThat(context.find(MockDrone.class)
            .filter(new QualifierFilter(Different.class))
            .filter(new LifecycleFilter(DronePoint.Lifecycle.METHOD))
            .single(), is(differentMethodDronePoint));

        assertThat(context.find(MockDrone.class)
            .filter(new QualifierFilter(Different.class))
            .filter(new LifecycleFilter(DronePoint.Lifecycle.DEPLOYMENT))
            .single(), is(differentDeploymentDronePoint));
    }
}
