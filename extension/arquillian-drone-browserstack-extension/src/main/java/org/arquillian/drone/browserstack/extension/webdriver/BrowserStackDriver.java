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
package org.arquillian.drone.browserstack.extension.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * This is an implementation of the {@link RemoteWebDriver} intended to be used with BrowserStack account.
 * BrowserStack is a cloud-based cross-browser testing tool. See browserstack.com
 */
public class BrowserStackDriver extends RemoteWebDriver {

    private static final Logger log = Logger.getLogger(BrowserStackDriver.class.getName());

    private boolean isSetBrowserStackLocal;
    private boolean isSetBrowserStackLocalManaged;

    public BrowserStackDriver(URL url, Capabilities capabilities, boolean isSetBrowserStackLocal,
        boolean isSetBrowserStackLocalManaged) {
        super(url, capabilities);
        this.isSetBrowserStackLocal = isSetBrowserStackLocal;
        this.isSetBrowserStackLocalManaged = isSetBrowserStackLocalManaged;
    }

    @Override
    public void get(String url) {
        String host = null;
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            log.warning(
                "The url " + url + " has been detected as a malformed URL. The message of the exception: " + e
                    .getMessage());
        }

        if (host != null && (host.equals("localhost") || host.equals("127.0.0.1"))) {
            if (isSetBrowserStackLocal && !isSetBrowserStackLocalManaged) {
                log.info(
                    "To test against localhost and other locations behind your firewall, you need to run a BrowserStackLocal binary. "
                        + "You can ignore this if you have already started it, otherwise see browserstack.com/local-testing");
            }
        }
        super.get(url);
    }
}
