package org.jboss.arquillian.drone.impl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.drone.spi.DronePoint;

/**
 * This registry keeps all {@link DronePoint}s with the {@link DronePoint.Lifecycle.DEPLOYMENT} scope and the reference
 * to the instance of the test class the DronePoint is declared in
 */
public class DeploymentDronePointsRegistry {

    private final Map<DronePoint<?>, Object> deploymentDronePoints;
    private Object testClass = null;

    public DeploymentDronePointsRegistry() {
        this.deploymentDronePoints = new HashMap<DronePoint<?>, Object>();
    }

    /**
     * Adds a {@link DronePoint} to the registry with the test class the DronePoint is declared in
     *
     * @param deploymentDronePoint
     *     a {@link DronePoint} to register
     * @param testClass
     *     a test class the {@link DronePoint} is declared in
     *
     * @return whether the addition was successful or not
     */
    public boolean addDronePoint(DronePoint<?> deploymentDronePoint, Object testClass) {
        if (deploymentDronePoint.getLifecycle() == DronePoint.Lifecycle.DEPLOYMENT) {
            deploymentDronePoints.put(deploymentDronePoint, testClass);
            return true;
        }
        return false;
    }

    /**
     * Filter for finding deployment {@link DronePoint}s tied to a deployment with the given deployment name
     *
     * @param deploymentName
     *     name of a deployment the injection points should be tied to
     *
     * @return a map of DronePoints and testClasses tied to a deployment with the given deployment name
     */
    public Map<DronePoint<?>, Object> filterDeploymentDronePoints(String deploymentName) {
        Map<DronePoint<?>, Object> matched = new HashMap<DronePoint<?>, Object>();
        for (DronePoint dronePoint : deploymentDronePoints.keySet()) {
            if (deploymentName.equals(getDeploymentName(dronePoint))) {
                matched.put(dronePoint, deploymentDronePoints.get(dronePoint));
            }
        }
        return matched;
    }

    /**
     * Returns a deployment name of a deployment the given {@link DronePoint} is tied to
     *
     * @param dronePoint
     *     a {@link DronePoint}
     *
     * @return a deployment name of a deployment the given {@link DronePoint} is tied to
     */
    private String getDeploymentName(DronePoint<?> dronePoint) {
        Annotation[] annotations = dronePoint.getAnnotations();
        OperateOnDeployment operateOnDeployment =
            SecurityActions.findAnnotation(annotations, OperateOnDeployment.class);
        return operateOnDeployment.value();
    }
}
