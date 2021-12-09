package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.input.BOMInputStream;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.Architecture;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * XmlStorageSource source is an abstract class that helps you to retrieve either latest release
 * or a release with some version from some specific XML-based storage, such as S3 buckets.
 * Eg. http://selenium-release.storage.googleapis.com/ for selenium bits or https://chromedriver.storage.googleapis.com/
 * for chrome web-drivers
 */
public abstract class XmlStorageSource implements ExternalBinarySource {

    private Logger log = Logger.getLogger(XmlStorageSource.class.toString());

    private HttpClient httpClient;

    protected String nodeName;

    protected String fileName;

    protected String urlToLatestRelease;

    private String storageUrl;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String latestVersion;

    public XmlStorageSource(String nodeName, String fileName, String storageUrl, String urlToLatestRelease, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.nodeName = nodeName;
        this.fileName = fileName;
        this.storageUrl = storageUrl;
        this.urlToLatestRelease = urlToLatestRelease;
    }

    /**
     * @param storageUrl An url to a xml storage from which information about releases should be retrieved from
     */
    public XmlStorageSource(String storageUrl, HttpClient httpClient) {
        this("Contents", "Key", storageUrl, null, httpClient);
    }

    /**
     * @param storageUrl         An url to a xml storage from which information about releases should be retrieved from
     * @param urlToLatestRelease An url where a version of the latest release could be retrieved from
     */
    public XmlStorageSource(String storageUrl, String urlToLatestRelease, HttpClient httpClient) {
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

    private List<DriverEntry> retrieveAllDriversEntries() throws Exception {
        final List<DriverEntry> results = new ArrayList<>();
        final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final InputSource is = new InputSource();
        is.setByteStream(new BOMInputStream(new ByteArrayInputStream(httpClient.get(storageUrl).getPayload().getBytes(StandardCharsets.UTF_8))));
        final Document doc = db.parse(is);
        final NodeList contentNodes = getDriverEntries(doc);
        for (int i = 0; i < contentNodes.getLength(); i++) {
            final Element driverNode = (Element) contentNodes.item(i);
            final DriverEntry driverEntry = new DriverEntry();
            final String key = getContentOfFirstElement(driverNode, this.fileName);
            if (key.contains("/")) {
                driverEntry.setKey(key);
                driverEntry.setLastModified(getLastModified(driverNode));
                driverEntry.setLocation(getLocation(driverNode));
                results.add(driverEntry);
            }
        }
        return results;
    }

    protected NodeList getDriverEntries(Document doc) {
        return ((Element) doc.getFirstChild()).getElementsByTagName(this.nodeName);
    }

    protected String getLastModified(Element element) {
        return getContentOfFirstElement(element, "LastModified");
    }

    protected String getLocation(Element element) {
        final String key = getContentOfFirstElement(element, this.fileName);
        return key.substring(0, key.indexOf("/"));
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

    protected String getContentOfFirstElement(Element element, String tagName) {
        NodeList elementsByTagName = element.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() == 0) {
            return "";
        }
        return elementsByTagName.item(0).getTextContent();
    }

    @Override
    public ExternalBinary getReleaseForVersion(String requiredVersion) throws Exception {
        return getReleaseForVersion(requiredVersion, Architecture.AUTO_DETECT);
    }

    @Override
    public ExternalBinary getReleaseForVersion(String requiredVersion, Architecture architecture) throws Exception {
        final List<DriverEntry> driverEntries = retrieveAllDriversEntries();
        final List<DriverEntry> matched = driverEntries
            .stream()
            .filter(driverEntry -> {
                final String expectedKeyRegex = getExpectedKeyRegex(requiredVersion, driverEntry.getLocation(), architecture);
                return driverEntry.getKey().matches(expectedKeyRegex);
            })
            .collect(Collectors.toList());

        if (matched.size() == 0) {
            throw new MissingBinaryException(
                "There wasn't found any binary with the key matching regex "
                    + getExpectedKeyRegex(requiredVersion, "directory") + " in the storage: " + storageUrl);
        }

        if (requiredVersion != null) {
            return new ExternalBinary(requiredVersion, storageUrl + matched.get(0).getKey());
        } else {
            final DriverEntry latestDriverEntry = findLatestDriver(matched);
            return new ExternalBinary(latestDriverEntry.getLocation(), storageUrl + latestDriverEntry.getKey());
        }
    }

    private DriverEntry findLatestDriver(List<DriverEntry> matched) {
        return matched.stream().min((c1, c2) -> {
            Date c1Date = parseDate(c1.getLastModified(), c1.getKey());
            Date c2Date = parseDate(c2.getLastModified(), c2.getKey());
            if (c1Date == null) return -1;
            if (c2Date == null) return 1;
            return Long.compare(c2Date.getTime(), c1Date.getTime());
        }).get();
    }

    /**
     * It is expected that this abstract method should return a regex that represents a key of an expected binary/file
     * stored in the xml storage. Commonly, key consists of <code>directory_name + / + file_name</code>.
     *
     * @param requiredVersion The required version set using method {@link XmlStorageSource#getReleaseForVersion},
     *                        or otherwise null
     * @param directory       The directory for the current binary the regex is being matched against
     * @return A regex that represents a key of an expected binary/file.
     */
    protected abstract String getExpectedKeyRegex(String requiredVersion, String directory);

    protected String getExpectedKeyRegex(String requiredVersion, String directory, Architecture architecture) {
        return getExpectedKeyRegex(requiredVersion, directory);
    }

    class DriverEntry {
        private String key;

        private String location;

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

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}
