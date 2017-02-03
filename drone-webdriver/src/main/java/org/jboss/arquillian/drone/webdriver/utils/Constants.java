/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import java.io.File;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class Constants {

    public static final String WEB_DRIVER_NOT_FOUND_ERROR_MESSAGE =
        "The required web driver class {0} is not on your class path so the factory {1} will not be available.";

    public static final String FIREFOX_DRIVER = "org.openqa.selenium.firefox.FirefoxDriver";
    public static final String EDGE_DRIVER = "org.openqa.selenium.edge.EdgeDriver";
    public static final String CHROME_DRIVER = "org.openqa.selenium.chrome.ChromeDriver";
    public static final String HTMLUNIT_DRIVER = "org.openqa.selenium.htmlunit.HtmlUnitDriver";
    public static final String IE_DRIVER = "org.openqa.selenium.ie.InternetExplorerDriver";
    public static final String WEB_DRIVER = "org.openqa.selenium.WebDriver";
    public static final String OPERA_DRIVER = "org.openqa.selenium.opera.OperaDriver";
    public static final String REMOTE_DRIVER = "org.openqa.selenium.remote.RemoteWebDriver";
    public static final String SAFARI_DRIVER = "org.openqa.selenium.safari.SafariDriver";
    public static final String PHANTOMJS_DRIVER = "org.openqa.selenium.phantomjs.PhantomJSDriver";

    public static final String ARQUILLIAN_DRONE_CACHE_DIRECTORY =
        System.getProperty("user.home") + File.separator
            + ".arquillian" + File.separator
            + "drone" + File.separator;

    public static final String DRONE_TARGET_DIRECTORY =
        "target" + File.separator + "drone" + File.separator;
}

