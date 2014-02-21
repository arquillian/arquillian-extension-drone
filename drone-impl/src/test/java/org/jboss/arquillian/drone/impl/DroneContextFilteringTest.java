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
import org.jboss.arquillian.drone.spi.InjectionPoint;
import org.jboss.arquillian.drone.spi.filter.DeploymentFilter;
import org.jboss.arquillian.drone.spi.filter.QualifierFilter;
import org.jboss.arquillian.drone.spi.filter.ScopeFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DroneContextFilteringTest {

    private static final String DEPLOYMENT_DEFAULT = "deployment_default";
    private static final String DEPLOYMENT_DIFFERENT = "deployment_different";

    DroneContext context;

    InjectionPoint<MockDrone> defaultClassInjectionPoint;
    InjectionPoint<MockDrone> defaultMethodInjectionPoint;
    InjectionPoint<MockDrone> defaultDeploymentInjectionPoint;
    InjectionPoint<MockDrone> differentClassInjectionPoint;
    InjectionPoint<MockDrone> differentMethodInjectionPoint;
    InjectionPoint<MockDrone> differentDeploymentInjectionPoint;

    @Before
    public void setup() {
        context = new DroneContextImpl();

        defaultClassInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class, Default.class,
                InjectionPoint.Scope.CLASS);
        context.storeDroneConfiguration(defaultClassInjectionPoint, null);
        defaultMethodInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class, Default.class,
                InjectionPoint.Scope.METHOD);
        context.storeDroneConfiguration(defaultMethodInjectionPoint, null);
        defaultDeploymentInjectionPoint = new DeploymentScopedInjectionPointImpl<MockDrone>(MockDrone.class,
                Default.class, InjectionPoint.Scope.DEPLOYMENT, DEPLOYMENT_DEFAULT);
        context.storeDroneConfiguration(defaultDeploymentInjectionPoint, null);

        differentClassInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class, Different.class,
                InjectionPoint.Scope.CLASS);
        context.storeDroneConfiguration(differentClassInjectionPoint, null);
        differentMethodInjectionPoint = new InjectionPointImpl<MockDrone>(MockDrone.class, Different.class,
                InjectionPoint.Scope.METHOD);
        context.storeDroneConfiguration(differentMethodInjectionPoint, null);
        differentDeploymentInjectionPoint = new DeploymentScopedInjectionPointImpl<MockDrone>(MockDrone.class,
                Different.class, InjectionPoint.Scope.DEPLOYMENT, DEPLOYMENT_DIFFERENT);
        context.storeDroneConfiguration(differentDeploymentInjectionPoint, null);
    }

    @Test
    public void testQualifierFiltering() {
        Assert.assertEquals(3, context.find(MockDrone.class, new QualifierFilter(Default.class)).size());
        Assert.assertEquals(3, context.find(MockDrone.class, new QualifierFilter(Different.class)).size());
    }

    @Test
    public void testScopeFiltering() {
        Assert.assertEquals(2, context.find(MockDrone.class, new ScopeFilter(InjectionPoint.Scope.CLASS)).size());
        Assert.assertEquals(2, context.find(MockDrone.class, new ScopeFilter(InjectionPoint.Scope.METHOD)).size());
        Assert.assertEquals(2, context.find(MockDrone.class, new ScopeFilter(InjectionPoint.Scope.DEPLOYMENT)).size());
    }

    @Test
    public void testDeploymentFiltering() {
        Assert.assertEquals(2, context.find(MockDrone.class, new DeploymentFilter(".*")).size());
        Assert.assertEquals(2, context.find(MockDrone.class, new DeploymentFilter()).size());
        Assert.assertEquals(2, context.find(MockDrone.class, new DeploymentFilter("deployment_.*")).size());
        Assert.assertEquals(0, context.find(MockDrone.class, new DeploymentFilter("invalid_.*")).size());
        Assert.assertEquals(defaultDeploymentInjectionPoint, context.findSingle(MockDrone.class, new DeploymentFilter(DEPLOYMENT_DEFAULT)));
        Assert.assertEquals(differentDeploymentInjectionPoint, context.findSingle(MockDrone.class, new DeploymentFilter(DEPLOYMENT_DIFFERENT)));
    }

    @Test
    public void testCombinedFiltering() {
        Assert.assertEquals(defaultClassInjectionPoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Default.class),
                        new ScopeFilter(InjectionPoint.Scope.CLASS)));
        Assert.assertEquals(defaultMethodInjectionPoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Default.class),
                        new ScopeFilter(InjectionPoint.Scope.METHOD)));
        Assert.assertEquals(defaultDeploymentInjectionPoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Default.class),
                        new ScopeFilter(InjectionPoint.Scope.DEPLOYMENT)));
        Assert.assertEquals(differentClassInjectionPoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Different.class),
                        new ScopeFilter(InjectionPoint.Scope.CLASS)));
        Assert.assertEquals(differentMethodInjectionPoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Different.class),
                        new ScopeFilter(InjectionPoint.Scope.METHOD)));
        Assert.assertEquals(differentDeploymentInjectionPoint,
                context.findSingle(MockDrone.class, new QualifierFilter(Different.class),
                        new ScopeFilter(InjectionPoint.Scope.DEPLOYMENT)));
    }

}
