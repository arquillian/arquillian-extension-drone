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
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.util.List;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.drone.webdriver.binary.process.SeleniumServerExecutor;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class TestReusedSessionStore extends AbstractTestTestBase {

    @Inject
    Instance<ReusedSessionStore> store;
    @Mock
    private ServiceLoader serviceLoader;
    @Mock
    private ReusedSessionPermanentStorage permanentStorage;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ReusableRemoteWebDriverExtension.class);
        extensions.add(SeleniumServerExecutor.class);
    }

    @Before
    public void before() {
        when(serviceLoader.onlyOne(ReusedSessionPermanentStorage.class)).thenReturn(permanentStorage);
        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
    }

    @Test
    public void test_store_is_created_on_before_suite_event() {
        getManager().fire(new BeforeSuite());
        assertNotNull(store.get());
        getManager().fire(new AfterSuite());
    }
}
