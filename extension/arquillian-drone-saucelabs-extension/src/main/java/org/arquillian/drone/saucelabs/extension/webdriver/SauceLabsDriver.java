/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
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
package org.arquillian.drone.saucelabs.extension.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * This is an implementation of the {@link RemoteWebDriver} intended to be used with Sauce Labs account.
 * saucelabs.com is a cloud-based cross-browser grid for executing Selenium WebDriver tests. See saucelabs.com
 */
public class SauceLabsDriver extends RemoteWebDriver {

    private static final Logger log = Logger.getLogger(SauceLabsDriver.class.getName());

    private final boolean isSetSauceConnectManaged;

    public SauceLabsDriver(URL url, Capabilities capabilities, boolean isSetSauceConnectManaged) {
        super(url, capabilities);
        this.isSetSauceConnectManaged = isSetSauceConnectManaged;
    }

    @Override
    public void get(String url) {
        String host = null;
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            log.warning("The url " + url + " has been detected as a malformed URL. The message of the exception: " + e
                .getMessage());
        }

        if (host != null && (host.equals("localhost") || host.equals("127.0.0.1"))) {
            if (!isSetSauceConnectManaged) {
                log.info(
                    "To test against localhost and other locations behind your firewall, you need to use Sauce Connect. "
                        + "You can ignore this if you have already started it, otherwise see saucelabs.com/connect");
            }
        }
        super.get(url);
    }
}
