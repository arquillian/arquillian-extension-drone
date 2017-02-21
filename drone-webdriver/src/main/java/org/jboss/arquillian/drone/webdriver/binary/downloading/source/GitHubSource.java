package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.apache.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.apache.http.HttpHeaders.LAST_MODIFIED;

/**
 * GitHub source is an abstract class that helps you to retrieve either latest release or a release with some version
 * from some specific repository.
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public abstract class GitHubSource implements ExternalBinarySource {

    private static final String LATEST_URL = "/releases/latest";
    private static final String RELEASES_URL = "/releases";

    private static final Logger log = Logger.getLogger(GitHubSource.class.toString());

    // JSON keys
    private String tagNameKey = "tag_name";
    private String assetNameKey = "name";
    private String browserDownloadUrlKey = "browser_download_url";
    private String assetsKey = "assets";

    private final HttpClient httpClient;
    private final GitHubLastUpdateCache cache;
    private final Gson gson;
    private final String projectUrl;
    private final String uniqueKey;

    /**
     * @param organization GitHub organization/user name the project belongs to
     * @param project      GitHub project name
     */
    public GitHubSource(String organization, String project, HttpClient httpClient, GitHubLastUpdateCache gitHubLastUpdateCache) {
        this.httpClient = httpClient;
        this.projectUrl = String.format("https://api.github.com/repos/%s/%s", organization, project);
        this.uniqueKey = organization + "@" + project;
        this.gson = new Gson(); // TODO think if that should be really a field
        this.cache = gitHubLastUpdateCache;
    }

    /**
     * It is expected that this abstract method should return a regex that represents an expected file name
     * of the release asset. These names are visible on pages of some specific release or accessible
     * via api.github request.
     *
     * @return A regex that represents an expected file name of an asset associated with the required release.
     */
    protected abstract String getExpectedFileNameRegex(String version);

    @Override
    public ExternalBinary getLatestRelease() throws Exception {
        final HttpClient.Response response = sentGetRequestWithPagination(projectUrl + LATEST_URL, 1, lastModificationHeader());

        final ExternalBinary binaryRelease;

        if (response.hasPayload()) {
            final JsonObject latestRelease = gson.fromJson(response.getPayload(), JsonElement.class).getAsJsonObject();
            String tagName = latestRelease.get(tagNameKey).getAsString();
            binaryRelease = new ExternalBinary(tagName);
            binaryRelease.setUrl(findReleaseBinaryUrl(latestRelease, binaryRelease.getVersion()));
            cache.store(binaryRelease, this.uniqueKey, extractModificationDate(response));
        } else {
            binaryRelease = cache.load(uniqueKey, ExternalBinary.class);
        }
        return binaryRelease;
    }

    private Map<String, String> lastModificationHeader() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(IF_MODIFIED_SINCE, cache.lastModificationOf(this.uniqueKey).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        return headers;
    }

    private ZonedDateTime extractModificationDate(HttpClient.Response response) {
        final String modificationDate = response.getHeader(LAST_MODIFIED);
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        return ZonedDateTime.parse(modificationDate, dateTimeFormatter);
    }

    @Override
    public ExternalBinary getReleaseForVersion(String version) throws Exception {
        final JsonArray releases = sentGetRequest(projectUrl + RELEASES_URL, Collections.emptyMap(), true).getAsJsonArray();

        if (releases != null) {

            for (JsonElement release : releases) {
                JsonObject releaseObject = release.getAsJsonObject();
                String releaseTagName = releaseObject.get(tagNameKey).getAsString();

                if (version.equals(releaseTagName)) {
                    final ExternalBinary binaryRelease = new ExternalBinary(releaseTagName);
                    binaryRelease.setUrl(findReleaseBinaryUrl(releaseObject, binaryRelease.getVersion()));
                    return binaryRelease;
                }
            }
            log.warning("There wasn't found any release for the version: " + version + " in the repository: " + projectUrl);
        }
        return null;
    }

    private String findReleaseBinaryUrl(JsonObject releaseObject, String version) throws Exception {
        final JsonArray assets = releaseObject.get(assetsKey).getAsJsonArray();
        for (JsonElement asset : assets) {
            JsonObject assetJson = asset.getAsJsonObject();
            String name = assetJson.get(assetNameKey).getAsString();
            if (name.matches(getExpectedFileNameRegex(version))) {
                return assetJson.get(browserDownloadUrlKey).getAsString();
            }
        }
        return null;
    }

    private JsonElement sentGetRequest(String url, Map<String, String> headers, boolean withPagination) throws Exception {

        final HttpClient.Response response = sentGetRequestWithPagination(url, 1, headers);
        JsonElement result = gson.fromJson(response.getPayload(), JsonElement.class);

        if (result != null && result.isJsonArray()) {
            JsonArray resultArray = result.getAsJsonArray();
            int i = 2;
            while (true) {
                final HttpClient.Response nextResponse = sentGetRequestWithPagination(url, i, headers);
                JsonArray page = gson.fromJson(nextResponse.getPayload(), JsonArray.class);
                if (page.size() == 0) {
                    break;
                }
                resultArray.addAll(page);
                if (!withPagination) {
                    break;
                }
                i++;
            }
            return resultArray;
        }
        return result;
    }

    private HttpClient.Response sentGetRequestWithPagination(String url, int pageNumber, Map<String, String> headers) throws Exception {
        final URI uri = new URIBuilder(url).setParameter("page", String.valueOf(pageNumber)).build();
        return httpClient.get(uri.toString(), headers);
    }
}
