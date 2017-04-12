package org.jboss.arquillian.drone.impl;

import java.lang.reflect.Method;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.drone.impl.mockdrone.MockDrone;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * A test case for testing a proper enrichment of deployment-scoped drone points.
 */
public class DeploymentTestCase extends EnricherTestCase {

    /**
     * A complex test method for testing a proper enrichment of deployment-scoped drone points used in a combination
     * with manual deployment
     *
     * @throws Exception
     *     When anything bad happens
     */
    @Test
    public void testClassWithManualDeployment() throws Exception {
        getManager().getContext(ClassContext.class).activate(ManualDeploymentClass.class);

        Object instance = new ManualDeploymentClass();
        mockTestClass(ManualDeploymentClass.class);
        getManager().getContext(TestContext.class).activate(instance);

        DroneContext context = fireAndVerifyBeforeSuiteProcess();
        fire(new BeforeClass(MethodEnrichedClass.class));

        DronePoint<MockDrone> dronePoint1 =
            new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.DEPLOYMENT,
                AnnotationMocks.drone(),
                AnnotationMocks.operateOnDeployment1());
        DronePoint<MockDrone> dronePoint2 =
            new DronePointImpl<MockDrone>(MockDrone.class, DronePoint.Lifecycle.DEPLOYMENT,
                AnnotationMocks.drone(),
                AnnotationMocks.operateOnDeployment2());

        runDeploymentTestMethod(dronePoint1, dronePoint2, false, false, false, false, instance, context,
            "testBeforeDeployment");

        runFailingTestMethod(instance, "testShouldFailDeployment1NotDeployed", MockDrone.class);

        runDeploymentTestMethod(dronePoint1, dronePoint2, false, false, true, false, instance, context,
            "testDeploy1");

        runFailingTestMethod(instance, "testShouldFailDeployment2NotDeployed", MockDrone.class);

        runDeploymentTestMethod(dronePoint1, dronePoint2, true, false, true, true, instance, context,
            "testDeploy2Deployed1", MockDrone.class);

        runDeploymentTestMethod(dronePoint1, dronePoint2, true, true, true, true, instance, context,
            "testDeployed12", MockDrone.class, MockDrone.class);

        runDeploymentTestMethod(dronePoint1, dronePoint2, true, true, false, true, instance, context,
            "testDestroy1Deployed2", MockDrone.class, MockDrone.class);

        runFailingTestMethod(instance, "testShouldFailDeployment1Destroyed", MockDrone.class);

        runDeploymentTestMethod(dronePoint1, dronePoint2, false, true, false, false, instance, context,
            "testDestroy2Destroyed1", MockDrone.class);

        runFailingTestMethod(instance, "testShouldFailDeployment2Destroyed", MockDrone.class);

        fire(new AfterClass(ManualDeploymentClass.class));
    }

    /**
     * Runs a test method of the {@link ManualDeploymentClass} class that should fail with ISE with the message
     * containing: "has deployment lifecycle"
     *
     * @param classInstance
     *     an instance of the {@link ManualDeploymentClass} class
     * @param testMethodName
     *     the test method to be run
     * @param params
     *     parameters of the test method (if any)
     *
     * @throws Exception
     *     When anything bad happens
     */
    private void runFailingTestMethod(Object classInstance, String testMethodName, Class<MockDrone>... params)
        throws Exception {
        Method testMethodBeforeDeployment = ManualDeploymentClass.class.getMethod(testMethodName, params);

        try {
            enrichClassAndResolveMethod(classInstance, testMethodBeforeDeployment);
        } catch (IllegalStateException ise) {
            if (ise.getMessage().contains("has deployment lifecycle")) {
                return;
            }
        }
        Assert.fail("The test method " + testMethodName
            + " should have thrown the IllegalStateException containing word: \"has deployment lifecycle\"");
    }

    /**
     * Runs a test method of the {@link ManualDeploymentClass} class and check if the given {@link DronePoint}s are
     * instantiated or not
     *
     * @param dronePoint1
     *     a drone point tied to {@link AnnotationMocks.DEPLOYMENT_1} deployment to be verified
     * @param dronePoint2
     *     a drone point tied to {@link AnnotationMocks.DEPLOYMENT_2} deployment to be verified
     * @param before1
     *     whether the {@code dronePoint1} should be instantiated before the test run
     * @param before2
     *     whether the {@code dronePoint2} should be instantiated before the test run
     * @param after1
     *     whether the {@code dronePoint1} should be instantiated after the test run
     * @param after2
     *     whether the {@code dronePoint2} should be instantiated after the test run
     * @param classInstance
     *     an instance of the {@link ManualDeploymentClass} class
     * @param context
     *     a drone context the given {@link DronePoint}s should belong to
     * @param testMethodName
     *     the test method to be run
     * @param params
     *     parameters of the test method (if any)
     *
     * @throws Exception
     *     When anything bad happens
     */
    private void runDeploymentTestMethod(DronePoint<MockDrone> dronePoint1, DronePoint<MockDrone> dronePoint2,
        boolean before1, boolean before2, boolean after1, boolean after2, Object classInstance, DroneContext context,
        String testMethodName, Class<MockDrone>... params) throws Exception {

        Method testMethodBeforeDeployment = ManualDeploymentClass.class.getMethod(testMethodName, params);

        Object[] parameters = enrichClassAndResolveMethod(classInstance, testMethodBeforeDeployment);

        verifyDronePointInstantiated(before1, context, dronePoint1);
        verifyDronePointInstantiated(before2, context, dronePoint2);

        testMethodBeforeDeployment.invoke(classInstance, parameters);

        verifyDronePointInstantiated(after1, context, dronePoint1, before1 && !after1);
        verifyDronePointInstantiated(after2, context, dronePoint2, before1 && !after1);

        fire(new After(classInstance, testMethodBeforeDeployment));
    }

    /**
     * Mocks a {@link TestClass} to return the given class when the method  {@link TestClass#getJavaClass()} is called.
     * The mocked test class will be also injectable inside of the Drone implementation.
     *
     * @param classToReturn
     *     class to be returned
     */
    private void mockTestClass(final Class<?> classToReturn) {
        TestClass testClassMock = Mockito.mock(TestClass.class);
        Mockito.when(testClassMock.getJavaClass()).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return classToReturn;
            }
        });

        bind(ApplicationScoped.class, TestClass.class, testClassMock);
    }

    class ManualDeploymentClass {
        @Drone
        @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_1)
        MockDrone deploymentDrone1;

        @Drone
        @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_2)
        MockDrone deploymentDrone2;

        public void testBeforeDeployment() {
            assertMockDrone1(false);
            assertMockDrone2(false);
        }

        public void testShouldFailDeployment1NotDeployed(
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_1) MockDrone paramDeplDrone1) {
        }

        public void testDeploy1() {
            assertMockDrone1(false);
            assertMockDrone2(false);
            fire(new AfterDeploy(deployableContainer, deploymentDescription1));
            assertMockDrone1(true);
            assertMockDrone2(false);
        }

        public void testShouldFailDeployment2NotDeployed(
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_2) MockDrone paramDeplDrone2) {
        }

        public void testDeploy2Deployed1(
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_1) MockDrone paramDeplDrone1) {
            assertMockDrone1(true);
            assertMockDrone2(false);
            assertMockDrone(true, paramDeplDrone1, AnnotationMocks.DEPLOYMENT_1);
            fire(new AfterDeploy(deployableContainer, deploymentDescription2));
            assertMockDrone1(true);
            assertMockDrone2(true);
            assertMockDrone(true, paramDeplDrone1, AnnotationMocks.DEPLOYMENT_1);
        }

        public void testDeployed12(
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_1) MockDrone paramDeplDrone1,
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_2) MockDrone paramDeplDrone2) {
            assertMockDrone1(true);
            assertMockDrone2(true);
            assertMockDrone(true, paramDeplDrone1, AnnotationMocks.DEPLOYMENT_1);
            assertMockDrone(true, paramDeplDrone2, AnnotationMocks.DEPLOYMENT_2);
        }

        public void testDestroy1Deployed2(
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_1) MockDrone paramDeplDrone1,
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_2) MockDrone paramDeplDrone2) {
            assertMockDrone1(true);
            assertMockDrone2(true);
            assertMockDrone(true, paramDeplDrone1, AnnotationMocks.DEPLOYMENT_1);
            assertMockDrone(true, paramDeplDrone2, AnnotationMocks.DEPLOYMENT_2);
            fire(new BeforeUnDeploy(deployableContainer, deploymentDescription1));
        }

        public void testShouldFailDeployment1Destroyed(
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_1) MockDrone paramDeplDrone1) {
        }

        public void testDestroy2Destroyed1(
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_2) MockDrone paramDeplDrone2) {
            assertMockDrone1(true);
            assertMockDrone2(true);
            assertMockDrone(true, paramDeplDrone2, AnnotationMocks.DEPLOYMENT_2);
            fire(new BeforeUnDeploy(deployableContainer, deploymentDescription2));
        }

        public void testShouldFailDeployment2Destroyed(
            @Drone @OperateOnDeployment(AnnotationMocks.DEPLOYMENT_2) MockDrone paramDeplDrone2) {
        }

        private void assertMockDrone1(boolean shouldExist) {
            assertMockDrone(shouldExist, deploymentDrone1, AnnotationMocks.DEPLOYMENT_1);
        }

        private void assertMockDrone2(boolean shouldExist) {
            assertMockDrone(shouldExist, deploymentDrone2, AnnotationMocks.DEPLOYMENT_2);
        }

        private void assertMockDrone(boolean shouldExist, MockDrone mockDrone, String deploymentName) {
            if (shouldExist) {
                Assert.assertNotNull(
                    "Mock drone instance tied to " + deploymentName + " should NOT be null", mockDrone);
            } else {
                Assert.assertNull(
                    "Mock drone instance tied to " + deploymentName + " should be null", mockDrone);
            }
        }
    }
}
