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
import com.google.gson.reflect.TypeToken;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class CapabilitiesMapperTest {

    @Test
    public void testParseChromeOptions() throws IOException {
        ChromeOptions chromeOptions = new ChromeOptions();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        String arguments = "--my-cool --arguments";
        desiredCapabilities.setCapability("chromeArguments", arguments);

        String extensions =
            "src/test/resources/files/my-file src/test/resources/files/cool-file src/test/resources/files/cool-extension";
        desiredCapabilities.setCapability("chromeExtensions", extensions);

        String encodedExtensions = "src/test/resources/files/cool-file src/test/resources/files/cool-extension";
        desiredCapabilities.setCapability("chromeEncodedExtensions", encodedExtensions);

        String experimentalOptionJson = "{\"perfLoggingPrefs\": {\n"
            + "\"traceCategories\": \",blink.console,disabled-by-default-devtools.timeline,benchmark\"\n"
            + " }, \"prefs\": {\"download.default_directory\": \"/usr/local/path/to/download/directory\"} }";
        desiredCapabilities.setCapability("chromeExperimentalOption", experimentalOptionJson);

        CapabilitiesMapper.mapCapabilities(chromeOptions, desiredCapabilities, "chrome");

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

        Assert.assertEquals(expectedChromeOptions, chromeOptions);
        Assert.assertEquals(expectedChromeOptions.toJson(), chromeOptions.toJson());
    }

    private static Map<String, Map<String, String>> handleJson(String capability) {

        String trimmedCapability = StringUtils.trimMultiline(capability);
        JsonObject json = new JsonParser().parse(trimmedCapability).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, Map<String, String>> dictionaries = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            Map<String, String> values = new Gson().fromJson(entry.getValue(), type);
            dictionaries.put(key, values);
        }

        return dictionaries;
    }
}