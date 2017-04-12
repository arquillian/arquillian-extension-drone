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

import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.drone.impl.DroneLifecycleManager.GlobalDroneConfiguration;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Global Drone configuration test cases
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class GlobalDroneConfigurationTestCase extends AbstractTestTestBase {

    @Mock
    private ServiceLoader serviceLoader;

    private String oldDebugValue;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DroneLifecycleManager.class);
        extensions.add(DroneRegistrar.class);
    }

    @Before
    public void storeArquillianDebug() {
        oldDebugValue = System.getProperty("arquillian.debug", "false");
        System.setProperty("arquillian.debug", "false");
    }

    @After
    public void restoreArquillianDebug() {
        System.setProperty("arquillian.debug", oldDebugValue);
    }

    @Test
    public void defaultConfiguration() throws Exception {

        // given
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class);

        getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

        // then
        verifyTimeout(DroneLifecycleManager.GlobalDroneConfiguration.DEFAULT_INSTANTIATION_TIMEOUT);
    }

    @Test
    public void specificConfiguration() throws Exception {
        // given
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class)
            .extension("drone").property("instantiationTimeoutInSeconds", "40");

        getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

        // then
        verifyTimeout(40);
    }

    @Test
    public void specificConfigurationWithDebugMode() throws Exception {

        // given
        System.setProperty("arquillian.debug", "true");
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class)
            .extension("drone").property("instantiationTimeoutInSeconds", "40");

        getManager().bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        getManager().bind(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

        // then
        verifyTimeout(0);
    }

    private void verifyTimeout(int timeout) {
        // when
        getManager().fire(new BeforeSuite());

        // then
        DroneRegistry registry = getManager().getContext(SuiteContext.class).getObjectStore().get(DroneRegistry.class);
        Assert.assertNotNull("Drone registry was created in the context", registry);

        DroneContext context = getManager().getContext(ApplicationContext.class).getObjectStore().get(DroneContext
            .class);
        Assert.assertNotNull("DroneContext was created in the context", context);

        GlobalDroneConfiguration globalDroneConfiguration = context.getGlobalDroneConfiguration
            (GlobalDroneConfiguration.class);
        Assert.assertNotNull("Global Drone configuration was created", globalDroneConfiguration);
        Assert.assertEquals("Drone timeout is set to " + timeout + " seconds", timeout,
            globalDroneConfiguration.getInstantiationTimeoutInSeconds());
    }
}
