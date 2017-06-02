package org.jboss.arquillian.drone.webdriver.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.jboss.arquillian.drone.webdriver.utils.Constants.ARQUILLIAN_DRONE_CACHE_DIRECTORY;

public class GitHubLastUpdateCache {

    private static final String ASSET_PROPERTY = "asset";
    private static final String LAST_MODIFIED_PROPERTY = "lastModified";
    private static final File DEFAULT_CACHE_DIRECTORY =
        new File(ARQUILLIAN_DRONE_CACHE_DIRECTORY + File.separator + "gh_cache" + File.separator);

    private final Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<ZonedDateTime>() {
    }.getType(), new ZonedDateTimeConverter()).create();
    private final File cacheDirectory;

    public GitHubLastUpdateCache(final File cacheDirectory) {
        this.cacheDirectory = createCacheDirectory(cacheDirectory);
    }

    public GitHubLastUpdateCache() {
        this(DEFAULT_CACHE_DIRECTORY);
    }

    private File createCacheDirectory(File cacheDirectory) {
        if (cacheDirectory.exists() && !cacheDirectory.isDirectory()) {
            throw new IllegalArgumentException(
                "Passed [" + cacheDirectory.getAbsolutePath() + "] exists and is not a directory.");
        }
        if (!cacheDirectory.exists() && !cacheDirectory.mkdirs()) {
            throw new RuntimeException("Could not create cache directory: " + cacheDirectory);
        }
        return cacheDirectory;
    }

    public ZonedDateTime lastModificationOf(String uniqueKey) {
        final ZonedDateTime lastModification;
        if (cacheFileExists(uniqueKey)) {
            final JsonElement lastModificationDate = deserializeCachedFile(uniqueKey).get(LAST_MODIFIED_PROPERTY);
            lastModification = gson.fromJson(lastModificationDate, ZonedDateTime.class);
        } else {
            lastModification = ZonedDateTime.of(2008, 4, 10, 0, 0, 0, 0, ZoneId.of("GMT"));
        }
        return lastModification;
    }

    public <T> T load(String uniqueKey, Class<T> type) {
        final JsonObject asset = deserializeCachedFile(uniqueKey).getAsJsonObject(ASSET_PROPERTY);
        return gson.fromJson(asset, type);
    }

    public boolean cacheFileExists(String uniqueKey){
        return Files.exists(Paths.get(createCachedFilePath(uniqueKey)));
    }

    public <T> void store(T asset, String uniqueKey, ZonedDateTime dateTime) {
        final String cachedFilePath = createCachedFilePath(uniqueKey);
        final JsonObject jsonObject = combineAsJson(asset, dateTime);
        try (FileOutputStream fileOutputStream = new FileOutputStream(cachedFilePath, false)) {
            fileOutputStream.write(jsonObject.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Unable to store " + jsonObject + "%n as [" + cachedFilePath + "]", e);
        }
    }

    private String createCachedFilePath(String uniqueKey) {
        return cacheDirectory.getAbsolutePath() + "/gh.cache." + uniqueKey + ".json";
    }

    private JsonObject deserializeCachedFile(String uniqueKey) {
        final String cachedFilePath = createCachedFilePath(uniqueKey);
        try (FileReader reader = new FileReader(cachedFilePath)) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize file [" + cachedFilePath + "]", e);
        }
    }

    private <T> JsonObject combineAsJson(T asset, ZonedDateTime dateTime) {
        final JsonElement assetAsJson = gson.toJsonTree(asset);
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add(LAST_MODIFIED_PROPERTY, gson.toJsonTree(dateTime));
        jsonObject.add(ASSET_PROPERTY, assetAsJson);
        return jsonObject;
    }

    private static class ZonedDateTimeConverter
        implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
        private static final DateTimeFormatter FORMATTER = Rfc2126DateTimeFormatter.INSTANCE;

        @Override
        public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(FORMATTER.format(src));
        }

        @Override
        public ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            return ZonedDateTime.parse(json.getAsString(), FORMATTER);
        }
    }
}
