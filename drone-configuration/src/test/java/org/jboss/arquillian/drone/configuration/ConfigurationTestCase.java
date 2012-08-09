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
package org.jboss.arquillian.drone.configuration;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 */
public class ConfigurationTestCase {

    @Test
    public void arquillianDescriptorTest() {
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone")
                .property("intField", "12345").property("stringField", "The descriptor string")
                .property("booleanField", "true");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
                new MockDroneConfiguration(), Default.class);

        validateConfiguration(configuration, 12345, true, "The descriptor string");
    }

    @Test
    public void systemPropertyTest() {
        try {
            System.setProperty("arquillian.mockdrone.int.field", "54321");
            System.setProperty("arquillian.mockdrone.string.field", "The property string");
            System.setProperty("arquillian.mockdrone.boolean.field", "false");

            MockDroneConfiguration configuration = ConfigurationMapper.fromSystemConfiguration(new MockDroneConfiguration(),
                    Default.class);

            validateConfiguration(configuration, 54321, false, "The property string");
        } finally {
            System.clearProperty("arquillian.mockdrone.int.field");
            System.clearProperty("arquillian.mockdrone.string.field");
            System.clearProperty("arquillian.mockdrone.boolean.field");
        }
    }

    @Test
    public void qualifierSystemPropertyTest() {
        try {
            System.setProperty("arquillian.mockdrone.int.field", "54321");
            System.setProperty("arquillian.mockdrone.string.field", "The property string");
            System.setProperty("arquillian.mockdrone.boolean.field", "false");

            System.setProperty("arquillian.mockdrone.differentmock.string.field", "The mock property field");

            MockDroneConfiguration configuration = ConfigurationMapper.fromSystemConfiguration(new MockDroneConfiguration(),
                    DifferentMock.class);

            validateConfiguration(configuration, 0, false, "The mock property field");
        } finally {
            System.clearProperty("arquillian.mockdrone.int.field");
            System.clearProperty("arquillian.mockdrone.string.field");
            System.clearProperty("arquillian.mockdrone.boolean.field");
        }
    }

    @Test
    public void qualifierArquillianDescriptorTest() {
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone")
                .property("intField", "12345").property("stringField", "The descriptor string")
                .property("booleanField", "true").extension("mockdrone-different")
                .property("stringField", "The different property field");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
                new MockDroneConfiguration(), Different.class);

        validateConfiguration(configuration, 0, false, "The different property field");
    }

    @Test
    public void mapFromDescriptorTest() {
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone")
                .property("intField", "12345").property("stringField", "The descriptor string")
                .property("booleanField", "true").property("mapFoo", "bar").property("mapFooBar", "barbar");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
                new MockDroneConfiguration(), Default.class);

        validateConfiguration(configuration, 12345, true, "The descriptor string");

        Assert.assertNotNull("Map was created", configuration.getMapMap());
        Assert.assertEquals("Map has two entries", 2, configuration.getMapMap().size());

        Assert.assertEquals("Map entry was mapped", "barbar", configuration.getMapMap().get("foo.bar"));
    }

    @Test
    public void mapFromSystemPropertiesTest() {
        try {
            System.setProperty("arquillian.mockdrone.map.foo", "the-bar");
            System.setProperty("arquillian.mockdrone.map.foo.bar", "the-bar-bar");
            System.setProperty("arquillian.mockdrone.map.foo_bar", "the-bar-bar-bar");
            System.setProperty("arquillian.mockdrone.map.whatever", "the-bar-bar");

            MockDroneConfiguration configuration = ConfigurationMapper.fromSystemConfiguration(new MockDroneConfiguration(),
                    Default.class);

            Assert.assertNotNull("Map was created", configuration.getMapMap());

            Assert.assertEquals("Map havs 4 entries", 4, configuration.getMapMap().size());

            Assert.assertEquals("Map entry was mapped", "the-bar", configuration.getMapMap().get("foo"));
            Assert.assertEquals("Map entry was mapped", "the-bar-bar", configuration.getMapMap().get("foo.bar"));
            Assert.assertEquals("Map entry was mapped", "the-bar-bar-bar", configuration.getMapMap().get("foo_bar"));
        } finally {
            System.clearProperty("arquillian.mockdrone.map.foo");
            System.clearProperty("arquillian.mockdrone.map.foo.bar");
            System.clearProperty("arquillian.mockdrone.map.foo_bar");
            System.clearProperty("arquillian.mockdrone.map.whatever");
        }
    }

    @Test
    public void mapFromArquillianDescriptorAndSystemPropertiesTest() {
        try {
            ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone")
                    .property("intField", "12345").property("stringField", "The descriptor string")
                    .property("booleanField", "true").property("mapFoo", "bar").property("mapFooBar", "barbar");

            System.setProperty("arquillian.mockdrone.map.combined.together", "descriptor&properties");

            MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
                    new MockDroneConfiguration(), Default.class);
            ConfigurationMapper.fromSystemConfiguration(configuration, Default.class);

            Assert.assertNotNull("Map was created", configuration.getMapMap());

            Assert.assertEquals("Map has three entries", 3, configuration.getMapMap().size());

            Assert.assertEquals("Map entry was mapped", "bar", configuration.getMapMap().get("foo"));
            Assert.assertEquals("Map entry was mapped", "barbar", configuration.getMapMap().get("foo.bar"));
            Assert.assertEquals("Map entry was mapped", "descriptor&properties", configuration.getMapMap().get("combined.together"));
        } finally {
            System.clearProperty("arquillian.mockdrone.map.combined.together");
        }
    }

    private void validateConfiguration(MockDroneConfiguration configuration, int expectedInt, boolean expectedBoolean,
            String expectedString) throws AssertionError {
        Assert.assertNotNull("Mock drone configuration was created in context", configuration);
        Assert.assertEquals("Mock drone configuration contains int", expectedInt, configuration.getIntField());
        Assert.assertEquals("Mock drone configuration contains boolean", expectedBoolean, configuration.isBooleanField());
        Assert.assertEquals("Mock drone configuration contains string", expectedString, configuration.getStringField());

    }

}
