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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse a list of strings
 *
 * @author <a href="trepel@redhat.com">Tomas Repel</a>
 */
public class StringUtils {

    /**
     * Parse string to tokens. Tokens are separated by whitespace. In case some token contains whitespace,
     * the whole token has to be quoted. For instance string 'opt0 opt1=val1 "opt2=val2 with space"' results in three
     * tokens.
     *
     * @param stringToBeParsed
     *     - string to be parsed to tokens
     *
     * @return List of tokens, returns empty list rather that null value
     */
    public static List<String> tokenize(String stringToBeParsed) {

        final String TOKEN = "\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|\\S+";
        final String QUOTED_TOKEN = "^\"(.*)\"$";

        List<String> options = new ArrayList<String>();
        if (stringToBeParsed != null && stringToBeParsed.length() != 0) {
            Pattern p = Pattern.compile(TOKEN, Pattern.DOTALL);
            Matcher m = p.matcher(stringToBeParsed);
            while (m.find()) {
                if (!(m.group().trim().equals(""))) {
                    options.add(Pattern.compile(QUOTED_TOKEN, Pattern.DOTALL).matcher(m.group().trim()).replaceAll("$1"));
                }
            }
        }
        return options;
    }

    public static String trimMultiline(String toTrim) {
        final StringBuilder builder = new StringBuilder(toTrim.length());
        for (String token : toTrim.split("\\s+")) {
            if (token != null) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty()) {
                    builder.append(trimmed).append(' ');
                }
            }
        }
        return builder.toString().trim();
    }
}
