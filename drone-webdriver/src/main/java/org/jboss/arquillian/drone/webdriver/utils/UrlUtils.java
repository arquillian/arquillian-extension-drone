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
package org.jboss.arquillian.drone.webdriver.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;

/**
 * A simple URL utility class
 */
public class UrlUtils {

    /**
     * Tries to open connection to the default URL of Selenium Server (localhost:4444) to check whether
     * Selenium Hub is started
     */
    public static boolean isSeleniumHubRunningOnDefaultUrl() {
        return isReachable(WebDriverConfiguration.DEFAULT_REMOTE_URL);
    }

    /**
     * Tries to open connection to the given {@link URL} to check it is reachable
     */
    public static boolean isReachable(URL url) {

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), 1000);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    /**
     * Checks whether the given {@link URL} is a localhost address
     */
    public static boolean isLocalhost(URL url) {
        try {
            // Check if the address is a valid special local or loop back
            InetAddress address = InetAddress.getByName(url.getHost());
            if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
                return true;
            }

            // Check if the address is defined on any interface
            try {
                return NetworkInterface.getByInetAddress(address) != null;
            } catch (SocketException e) {
                return false;
            }
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
