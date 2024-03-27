/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package org.arquillian.drone.appium.extension.webdriver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileBrowserType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import io.appium.java_client.windows.WindowsDriver;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.factory.CapabilitiesOptionsMapper;
import org.jboss.arquillian.drone.webdriver.factory.ChromeDriverFactory;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilitiesRegistry;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URL;

import static org.arquillian.drone.appium.extension.webdriver.AppiumCapabilities.READABLE_NAME;

/**
 * Factory which combines {@link org.jboss.arquillian.drone.spi.Configurator},
 * {@link org.jboss.arquillian.drone.spi.Instantiator} and {@link org.jboss.arquillian.drone.spi.Destructor} for
 * Appium Java Client.
 *
 * @see <a href="https://github.com/appium/java-client">https://github.com/appium/java-client</a>
 * @see <a href="https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/caps.md">https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/caps.md</a>
 *  for supported capabilities
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class AppiumDriverFactory implements
    Configurator<AppiumDriver, WebDriverConfiguration>,
    Instantiator<AppiumDriver, WebDriverConfiguration>,
    Destructor<AppiumDriver> {

    @Inject
    private Instance<BrowserCapabilitiesRegistry> registryInstance;

    @Override
    public void destroyInstance(AppiumDriver instance) {
        instance.quit();
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    /**
     * Creates {@link AndroidDriver}, {@link IOSDriver}, {@link WindowsDriver} or generic {@link AppiumDriver}
     * based on {@value MobileCapabilityType#PLATFORM_NAME} in Arquillian descriptor.
     *
     * @param configuration
     *     the configuration object for the extension
     *
     * @return The Appium WebDriver
     */
    @Override
    public AppiumDriver createInstance(WebDriverConfiguration configuration) {
        Capabilities capabilities = getCapabilities(configuration);

        String platform = (String)capabilities.getCapability(MobileCapabilityType.PLATFORM_NAME);
        if (Validate.empty(platform)) {
            throw new IllegalArgumentException("You have to specify " + MobileCapabilityType.PLATFORM_NAME);
        }
        platform = platform.toLowerCase();

        Class<? extends AppiumDriver> driverClass;
        if (MobilePlatform.ANDROID.toLowerCase().equals(platform)) {
            driverClass = AndroidDriver.class;
        }
        else if (MobilePlatform.IOS.toLowerCase().equals(platform)) {
            driverClass = IOSDriver.class;
        }
        else if (MobilePlatform.WINDOWS.toLowerCase().equals(platform)) {
            driverClass = WindowsDriver.class;
        }
        else {
            driverClass = AppiumDriver.class;
        }

        URL remoteAddress = configuration.getRemoteAddress();
        try {
            if (remoteAddress == null) {
                return driverClass.getConstructor(Capabilities.class).newInstance(capabilities);
            }
            else {
                return driverClass.getConstructor(URL.class, Capabilities.class).newInstance(remoteAddress, capabilities);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates {@link Capabilities} instance with Chrome options set in case of Android and Chrome browser
     *
     * @param configuration
     * @return {@link Capabilities} instance
     */
    public Capabilities getCapabilities(WebDriverConfiguration configuration) {
        MutableCapabilities capabilities = new MutableCapabilities(configuration.getCapabilities());

        String browser = (String)capabilities.getCapability(MobileCapabilityType.BROWSER_NAME);
        if (browser != null) browser = browser.toLowerCase();

        // Set chromeOptions
        if (MobileBrowserType.CHROME.toLowerCase().equals(browser)) {
            ChromeOptions chromeOptions = new ChromeOptions();
            CapabilitiesOptionsMapper.mapCapabilities(chromeOptions, capabilities, ChromeDriverFactory.BROWSER_CAPABILITIES);
            capabilities.setCapability(AndroidMobileCapabilityType.CHROME_OPTIONS, chromeOptions);
        }

        return capabilities;
    }

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, DronePoint<AppiumDriver> dronePoint) {
        BrowserCapabilities browser = registryInstance.get().getEntryFor(READABLE_NAME);
        return new WebDriverConfiguration(browser).configure(descriptor, dronePoint.getQualifier());
    }
}
