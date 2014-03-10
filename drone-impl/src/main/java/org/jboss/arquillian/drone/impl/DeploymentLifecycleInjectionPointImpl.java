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

import org.jboss.arquillian.drone.spi.DeploymentLifecycleInjectionPoint;

import java.lang.annotation.Annotation;

public class DeploymentLifecycleInjectionPointImpl<DRONE> extends InjectionPointImpl<DRONE> implements
        DeploymentLifecycleInjectionPoint<DRONE> {

    private final String deployment;

    public DeploymentLifecycleInjectionPointImpl(Class<DRONE> droneClass, Class<? extends Annotation> qualifier,
                                                 Lifecycle lifecycle, String deployment) {
        super(droneClass, qualifier, lifecycle);

        this.deployment = deployment;
    }

    @Override
    public String getDeploymentName() {
        return deployment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DeploymentLifecycleInjectionPointImpl that = (DeploymentLifecycleInjectionPointImpl) o;

        if (deployment != null ? !deployment.equals(that.deployment) : that.deployment != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (deployment != null ? deployment.hashCode() : 0);
        return result;
    }
}
