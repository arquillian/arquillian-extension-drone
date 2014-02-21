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

import org.jboss.arquillian.drone.spi.DeploymentScopedInjectionPoint;

import java.lang.annotation.Annotation;

public class DeploymentScopedInjectionPointImpl<DRONE> extends InjectionPointImpl<DRONE> implements
        DeploymentScopedInjectionPoint<DRONE> {

    private final String deployment;

    public DeploymentScopedInjectionPointImpl(Class<DRONE> droneClass, Class<? extends Annotation> qualifier, Scope
            scope, String deployment) {
        super(droneClass, qualifier, scope);

        this.deployment = deployment;
    }


    @Override
    public String getDeployment() {
        return deployment;
    }
}
