package org.jboss.arquillian.drone.webdriver.example;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import qualifier.FirefoxUserPrefs;

@RunWith(Arquillian.class)
public class FirefoxUserPrefsTestCase extends AbstractWebDriver {

    @Drone
    @FirefoxUserPrefs
    WebDriver driver;

    @Override
    protected WebDriver driver() {
        return driver;
    }
}
