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

import java.util.Map;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;

/**
 *
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
            .property("booleanField", "true").property("foo", "bar").property("foo.bar", "barbar");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
            new MockDroneConfiguration(), Default.class);

        validateConfiguration(configuration, 12345, true, "The descriptor string");

        Assert.assertNotNull("Map was created", configuration.getMapMap());
        Assert.assertEquals("Map has two entries", 2, configuration.getMapMap().size());

        Assert.assertEquals("Map entry was mapped", "barbar", configuration.getMapMap().get("foo.bar"));
    }

    @Test
    public void mapNonCamelCases() {

        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class)
            .extension("mockdrone")
            .property("opera.no_quit", "true")
            .property("firefox_profile", "JSON value")
            .property("acceptSslCerts", "true");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
            new MockDroneConfiguration(), Default.class);

        Assert.assertNotNull("Map was created", configuration.getMapMap());

        Assert.assertEquals("Map has three entries", 3, configuration.getMapMap().size());

        Assert.assertEquals("Map entry was mapped", "true", configuration.getMapMap().get("opera.no_quit"));
        Assert.assertEquals("Map entry was mapped", "JSON value", configuration.getMapMap().get("firefox_profile"));
        Assert.assertEquals("Map entry was mapped", Boolean.TRUE, configuration.getMapMap().get("acceptSslCerts"));
    }

    @Test
    public void mapPropertiesNeededToBeCast() {

        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone")
            .property("loggingPrefs", "driver=WARNING,profiling=INFO").property("webStorageEnabled", "false")
            .property("acceptSslCerts", "true");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
            new MockDroneConfiguration(), Default.class);

        Assert.assertNotNull("Map was created", configuration.getMapMap());

        Assert.assertEquals("Map has three entries", 3, configuration.getMapMap().size());
        Assert.assertEquals("Map entry was mapped", Boolean.FALSE, configuration.getMapMap().get("webStorageEnabled"));
        Assert.assertEquals("Map entry was mapped", Boolean.TRUE, configuration.getMapMap().get("acceptSslCerts"));

        @SuppressWarnings("unchecked")
        Map<String, String> loggingPrefs = (Map<String, String>) configuration.getMapMap().get("loggingPrefs");
        Assert.assertEquals("Logging Preferencies have two entries", 2, loggingPrefs.size());
        Assert.assertTrue("Logging Preferencies contain driver", loggingPrefs.keySet().contains("driver"));
        Assert.assertTrue("Logging Preferencies contain profiling", loggingPrefs.keySet().contains("profiling"));
        Assert.assertEquals("Logging Preferencies contain WARNING for driver", "WARNING", loggingPrefs.get("driver"));
        Assert.assertEquals("Logging Preferencies contain INFO for profiling", "INFO", loggingPrefs.get("profiling"));
    }

    @Test
    public void mapTypedDroneConfiguration() {
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class).extension("typedmockdrone");

        TypedMockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
            new TypedMockDroneConfiguration(), Default.class);

        Assert.assertNotNull("Map<String, Object> was created", configuration.getObjectMap());
        Assert.assertNull("Map<String, String> was not created", configuration.getStringMap());
    }

    private void validateConfiguration(MockDroneConfiguration configuration, int expectedInt, boolean expectedBoolean,
        String expectedString) throws AssertionError {
        Assert.assertNotNull("Mock drone configuration was created in context", configuration);
        Assert.assertEquals("Mock drone configuration contains int", expectedInt, configuration.getIntField());
        Assert.assertEquals("Mock drone configuration contains boolean", expectedBoolean, configuration.isBooleanField());
        Assert.assertEquals("Mock drone configuration contains string", expectedString, configuration.getStringField());
    }
}
