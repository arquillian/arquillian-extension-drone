package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * A slightly changed {@link GitHubSource} implementation handling PhantomJS binaries. The latest version is retrieved
 * from list of GH tags and download URL is constructed to use Bitbucket downloads storage.
 */
public class PhantomJSGitHubBitbucketSource extends GitHubSource {

    private static String BASE_DOWNLOAD_URL = "https://bitbucket.org/ariya/phantomjs/downloads/";

    private static String lastPhantomJSRelease = "2.1.1";

    public PhantomJSGitHubBitbucketSource(HttpClient httpClient, GitHubLastUpdateCache gitHubLastUpdateCache,
        DesiredCapabilities capabilities) {
        super("ariya", "phantomjs", httpClient, gitHubLastUpdateCache, capabilities);
    }

    /**
     * As there was announced the end of the development of PhantomJS:
     * https://groups.google.com/forum/#!msg/phantomjs/9aI5d-LDuNE/5Z3SMZrqAQAJ
     * it is not necessary to check the latest release - it is expected that there won't be any newer than the last
     * one: 2.1.1
     * If the development of PhantomJS is resurrected, then the original logic will be brought back. In this case
     * as a reference use this logic:
     * https://github.com/arquillian/arquillian-extension-drone/blob/5f4f64146dfbb42b641464dfb89aaa811b008a31/drone-webdriver/src/main/java/org/jboss/arquillian/drone/webdriver/binary/downloading/source/PhantomJSGitHubBitbucketSource.java#L30-L51
     * or The class that is currently used for testing purposes: GitHubSourceLatestReleaseFromTagsTestCase.PhantomJSSourceForLatestRelease
     */
    public ExternalBinary getLatestRelease() throws Exception {

        ExternalBinary lastPhantomJSVersion = new ExternalBinary(lastPhantomJSRelease);
        lastPhantomJSVersion.setUrl(getUrlForVersion(lastPhantomJSRelease));

        return lastPhantomJSVersion;
    }

    @Override
    public ExternalBinary getReleaseForVersion(String version) throws Exception {
        ExternalBinary phantomJSBinary = new ExternalBinary(version);
        phantomJSBinary.setUrl(getUrlForVersion(version));
        return phantomJSBinary;
    }

    protected String getUrlForVersion(String version) {
        return BASE_DOWNLOAD_URL + getFileNameRegexToDownload(version);
    }

    @Override
    public String getFileNameRegexToDownload(String version) {
        StringBuilder phantomFilenameZip = new StringBuilder("phantomjs-");
        phantomFilenameZip.append(version).append("-");

        if (PlatformUtils.isMac()) {
            phantomFilenameZip.append("macosx.zip").toString();
        } else if (PlatformUtils.isWindows()) {
            phantomFilenameZip.append("windows.zip");
        } else {
            phantomFilenameZip.append("linux-");
            if (PlatformUtils.is32()) {
                phantomFilenameZip.append("i686.tar.bz2").toString();
            } else {
                phantomFilenameZip.append("x86_64.tar.bz2").toString();
            }
        }
        return phantomFilenameZip.toString();
    }
}

