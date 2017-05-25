package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.Rfc2126DateTimeFormatter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.apache.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.apache.http.HttpHeaders.LAST_MODIFIED;

/**
 * GitHub source is an abstract class that helps you to retrieve either latest release or a release with some version
 * from some specific repository.
 */
public abstract class GitHubSource implements ExternalBinarySource {

    private static final String LATEST_URL = "/releases/latest";
    private static final String RELEASES_URL = "/releases";

    private static final Logger log = Logger.getLogger(GitHubSource.class.toString());
    private static final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final GitHubLastUpdateCache cache;
    private final String projectUrl;
    private final String uniqueKey;
    // JSON keys
    private String tagNameKey = "tag_name";
    private String assetNameKey = "name";
    private String browserDownloadUrlKey = "browser_download_url";
    private String assetsKey = "assets";

    /**
     * @param organization
     *     GitHub organization/user name the project belongs to
     * @param project
     *     GitHub project name
     */
    public GitHubSource(String organization, String project, HttpClient httpClient,
        GitHubLastUpdateCache gitHubLastUpdateCache) {
        this.httpClient = httpClient;
        this.projectUrl = String.format("https://api.github.com/repos/%s/%s", organization, project);
        this.uniqueKey = organization + "@" + project;
        this.cache = gitHubLastUpdateCache;
    }

    @Override
    public ExternalBinary getLatestRelease() throws Exception {
        final HttpClient.Response response =
            sentGetRequestWithPagination(projectUrl + LATEST_URL, 1, lastModificationHeader());
        final ExternalBinary binaryRelease;

        if (response.hasPayload()) {
            final JsonObject latestRelease = gson.fromJson(response.getPayload(), JsonElement.class).getAsJsonObject();
            String tagName = latestRelease.get(tagNameKey).getAsString();
            binaryRelease = new ExternalBinary(tagName);
            binaryRelease.setUrl(findReleaseBinaryUrl(latestRelease, binaryRelease.getVersion()));
            cache.store(binaryRelease, uniqueKey, extractModificationDate(response));
        } else {
            binaryRelease = cache.load(uniqueKey, ExternalBinary.class);
        }
        return binaryRelease;
    }

    protected Map<String, String> lastModificationHeader() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(IF_MODIFIED_SINCE, cache.lastModificationOf(uniqueKey)
            .withZoneSameInstant(ZoneId.of("GMT"))
            .format(Rfc2126DateTimeFormatter.INSTANCE));
        return headers;
    }

    protected ZonedDateTime extractModificationDate(HttpClient.Response response) {
        final String modificationDate = response.getHeader(LAST_MODIFIED);
        final DateTimeFormatter dateTimeFormatter = Rfc2126DateTimeFormatter.INSTANCE;
        return ZonedDateTime.parse(modificationDate, dateTimeFormatter);
    }

    @Override
    public ExternalBinary getReleaseForVersion(String version) throws Exception {
        String url = projectUrl + RELEASES_URL;
        int pageNumber = 1;
        List<String> availableVersions = new ArrayList<>();
        JsonElement releases = getReleasesJson(url, pageNumber);

        while (containsSubElements(releases)) {

            ExternalBinary releaseForVersion =
                getReleaseForVersion(version, releases.getAsJsonArray(), availableVersions);

            if (releaseForVersion != null) {
                return releaseForVersion;
            }
            releases = getReleasesJson(url, ++pageNumber);
        }

        throw new IllegalArgumentException(
            "No release matching version " + version + " has been found in the repository" + projectUrl
                + " Available versions are: " + availableVersions);
    }

    private boolean containsSubElements(JsonElement releases) {
        return releases != null && releases.isJsonArray() && releases.getAsJsonArray().size() > 0;
    }

    private JsonElement getReleasesJson(String url, int pageNumber) throws Exception {
        HttpClient.Response response = sentGetRequestWithPagination(url, pageNumber, Collections.emptyMap());
        return gson.fromJson(response.getPayload(), JsonElement.class);
    }

    private ExternalBinary getReleaseForVersion(String version, JsonArray releases, List<String> availableVersions)
        throws Exception {
        for (JsonElement release : releases) {
            JsonObject releaseObject = release.getAsJsonObject();
            String releaseTagName = releaseObject.get(tagNameKey).getAsString();

            if (version.equals(releaseTagName)) {
                final ExternalBinary binaryRelease = new ExternalBinary(releaseTagName);
                binaryRelease.setUrl(findReleaseBinaryUrl(releaseObject, binaryRelease.getVersion()));
                return binaryRelease;
            } else {
                availableVersions.add(releaseTagName);
            }
        }
        return null;
    }

    protected String findReleaseBinaryUrl(JsonObject releaseObject, String version) throws Exception {
        final JsonArray assets = releaseObject.get(assetsKey).getAsJsonArray();
        for (JsonElement asset : assets) {
            JsonObject assetJson = asset.getAsJsonObject();
            String name = assetJson.get(assetNameKey).getAsString();
            if (name.matches(getFileNameRegexToDownload(version))) {
                return assetJson.get(browserDownloadUrlKey).getAsString();
            }
        }
        return null;
    }

    protected HttpClient.Response sentGetRequestWithPagination(String url, int pageNumber, Map<String, String> headers)
        throws Exception {
        final URIBuilder uriBuilder = new URIBuilder(url);
        if (pageNumber != 1) {
            uriBuilder.setParameter("page", String.valueOf(pageNumber));
        }
        return httpClient.get(uriBuilder.build().toString(), headers);
    }

    protected String getProjectUrl() {
        return projectUrl;
    }

    protected Gson getGson() {
        return gson;
    }

    protected String getUniqueKey() {
        return uniqueKey;
    }

    protected GitHubLastUpdateCache getCache() {
        return cache;
    }
}
