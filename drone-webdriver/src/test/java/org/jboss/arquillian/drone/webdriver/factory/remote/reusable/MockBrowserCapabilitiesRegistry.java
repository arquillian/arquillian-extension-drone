package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilitiesRegistry;
import org.junit.Assert;

import static org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil.WEBDRIVER_REUSABLE_EXT;
import static org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil.getBrowserProperty;

public class MockBrowserCapabilitiesRegistry implements BrowserCapabilitiesRegistry {

    private final Map<String, BrowserCapabilities> cache;

    public MockBrowserCapabilitiesRegistry() {
        this.cache = new HashMap<String, BrowserCapabilities>();

        String browser = getBrowserProperty(WEBDRIVER_REUSABLE_EXT);
        if ("chrome".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Chrome());
        } else if ("edge".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Edge());
        } else if ("firefox".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Firefox());
        } else if ("htmlunit".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.HtmlUnit());
        } else if ("internetexplorer".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.InternetExplorer());
        } else if ("opera".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Opera());
        } else if ("safari".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.Safari());
        } else if ("chromeheadless".equals(browser)) {
            registerBrowserCapabilitiesFor(browser, new BrowserCapabilitiesList.ChromeHeadless());
        } else {
            Assert.fail("MockBrowserCapabilitiesRegistry does not implement " + browser);
        }
    }

    public static MockBrowserCapabilitiesRegistry createSingletonRegistry() {

        MockBrowserCapabilitiesRegistry registry = new MockBrowserCapabilitiesRegistry();
        return registry;
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
    public BrowserCapabilitiesRegistry registerBrowserCapabilitiesFor(String key,
        BrowserCapabilities browserCapabilities) {
        cache.put(key, browserCapabilities);
        return this;
    }

    @Override
    public Collection<BrowserCapabilities> getAllBrowserCapabilities() {

        Assert.assertEquals("There is only one BrowserCapability defined", 1, cache.size());

        return Collections.unmodifiableCollection(cache.values());
    }
}
