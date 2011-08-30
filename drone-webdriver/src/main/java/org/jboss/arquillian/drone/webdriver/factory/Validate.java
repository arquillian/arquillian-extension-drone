/*
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
package org.jboss.arquillian.drone.webdriver.factory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
class Validate {

    static boolean empty(String object) {
        return object == null || object.length() == 0;
    }

    static boolean nonEmpty(String object) {
        return !empty(object);
    }

    static void isEmpty(String object, String message) throws IllegalArgumentException {
        if (empty(object)) {
            throw new IllegalArgumentException(message);
        }
    }

    static void isValidPath(String path, String message) throws IllegalArgumentException {
        isEmpty(path, message);

        File file = new File(path);

        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException(message);
        }
    }

    static void isValidUrl(String url, String message) throws IllegalArgumentException {
        isEmpty(url, message);

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    static void isExecutable(String path, String message) throws IllegalArgumentException {
        isEmpty(path, message);

        File file = new File(path);

        if (!file.exists() || !file.canExecute()) {
            throw new IllegalArgumentException(message);
        }
    }

}
