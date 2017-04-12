/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * <p>
 * Copyright 2007-2009 Selenium committers
 * <p>
 * This is a parsing replacement for org.openqa.selenium.firefox.Preferences, which is not public.
 * As such, it shares the parsing logic.
 */
package org.jboss.arquillian.drone.webdriver.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author Selenium commiters
 */
public class FirefoxPrefsReader {

    private static final Pattern PREFERENCE_PATTERN =
        Pattern.compile("user_pref\\(\"([^\"]+)\", (\"?.+?\"?)\\);");

    /**
     * Reads Firefox preferences in specific format from file and
     *
     * @param prefs
     *     File to be parsed
     *
     * @return Map of String|Boolean|Integer to be later used by FirefoxProfile
     *
     * @throws IllegalArgumentException
     *     If parsing went wrong
     */
    public static Map<String, Object> getPreferences(File prefs) throws IllegalArgumentException {

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(prefs));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Matcher m = PREFERENCE_PATTERN.matcher(line);
                if (m.matches()) {
                    map.put(m.group(1), preferenceAsValue(m.group(2)));
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("User preferences for firefox " + prefs.getAbsolutePath()
                + " does not represent a valid preferences file", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        return map;
    }

    private static Object preferenceAsValue(String toConvert) throws IOException {
        if (toConvert.startsWith("\"") && toConvert.endsWith("\"")) {
            return toConvert.substring(1, toConvert.length() - 1).replaceAll("\\\\\\\\", "\\\\");
        }

        if ("false".equals(toConvert) || "true".equals(toConvert)) {
            return Boolean.parseBoolean(toConvert);
        }

        try {
            return Integer.parseInt(toConvert);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid property format for user preferences " + toConvert);
        }
    }
}
