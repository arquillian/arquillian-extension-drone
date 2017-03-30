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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for map related configurations
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class MapPropertiesMappingTestCase {

    @Test
    public void mapFromDescriptorTest() {
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class)
            .extension("mockdrone")
            .property("intField", "12345")
            .property("stringField", "The descriptor string")
            .property("booleanField", "true")
            .property("foo", "foo value")
            .property("foo.bar", "foo.bar value")
            .property("foo_bar", "foo_bar value")
            .property("abcDef", "abcDef value")
            .property("FOOO-bar", "FOOO-bar value");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
            new MockDroneConfiguration(), Default.class);

        Assert.assertNotNull("Map was created", configuration.getMapMap());
        Assert.assertEquals("Map has five entries", 5, configuration.getMapMap().size());

        Assert.assertEquals("Map entry was mapped", "foo value", configuration.getMapMap().get("foo"));
        Assert.assertEquals("Map entry was mapped", "foo.bar value", configuration.getMapMap().get("foo.bar"));
        Assert.assertEquals("Map entry was mapped", "foo_bar value", configuration.getMapMap().get("foo_bar"));
        Assert.assertEquals("Map entry was mapped", "abcDef value", configuration.getMapMap().get("abcDef"));
        Assert.assertEquals("Map entry was mapped", "FOOO-bar value", configuration.getMapMap().get("FOOO-bar"));
    }

    @Test
    public void mapFromPropertiesWithoutName() {
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class)
            .extension("mockdrone")
            .property(null, "A NULL property")
            .property("intField", "12345")
            .property("stringField", "The descriptor string")
            .property("booleanField", "true")
            .property("foo", "foo value")
            .property("foo.bar", "foo.bar value")
            .property("foo_bar", "foo_bar value")
            .property("abcDef", "abcDef value")
            .property("FOOO-bar", "FOOO-bar value");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
            new MockDroneConfiguration(), Default.class);

        Assert.assertNotNull("Map was created", configuration.getMapMap());
        Assert.assertEquals("Map has five entries", 5, configuration.getMapMap().size());

        Assert.assertEquals("Map entry was mapped", "foo value", configuration.getMapMap().get("foo"));
        Assert.assertEquals("Map entry was mapped", "foo.bar value", configuration.getMapMap().get("foo.bar"));
        Assert.assertEquals("Map entry was mapped", "foo_bar value", configuration.getMapMap().get("foo_bar"));
        Assert.assertEquals("Map entry was mapped", "abcDef value", configuration.getMapMap().get("abcDef"));
        Assert.assertEquals("Map entry was mapped", "FOOO-bar value", configuration.getMapMap().get("FOOO-bar"));
    }
}