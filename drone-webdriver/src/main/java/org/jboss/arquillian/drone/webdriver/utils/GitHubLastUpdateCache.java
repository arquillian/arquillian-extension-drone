package org.jboss.arquillian.drone.webdriver.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;

public class GitHubLastUpdateCache {

    private static final String ASSET_PROPERTY = "asset";
    private static final String LAST_MODIFIED_PROPERTY = "lastModified";

    private final File cacheDirectory;
    private final Gson gson;

    public GitHubLastUpdateCache(final File cacheDirectory) {
        this.gson = new Gson();
        this.cacheDirectory = cacheDirectory;
    }

    public LocalDateTime lastModificationOf(String uniqueKey) {
        final JsonObject lastModificationDate = deserializeCachedFile(uniqueKey).getAsJsonObject(LAST_MODIFIED_PROPERTY);
        return gson.fromJson(lastModificationDate, LocalDateTime.class);
    }

    public <T> T load(String uniqueKey, Class<T> type) {
        final JsonObject asset = deserializeCachedFile(uniqueKey).getAsJsonObject(ASSET_PROPERTY);
        return gson.fromJson(asset, type);
    }

    public <T> void store(T asset, String uniqueKey, LocalDateTime dateTime) {
        final String cachedFilePath = createCachedFilePath(uniqueKey);
        final JsonObject jsonObject = combineAsJson(asset, dateTime);
        try (final FileOutputStream fileOutputStream = new FileOutputStream(cachedFilePath, false)) {
            fileOutputStream.write(jsonObject.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Unable to store " + jsonObject + "%n as [" + cachedFilePath + "]",e);
        }
    }

    private String createCachedFilePath(String uniqueKey) {
        return cacheDirectory.getAbsolutePath() + "/gh.cache." + uniqueKey + ".json";
    }

    private JsonObject deserializeCachedFile(String uniqueKey) {
        final String cachedFilePath = createCachedFilePath(uniqueKey);
        try (final FileReader reader = new FileReader(cachedFilePath)) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize file [" + cachedFilePath + "]", e);
        }
    }

    private <T> JsonObject combineAsJson(T asset, LocalDateTime dateTime) {
        final JsonElement assetAsJson = gson.toJsonTree(asset);
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add(LAST_MODIFIED_PROPERTY, gson.toJsonTree(dateTime)); // TODO change to GMT, as this is required for HTTP header
        jsonObject.add(ASSET_PROPERTY, assetAsJson);
        return jsonObject;
    }
}
