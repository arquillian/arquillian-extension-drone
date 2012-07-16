package org.jboss.arquillian.drone.webdriver.example;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import qualifier.LegacyConfiguration;

/**
 * Tests legacy configuration for webdriver via implementationClass
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
@RunWith(Arquillian.class)
public class LegacyWebDriverTestCase extends AbstractWebDriver {

    @Drone
    @LegacyConfiguration
    WebDriver driver;

    @Override
    protected WebDriver driver() {
        return driver;
    }

    @Test
    public void isOnFirefox() {
        Assert.assertTrue("Legacy WebDriver configuration is executing Firefox", driver instanceof FirefoxDriver);
    }

}
