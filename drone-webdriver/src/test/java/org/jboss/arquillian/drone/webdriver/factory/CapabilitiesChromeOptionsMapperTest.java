/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.junit.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;

public class CapabilitiesChromeOptionsMapperTest {

    private static final Gson GSON = new Gson();

    private static Map<String, Map<String, String>> handleJson(String capability) {

        String trimmedCapability = StringUtils.trimMultiline(capability);
        JsonObject json = new JsonParser().parse(trimmedCapability).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, Map<String, String>> dictionaries = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            Map<String, String> values = GSON.fromJson(entry.getValue(), type);
            dictionaries.put(key, values);
        }

        return dictionaries;
    }

    @Test
    public void testParseChromeOptions() throws IOException {
        ChromeOptions chromeOptions = new ChromeOptions();
        MutableCapabilities capabilities = new MutableCapabilities();

        String arguments = "--my-cool --arguments";
        capabilities.setCapability("chromeArguments", arguments);

        String extensions =
            "src/test/resources/files/my-file src/test/resources/files/cool-file src/test/resources/files/cool-extension";
        capabilities.setCapability("chromeExtensions", extensions);

        String encodedExtensions = "src/test/resources/files/cool-file src/test/resources/files/cool-extension";
        capabilities.setCapability("chromeEncodedExtensions", encodedExtensions);

        String experimentalOptionJson = "{\"perfLoggingPrefs\": {\n"
            + "\"traceCategories\": \",blink.console,disabled-by-default-devtools.timeline,benchmark\"\n"
            + " }, \"prefs\": {\"download.default_directory\": \"/usr/local/path/to/download/directory\"} }";
        capabilities.setCapability("chromeExperimentalOption", experimentalOptionJson);

        CapabilitiesOptionsMapper.mapCapabilities(chromeOptions, capabilities, "chrome");

        ChromeOptions expectedChromeOptions = new ChromeOptions();
        expectedChromeOptions.addArguments(arguments.split(" "));
        for (String path : extensions.split(" ")) {
            expectedChromeOptions.addExtensions(new File(path));
        }
        expectedChromeOptions.addEncodedExtensions(encodedExtensions.split(" "));

        Map<String, Map<String, String>> dicts = handleJson(experimentalOptionJson);
        for (String param : dicts.keySet()) {
            expectedChromeOptions.setExperimentalOption(param, dicts.get(param));
        }

        Assertions.assertThat(expectedChromeOptions).isEqualToComparingFieldByFieldRecursively(chromeOptions);
    }

    // reproducer for https://github.com/arquillian/arquillian-extension-drone/issues/114
    @Test
    public void testParseChromeOptionsWithSimpleJsonAsExperimentalOption() throws IOException {
        // given
        ChromeOptions chromeOptions = new ChromeOptions();
        MutableCapabilities capabilities = new MutableCapabilities();

        String experimentalOptionJson = "{" +
            "\"booleanOption\": false," +
            "\"stringOption\": \"hello\"," +
            "\"numberOption\": 12345" +
            "}";
        capabilities.setCapability("chromeExperimentalOption", experimentalOptionJson);

        // when
        CapabilitiesOptionsMapper.mapCapabilities(chromeOptions, capabilities, "chrome");

        //then
        ChromeOptions expectedChromeOptions = new ChromeOptions();
        expectedChromeOptions.setExperimentalOption("booleanOption", false);
        expectedChromeOptions.setExperimentalOption("stringOption", "hello");
        expectedChromeOptions.setExperimentalOption("numberOption", new LazilyParsedNumber("12345"));
        Assertions.assertThat(chromeOptions).isEqualToComparingFieldByFieldRecursively(expectedChromeOptions);
    }
}
