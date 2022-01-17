package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.io.IOException;
import java.util.logging.Logger;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;

/**
 * UrlStorageSource source is an abstract class that helps you to retrieve either latest release
 * or a release with some version from storage, that has binaries available via url in a format:
 * [storage_base_url]/[version]/[binary_file_name]
 *
 * The UrlStorageSource also must provide and URL to obtain est available binary version
 */
public abstract class UrlStorageSource implements ExternalBinarySource {

    private Logger log = Logger.getLogger(UrlStorageSource.class.toString());

    private HttpClient httpClient;

    protected String nodeName;

    protected String fileName;

    protected String urlToLatestRelease;

    private String storageUrl;

    private String latestVersion;

    public UrlStorageSource(String nodeName, String fileName, String storageUrl, String urlToLatestRelease, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.nodeName = nodeName;
        this.fileName = fileName;
        this.storageUrl = storageUrl;
        this.urlToLatestRelease = urlToLatestRelease;
    }

    /**
     * @param storageUrl An url to a storage from which information about releases should be retrieved from
     */
    public UrlStorageSource(String storageUrl, HttpClient httpClient) {
        this("Contents", "Key", storageUrl, null, httpClient);
    }

    /**
     * @param storageUrl         An url to a storage from which information about releases should be retrieved from
     * @param urlToLatestRelease An url where a version of the latest release could be retrieved from
     */
    public UrlStorageSource(String storageUrl, String urlToLatestRelease, HttpClient httpClient) {
        this("Contents", "Key", storageUrl, urlToLatestRelease, httpClient);
    }

    @Override
    public ExternalBinary getLatestRelease() throws Exception {
        return getLatestRelease("UTF-8");
    }

    protected ExternalBinary getLatestRelease(String charset) throws Exception {
        if (urlToLatestRelease != null) {
            latestVersion = getVersion(urlToLatestRelease, charset);
        }
        return getReleaseForVersion(latestVersion);
    }

    protected String getVersion(String versionUrl, String charset) throws IOException {
        return StringUtils.trimMultiline(httpClient.get(versionUrl, charset).getPayload());
    }

    @Override
    public ExternalBinary getReleaseForVersion(String requiredVersion) throws Exception {
        return getReleaseForVersion(requiredVersion, Architecture.AUTO_DETECT);
    }

    @Override
    public ExternalBinary getReleaseForVersion(String requiredVersion, Architecture architecture) throws Exception {
        final String externalBinaryUrl = storageUrl + getFileNameRegexToDownload(requiredVersion, architecture);

        final HttpClient.Response response = httpClient.get(externalBinaryUrl);
        if (response.getStatusCode() != 200) {
            log.warning("There wasn't found any binary on the url: " + externalBinaryUrl);
            throw new MissingBinaryException("There wasn't found any binary on the url: " + externalBinaryUrl);
        }

        return new ExternalBinary(requiredVersion, externalBinaryUrl);
    }
}
