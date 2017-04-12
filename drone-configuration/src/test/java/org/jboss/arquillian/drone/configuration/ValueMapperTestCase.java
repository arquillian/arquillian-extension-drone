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

import java.net.URI;
import java.net.URL;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for value mapping
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class ValueMapperTestCase {

    @Test
    public void valueMappersTest() throws Exception {
        ArquillianDescriptor descriptor = Descriptors.create(ArquillianDescriptor.class).extension("mockdrone")
            .property("booleanField", "true").property("intField", "12345").property("integerField", "123456")
            .property("urlField", new URL("http://foo.org").toString())
            .property("uriField", new URI("http://foo.org").toString()).property("longField", "12345678");

        MockDroneConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor,
            new MockDroneConfiguration(), Default.class);

        Assert.assertEquals("booleanField is set", true, configuration.isBooleanField());
        Assert.assertEquals("intField is set", 12345, configuration.getIntField());
        Assert.assertEquals("longField is set", 12345678, configuration.getLongField());
        Assert.assertEquals("integerField is set", new Integer(123456), configuration.getIntegerField());
        Assert.assertEquals("urlField is set", new URL("http://foo.org"), configuration.getUrlField());
        Assert.assertEquals("uriField is set", new URI("http://foo.org"), configuration.getUriField());
    }
}
