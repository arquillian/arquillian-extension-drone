package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;
import org.openqa.selenium.Capabilities;

public class GeckoDriverGitHubSource extends GitHubSource {

    public GeckoDriverGitHubSource(HttpClient httpClient,
                                   GitHubLastUpdateCache gitHubLastUpdateCache,
                                   Capabilities capabilities) {
        super("mozilla", "geckodriver", httpClient, gitHubLastUpdateCache, capabilities);
    }

    @Override
    public String getFileNameRegexToDownload(String version) {
        StringBuilder fileNameRegex = new StringBuilder("^geckodriver-");
        fileNameRegex.append(version).append("-");
        if (PlatformUtils.isMac()) {
            fileNameRegex.append("macos");
            if (PlatformUtils.isMacAppleSilicon()) {
                fileNameRegex.append("-aarch64");
            }
        } else if (PlatformUtils.isWindows() || PlatformUtils.isLinux()) {
            if (PlatformUtils.isWindows()) {
                fileNameRegex.append("win");
            } else {
                fileNameRegex.append("linux");
            }
            if (PlatformUtils.is32()) {
                fileNameRegex.append("32");
            } else {
                fileNameRegex.append("64");
            }
        } else {
            throw new RuntimeException(
                String.format(
                    "GeckoDriver binary download is not available for os.name = %s, os.arch = %s",
                    System.getProperty("os.name"),
                    System.getProperty("os.arch")
                )
            );
        }
        return fileNameRegex.append("\\.(zip|tgz|tar\\.gz)$").toString();
    }

}
