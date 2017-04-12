/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.configuration;

import java.util.List;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.impl.extension.ConfigurationRegistrar;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for verification that Arquillian Core property parser is a proper replacement for legacy Drone property parser
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class ArquillianCorePropertyParserEquivalenceTestCase extends AbstractTestTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ConfigurationRegistrar.class);
    }

    @Test
    public void withoutQualifierArquillianCore() {
        try {
            System.setProperty("arq.extension.mockdrone.booleanField", "true");
            System.setProperty("arq.extension.mockdrone.abcDef", "abcDef value");

            fire(new ManagerStarted());

            ArquillianDescriptor descriptor = getManager().getContext(ApplicationContext.class).getObjectStore()
                .get(ArquillianDescriptor.class);

            Assert.assertNotNull("ArquillianDescriptor was created", descriptor);

            MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
                new MockDroneConfiguration(), Default.class);

            Assert.assertNotNull("Map was created", configuration.getMapMap());

            Assert.assertEquals("Map has five entries", 1, configuration.getMapMap().size());

            Assert.assertEquals("Boolean field was set", true, configuration.isBooleanField());
            Assert.assertEquals("Map entry was mapped", "abcDef value", configuration.getMapMap().get("abcDef"));
        } finally {
            System.clearProperty("arq.extension.mockdrone.booleanField");
            System.clearProperty("arq.extension.mockdrone.abcDef");
        }
    }

    @Test
    public void withQualifierArquillianCore() {
        try {
            System.setProperty("arq.extension.mockdrone-different.booleanField", "true");
            System.setProperty("arq.extension.mockdrone-different.abcDef", "abcDef value");

            fire(new ManagerStarted());

            ArquillianDescriptor descriptor = getManager().getContext(ApplicationContext.class).getObjectStore()
                .get(ArquillianDescriptor.class);

            Assert.assertNotNull("ArquillianDescriptor was created", descriptor);

            MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
                new MockDroneConfiguration(), Different.class);

            Assert.assertNotNull("Map was created", configuration.getMapMap());

            Assert.assertEquals("Map has five entries", 1, configuration.getMapMap().size());

            Assert.assertEquals("Boolean field was set", true, configuration.isBooleanField());
            Assert.assertEquals("Map entry was mapped", "abcDef value", configuration.getMapMap().get("abcDef"));
        } finally {
            System.clearProperty("arq.extension.mockdrone-different.booleanField");
            System.clearProperty("arq.extension.mockdrone-different.abcDef");
        }
    }
}
