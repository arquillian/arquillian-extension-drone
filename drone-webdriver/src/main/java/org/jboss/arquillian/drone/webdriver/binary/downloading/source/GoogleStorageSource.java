package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * GoogleStorageSource source is an abstract class that helps you to retrieve either latest release
 * or a release with some version from some specific google storage.
 * Eg. http://selenium-release.storage.googleapis.com/ for selenium bits or https://chromedriver.storage.googleapis.com/
 * for chrome web-drivers
 */
public abstract class GoogleStorageSource implements ExternalBinarySource {

    private Logger log = Logger.getLogger(GoogleStorageSource.class.toString());

    private HttpClient httpClient;
    private String storageUrl;
    private String urlToLatestRelease;

    private ArrayList<Content> contents;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private String latestVersion;

    /**
     * @param storageUrl
     *     An url to a google storage from which information about releases should be retrieved from
     */
    public GoogleStorageSource(String storageUrl, HttpClient httpClient) {
        this.storageUrl = storageUrl;
        this.httpClient = httpClient;
    }

    /**
     * @param storageUrl
     *     An url to a google storage from which information about releases should be retrieved from
     * @param urlToLatestRelease
     *     An url where a version of the latest release could be retrieved from
     */
    public GoogleStorageSource(String storageUrl, String urlToLatestRelease, HttpClient httpClient) {
        this(storageUrl, httpClient);
        this.urlToLatestRelease = urlToLatestRelease;
    }

    @Override
    public ExternalBinary getLatestRelease() throws Exception {
        if (urlToLatestRelease != null) {
            latestVersion = StringUtils.trimMultiline(httpClient.get(urlToLatestRelease).getPayload());
        } else {
            retrieveContents();
        }
        return getReleaseForVersion(latestVersion);
    }

    private void retrieveContents() throws Exception {
        if (contents == null) {
            contents = new ArrayList<>();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(httpClient.get(storageUrl).getPayload()));
            Document doc = db.parse(is);
            NodeList contentNodes = ((Element) doc.getFirstChild()).getElementsByTagName("Contents");
            for (int i = 0; i < contentNodes.getLength(); i++) {
                Element item = (Element) contentNodes.item(i);
                Content content = new Content();
                String key = getContentOfFirstElement(item, "Key");
                if (key.contains("/")) {
                    content.setKey(key);
                    content.setLastModified(getContentOfFirstElement(item, "LastModified"));
                    content.setDirectory(key.substring(0, key.indexOf("/")));
                    contents.add(content);
                }
            }
        }
    }

    private Date parseDate(String date, String key) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            log.warning("Date " + date + " of content " + storageUrl + key
                + " could not have been parsed. This content will be omitted. See the exception msg: "
                + e.getMessage());
        }
        return null;
    }

    private String getContentOfFirstElement(Element element, String tagName) {
        NodeList elementsByTagName = element.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() == 0) {
            return "";
        }
        return elementsByTagName.item(0).getTextContent();
    }

    @Override
    public ExternalBinary getReleaseForVersion(String requiredVersion) throws Exception {
        retrieveContents();
        List<Content> matched = contents
            .stream()
            .filter(content -> content.getKey().matches(getExpectedKeyRegex(requiredVersion, content.getDirectory())))
            .collect(Collectors.toList());

        if (matched.size() == 0) {
            throw new IllegalStateException(
                "There wasn't found any binary with the key matching regex "
                    + getExpectedKeyRegex(requiredVersion, "directory") + " in the storage: " + storageUrl);
        }

        if (requiredVersion != null) {
            return new ExternalBinary(requiredVersion, storageUrl + matched.get(0).getKey());
        } else {
            Content latestContent = findLatestContent(matched);
            return new ExternalBinary(latestContent.getDirectory(), storageUrl + latestContent.getKey());
        }
    }

    private Content findLatestContent(List<Content> matched) {
        return matched.stream()
            .sorted((c1, c2) -> {
                Date c1Date = parseDate(c1.getLastModified(), c1.getKey());
                Date c2Date = parseDate(c2.getLastModified(), c2.getKey());
                if (c1Date == null) return -1;
                if (c2Date == null) return 1;
                return Long.compare(c2Date.getTime(), c1Date.getTime());
            })
            .findFirst().get();
    }

    /**
     * It is expected that this abstract method should return a regex that represents a key of an expected binary/file
     * stored in the google storage. Key consist of <code>directory_name + / + file_name</code>. Keys are visible on
     * the root of the google storage url (without index.html suffix)
     *
     * @param requiredVersion
     *     The required version set using method {@link GoogleStorageSource#getReleaseForVersion},
     *     or otherwise null
     * @param directory
     *     The directory for the current binary the regex is being matched against
     *
     * @return A regex that represents a key of an expected binary/file stored in the google storage.
     */
    protected abstract String getExpectedKeyRegex(String requiredVersion, String directory);

    class Content {
        private String key;
        private String directory;
        private String lastModified;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }
}
