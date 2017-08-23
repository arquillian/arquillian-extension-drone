package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

public class GeckoDriverGitHubSource extends GitHubSource {

    public GeckoDriverGitHubSource(HttpClient httpClient, GitHubLastUpdateCache gitHubLastUpdateCache,
        DesiredCapabilities capabilities) {
        super("mozilla", "geckodriver", httpClient, gitHubLastUpdateCache, capabilities);
    }

    @Override
    public String getFileNameRegexToDownload(String version) {
        StringBuilder fileNameRegex = new StringBuilder("geckodriver-");
        fileNameRegex.append(version).append("-");
        if (PlatformUtils.isMac()) {
            fileNameRegex.append("macos").toString();
        } else {
            if (PlatformUtils.isWindows() || PlatformUtils.isUnix()) {
                if (PlatformUtils.isWindows()) {
                    fileNameRegex.append("win");
                } else {
                    fileNameRegex.append("linux");
                }
                if (PlatformUtils.is32()) {
                    fileNameRegex.append("32").toString();
                } else {
                    fileNameRegex.append("64").toString();
                }
            } else {
                fileNameRegex.append("arm7hf").toString();
            }
        }
        return fileNameRegex.append(".*").toString();
    }
}
