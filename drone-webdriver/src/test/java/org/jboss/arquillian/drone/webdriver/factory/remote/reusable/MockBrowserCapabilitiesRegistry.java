package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilitiesRegistry;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;

class MockBrowserCapabilitiesRegistry implements BrowserCapabilitiesRegistry {

    private final Map<String, BrowserCapabilities> cache;

    public static MockBrowserCapabilitiesRegistry createSingletonRegistry() {
        // set desired capabilities
        ArquillianDescriptor arqXml = Descriptors.importAs(ArquillianDescriptor.class).fromStream(
                URLClassLoader.getSystemResourceAsStream("arquillian.xml"), true);

        MockBrowserCapabilitiesRegistry registry = new MockBrowserCapabilitiesRegistry(arqXml);
        return registry;
    }

    public MockBrowserCapabilitiesRegistry(ArquillianDescriptor descriptor) {
        this.cache = new HashMap<String, BrowserCapabilities>();

        String browser = getBrowser(descriptor);
        if ("phantomjs".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.PhantomJS());
        }
        else if ("android".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Android());
        }
        else if ("chrome".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Chrome());
        }
        else if ("firefox".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Firefox());
        }
        else if ("htmlUnit".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.HtmlUnit());
        }
        else if ("internetExplorer".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.InternetExplorer());
        }
        else if ("iphone".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.IPhone());
        }
        else if ("opera".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Opera());
        }
        else if ("safari".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Safari());
        }
        else {
            Assert.fail("MockBrowserCapabilitiesRegistry does not implement " + browser);
        }
    }

    @Override
    public BrowserCapabilities getEntryFor(String key) throws IllegalStateException {
        return cache.get(key);
    }

    @Override
    public BrowserCapabilities getEntryByImplementationClassName(String className) {
        return null;
    }

    @Override
    public BrowserCapabilitiesRegistry registerBrowserCapabilitiesFor(String key, BrowserCapabilities browserCapabilities) {
        cache.put(key, browserCapabilities);
        return this;
    }

    @Override
    public Collection<BrowserCapabilities> getAllBrowserCapabilities() {

        Assert.assertEquals("There is only one BrowserCapability defined", 1, cache.size());

        return Collections.unmodifiableCollection(cache.values());
    }

    private String getBrowser(ArquillianDescriptor arquillian) {
        ExtensionDef webdriver = arquillian.extension("webdriver-reusable");
        Assert.assertNotNull("webdriver-reusable extension should be defined in arquillian.xml", webdriver);
        Map<String, String> props = webdriver.getExtensionProperties();

        return props.get("browser");
    }

}
