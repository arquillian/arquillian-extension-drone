/**
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
package org.arquillian.drone.saucelabs.extension.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Util class for resolving download url of the SauceConnect binary
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BinaryUrlUtils {


    /**
     * Returns name of a zip file, that should contain the SauceConnect binary. The name contains corresponding
     * name of the platform the program is running on.
     *
     * @return Formatted name of the SauceConnect zip file
     */
    public static String getPlatformBinaryNameUrl() {
        String basicStaticUrl = "https://saucelabs.com/downloads/sc-4.3.13-";
        String parsedUrl = null;
        switch (PlatformUtils.platform().os()) {
            case WINDOWS:
                parsedUrl = getUrl("win32");
                return Utils.isEmpty(parsedUrl) ? basicStaticUrl + "win32.zip" : parsedUrl;
            case UNIX:
                if (PlatformUtils.is64()) {
                    parsedUrl = getUrl("linux");
                    return Utils.isEmpty(parsedUrl) ? basicStaticUrl + "linux.tar.gz" : parsedUrl;
                } else {
                    parsedUrl = getUrl("linux32");
                    return Utils.isEmpty(parsedUrl) ? basicStaticUrl + "linux32.tar.gz" : parsedUrl;
                }
            case MACOSX:
                parsedUrl = getUrl("osx");
                return Utils.isEmpty(parsedUrl) ? basicStaticUrl + "osx.zip" : parsedUrl;
            default:
                throw new IllegalStateException("The current platform is not supported."
                                                    + "Supported platforms are windows, linux and macosx."
                                                    + "Your platform has been detected as "
                                                    + PlatformUtils.platform().os().toString().toLowerCase() + ""
                                                    + "from the the system property 'os.name' => '" + PlatformUtils.OS
                                                    + "'.");

        }
    }

    private static String getUrl(String platform) {
        JSONObject json = null;
        try {
            json = readJsonFromUrl("https://saucelabs.com/versions.json");
            return json.getJSONObject("Sauce Connect").getJSONObject(platform).getString("download_url");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }
}
