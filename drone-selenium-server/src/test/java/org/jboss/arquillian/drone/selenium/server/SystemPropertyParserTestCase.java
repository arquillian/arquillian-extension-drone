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
package org.jboss.arquillian.drone.selenium.server;

import org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.selenium.server.impl.SystemEnvHolder;
import org.junit.Assert;
import org.junit.Test;

public class SystemPropertyParserTestCase {

    @Test
    public void setSystemProperty() {
        SeleniumServerConfiguration configuration = new SeleniumServerConfiguration();
        configuration.setSystemProperties("-Dfoo=bar -Dfoo2=bar2 -Dfoo3 -Dfoo4=bar4");

        SystemEnvHolder env = new SystemEnvHolder();
        env.modifyEnvBy(configuration);

        Assert.assertEquals("foo property was set to bar", System.getProperty("foo"), "bar");
        Assert.assertEquals("foo2 property was set to bar2", System.getProperty("foo2"), "bar2");
        Assert.assertEquals("foo4 property was set to bar4", System.getProperty("foo4"), "bar4");

        System.clearProperty("foo");
        System.clearProperty("foo2");
        System.clearProperty("foo4");
    }

    @Test
    public void setProxyAndRestore() {
        SeleniumServerConfiguration configuration = new SeleniumServerConfiguration();
        configuration.setProxyHost("localhost");
        configuration.setProxyPort("8888");
        configuration.setNonProxyHosts("localhost,mymachine");

        SystemEnvHolder env = new SystemEnvHolder();

        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        String nonProxyHosts = System.getProperty("http.nonProxyHosts");

        env.modifyEnvBy(configuration);

        Assert.assertEquals("http.proxyHost was set", System.getProperty("http.proxyHost"), "localhost");
        Assert.assertEquals("http.proxyPort was set", System.getProperty("http.proxyPort"), "8888");
        Assert.assertEquals("http.nonProxyHosts was set", System.getProperty("http.nonProxyHosts"), "localhost,mymachine");

        env.restore();

        Assert.assertEquals("http.proxyHost was restored", System.getProperty("http.proxyHost"), proxyHost);
        Assert.assertEquals("http.proxyPort was restored", System.getProperty("http.proxyPort"), proxyPort);
        Assert.assertEquals("http.nonProxyHosts was restored", System.getProperty("http.nonProxyHosts"), nonProxyHosts);
    }
}
