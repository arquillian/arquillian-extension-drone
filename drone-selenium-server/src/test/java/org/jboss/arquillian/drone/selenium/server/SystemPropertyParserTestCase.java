package org.jboss.arquillian.drone.selenium.server;

import junit.framework.Assert;

import org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.selenium.server.impl.SystemEnvHolder;
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
