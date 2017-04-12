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
package org.jboss.arquillian.drone.spi.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointFilter;
import org.jboss.arquillian.drone.spi.deployment.DeploymentNameKey;

/**
 * Filter for finding deployment injection points.
 */
public class DeploymentFilter implements DronePointFilter<Object> {

    private final Pattern pattern;

    /**
     * Creates a deployment filter which will match all the deployment injection points.
     */
    public DeploymentFilter() {
        this(".*");
    }

    /**
     * Creates a deployment filter which will match deployment injection points, whose name matches the regex.
     */
    public DeploymentFilter(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean accepts(DroneContext context, DronePoint<?> dronePoint) {
        String deploymentName = context.get(dronePoint).getMetadata(DeploymentNameKey.class);
        if (deploymentName == null) {
            return false;
        }

        Matcher matcher = pattern.matcher(deploymentName);
        return matcher.matches();
    }
}
