/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.selenium.server;

import java.util.List;

import org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerConfigured;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerStarted;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerStopped;
import org.jboss.arquillian.drone.selenium.server.impl.SeleniumServerCreator;
import org.jboss.arquillian.drone.selenium.server.impl.SeleniumServerDestructor;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.server.SeleniumServer;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumServerTestCase extends AbstractTestTestBase {
    @Spy
    SeleniumServerConfiguration configuration = new SeleniumServerConfiguration();

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(SeleniumServerCreator.class);
        extensions.add(SeleniumServerDestructor.class);
    }

    @Before
    public void setMocks() {
        bind(SuiteScoped.class, SeleniumServerConfiguration.class, configuration);
        Mockito.when(configuration.isEnable()).thenReturn(true);
    }

    @Test
    public void serverCreatedAndDestroyed() throws Exception {

        fire(new SeleniumServerConfigured(configuration));

        SeleniumServer server = getManager().getContext(SuiteContext.class).getObjectStore().get(SeleniumServer.class);

        Assert.assertNotNull("Selenium configuration object is present in context", server);
        assertEventFired(SeleniumServerStarted.class, 1);

        fire(new AfterSuite());

        assertEventFired(SeleniumServerStopped.class, 1);
    }
}
