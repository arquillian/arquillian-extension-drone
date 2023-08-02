package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.Rfc2126DateTimeFormatter;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;

import static org.apache.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.apache.http.HttpHeaders.LAST_MODIFIED;

/**
 * GitHub source is an abstract class that helps you to retrieve either latest release or a release with some version
 * from some specific repository.
 */
public abstract class GitHubSource implements ExternalBinarySource {

    public static final String GITHUB_USERNAME_PROPERTY = "githubUsername";
    public static final String GITHUB_TOKEN_PROPERTY = "githubToken";
    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    public static final String BASIC_AUTHORIZATION_HEADER_VALUE_PREFIX = "Basic ";

    private static final String LATEST_URL = "/releases/latest";
    private static final String RELEASES_URL = "/releases";

    private static final Logger log = Logger.getLogger(GitHubSource.class.toString());
    private static final Gson gson = new Gson();
    private static final String HEADER_X_RATELIMIT_RESET = "X-RateLimit-Reset";
    private static final String HEADER_X_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
    private final HttpClient httpClient;
    private final GitHubLastUpdateCache cache;
    private final String projectUrl;
    private final String uniqueKey;
    private String username;
    private String token;
    // JSON keys
    private String tagNameKey = "tag_name";
    private String assetNameKey = "name";
    private String browserDownloadUrlKey = "browser_download_url";
    private String assetsKey = "assets";

    public GitHubSource(String organization, String project, HttpClient httpClient,
        GitHubLastUpdateCache gitHubLastUpdateCache) {
        this.httpClient = httpClient;
        this.projectUrl = String.format("https://api.github.com/repos/%s/%s", organization, project);
        this.uniqueKey = organization + "@" + project;
        this.cache = gitHubLastUpdateCache;
    }

    public GitHubSource(String organization, String project, HttpClient httpClient,
        GitHubLastUpdateCache gitHubLastUpdateCache, Capabilities capabilities) {
        this(organization, project, httpClient, gitHubLastUpdateCache);
        username = (String) capabilities.getCapability(GITHUB_USERNAME_PROPERTY);
        token = (String) capabilities.getCapability(GITHUB_TOKEN_PROPERTY);
    }

    @Override
    public ExternalBinary getLatestRelease() throws Exception {
        final HttpClient.Response response =
            sentGetRequestWithPagination(projectUrl + LATEST_URL, 1, lastModificationHeader());

        if (response.hasPayload()) {
            return processResponsePayload(response);
        } else {
            return cache.load(uniqueKey, ExternalBinary.class);
        }
    }

    private ExternalBinary processResponsePayload(HttpClient.Response response) throws Exception {
        final ExternalBinary binaryRelease;
        JsonElement jsonElement = gson.fromJson(response.getPayload(), JsonElement.class);
        final JsonObject latestRelease = jsonElement.getAsJsonObject();
        JsonElement tagNameElement = latestRelease.get(tagNameKey);

        if (tagNameElement == null) {
            binaryRelease = processEmptyResponse(response);
        } else {
            String tagName = tagNameElement.getAsString();
            binaryRelease = new ExternalBinary(tagName);
            binaryRelease.setUrl(findReleaseBinaryUrl(latestRelease, binaryRelease.getVersion()));
            cache.store(binaryRelease, uniqueKey, extractModificationDate(response));
        }
        return binaryRelease;
    }

    private ExternalBinary processEmptyResponse(HttpClient.Response response) throws Exception {
        StringBuffer msg = createErrorMessage(response, true);
        if (cache.cacheFileExists(uniqueKey)) {
            ExternalBinary binaryRelease = cache.load(uniqueKey, ExternalBinary.class);
            msg.append(" It will be used the cached version as the latest one: " + binaryRelease.getVersion());
            log.warning(msg.toString());
            return binaryRelease;
        } else {
            throw new IllegalStateException(msg.toString());
        }
    }

    private StringBuffer createErrorMessage(HttpClient.Response response, boolean latest) {
        StringBuffer msg = new StringBuffer();
        if ("0".equals(response.getHeader(HEADER_X_RATELIMIT_REMAINING))) {
            msg.append("GitHub API rate limit exceeded. To get the information about the ");
            if (latest) {
                msg.append("latest ");
            }
            msg.append("release you need to wait till the rate limit is reset");
            try {
                Date resetTime = new Date(Long.valueOf(response.getHeader(HEADER_X_RATELIMIT_RESET)) * 1000L);
                msg.append(" which will be: " + resetTime);
            } catch (NumberFormatException e) {
            }
            msg.append(".");
        } else {
            msg.append("There is some problem on GitHub server. It responded with: " + response.getPayload() + "\n");
        }
        return msg;
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
        if (modificationDate != null) {
            final DateTimeFormatter dateTimeFormatter = Rfc2126DateTimeFormatter.INSTANCE;
            return ZonedDateTime.parse(modificationDate, dateTimeFormatter);
        }
        return ZonedDateTime.now();
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
        if (availableVersions.isEmpty()) {
            throw new IllegalStateException(
                createErrorMessage(sentGetRequestWithPagination(url, 1, new HashMap<>()), false).toString());
        } else {
            throw new IllegalArgumentException(
                "No release matching version " + version + " has been found in the repository " + projectUrl
                    + " Available versions are: " + availableVersions + ".");
        }
    }

    private boolean containsSubElements(JsonElement releases) {
        return releases != null && releases.isJsonArray() && releases.getAsJsonArray().size() > 0;
    }

    private JsonElement getReleasesJson(String url, int pageNumber) throws Exception {
        HttpClient.Response response = sentGetRequestWithPagination(url, pageNumber, new HashMap<>());
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
        addAuthParams(headers);
        return httpClient.get(uriBuilder.build().toString(), headers);
    }

    private void addAuthParams(Map<String, String> headers) {
        if (Validate.nonEmpty(username) && Validate.nonEmpty(token)) {
            String authParam = Base64.getEncoder().encodeToString((username + ":" + token).getBytes());
            headers.put(AUTHORIZATION_HEADER_KEY, BASIC_AUTHORIZATION_HEADER_VALUE_PREFIX + authParam);
        }
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
