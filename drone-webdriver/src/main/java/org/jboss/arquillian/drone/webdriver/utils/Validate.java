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
package org.jboss.arquillian.drone.webdriver.utils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import org.openqa.selenium.Platform;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import static org.openqa.selenium.Platform.WINDOWS;

/**
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class Validate {

    private static final FileExecutableChecker fileExecutableChecker = new FileExecutableChecker();
    private static final ImmutableSet<String> ENDINGS = Platform.getCurrent().is(WINDOWS) ?
        ImmutableSet.of("", ".cmd", ".exe", ".com", ".bat") : ImmutableSet.of("");

    public static boolean empty(Object object) {
        return object == null;
    }

    public static boolean empty(String object) {
        return object == null || object.length() == 0;
    }

    public static boolean nonEmpty(String object) {
        return !empty(object);
    }

    public static void isEmpty(Object object, String message) throws IllegalArgumentException {
        if (empty(object)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isEmpty(String object, String message) throws IllegalArgumentException {
        if (empty(object)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isValidPath(String path, String message) throws IllegalArgumentException {
        isEmpty(path, message);

        File file = new File(path);

        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isValidUrl(URL url, String message) throws IllegalArgumentException {
        if (url == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isValidUrl(String url, String message) throws IllegalArgumentException {
        isEmpty(url, message);

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    public static void isExecutable(String path, String message) throws IllegalArgumentException {
        if (!executable(path)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Find the executable by scanning the file system and the PATH. In the case of Windows this
     * method allows common executable endings (".com", ".bat" and ".exe") to be omitted.
     *
     * @param command The name of the executable to find
     * @return Whether the command is executable or not.
     */
    public static boolean isCommandExecutable(String command) throws IllegalArgumentException {
        File file = new File(command);
        if (fileExecutableChecker.canExecute(file)) {
            return true;
        }

        if (Platform.getCurrent().is(WINDOWS)) {
            file = new File(command + ".exe");
            if (fileExecutableChecker.canExecute(file)) {
                return true;
            }
        }

        final ImmutableSet.Builder<String> pathSegmentBuilder = new ImmutableSet.Builder<>();
        addPathFromEnvironment(pathSegmentBuilder);
        if (Platform.getCurrent().is(Platform.MAC)) {
            addMacSpecificPath(pathSegmentBuilder);
        }

        for (String pathSegment : pathSegmentBuilder.build()) {
            for (String ending : ENDINGS) {
                file = new File(pathSegment, command + ending);
                if (fileExecutableChecker.canExecute(file)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addPathFromEnvironment(final ImmutableSet.Builder<String> pathSegmentBuilder) {
        String pathName = "PATH";
        Map<String, String> env = System.getenv();
        if (!env.containsKey(pathName)) {
            for (String key : env.keySet()) {
                if (pathName.equalsIgnoreCase(key)) {
                    pathName = key;
                    break;
                }
            }
        }
        String path = env.get(pathName);
        if (path != null) {
            pathSegmentBuilder.add(path.split(File.pathSeparator));
        }
    }

    private static void addMacSpecificPath(final ImmutableSet.Builder<String> pathSegmentBuilder) {
        File pathFile = new File("/etc/paths");
        if (pathFile.exists()) {
            try {
                pathSegmentBuilder.addAll(Files.readLines(pathFile, Charsets.UTF_8));
            } catch (IOException e) {
                // Guess we won't include those, then
            }
        }
    }

    public static boolean executable(String path) {
        if (empty(path)){
            return false;
        }

        File file = new File(path);

        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("The file %s does not exist", path));
        }

        return fileExecutableChecker.canExecute(file);
    }

    /**
     * Checker if a file can be executed. It requires Java 6 to do that. If anything goes wrong, it supposes that a file can be
     * executed.
     *
     */
    private static final class FileExecutableChecker {
        private static final Logger log = Logger.getLogger(FileExecutableChecker.class.getName());

        private final Method isExecutableMethod;

        FileExecutableChecker() {
            Method m = null;
            try {
                m = File.class.getMethod("canExecute");
            } catch (SecurityException e) {
                log.warning("Unable to verify executable bits for files, will consider them all executable. " + e.getMessage());
            } catch (NoSuchMethodException e) {
                log.warning("Unable to verify executable bits for files, will consider them all executable. " + e.getMessage());
            }

            this.isExecutableMethod = m;
        }

        public boolean canExecute(File file) {
            if (isExecutableMethod == null) {
                return true;
            }

            Boolean result = true;
            try {
                result = (Boolean) isExecutableMethod.invoke(file);
            } catch (IllegalArgumentException e) {
                log.warning("Unable to check if " + file.getAbsolutePath() + " can be executed, will consider it executable."
                        + e.getMessage());
            } catch (IllegalAccessException e) {
                log.warning("Unable to check if " + file.getAbsolutePath() + " can be executed, will consider it executable."
                        + e.getMessage());
            } catch (InvocationTargetException e) {
                log.warning("Unable to check if " + file.getAbsolutePath() + " can be executed, will consider it executable."
                        + e.getMessage());
            }

            return result;
        }
    }

}
