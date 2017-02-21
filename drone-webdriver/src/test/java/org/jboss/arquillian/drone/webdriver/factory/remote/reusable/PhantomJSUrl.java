package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import org.jboss.arquillian.phantom.resolver.maven.PlatformUtils;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class PhantomJSUrl {

    public static String getPhantomJs211Url() {
        StringBuffer baseUrl = new StringBuffer("https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-");
        if (PlatformUtils.isMac()) {
            baseUrl.append("macosx.zip");
        } else if (PlatformUtils.isWindows()) {
            baseUrl.append("windows.zip");
        } else if (PlatformUtils.isUnix()) {
            baseUrl.append("linux-");
            if (PlatformUtils.is32()) {
                baseUrl.append("i686.tar.bz2");
            } else {
                baseUrl.append("x86_64.tar.bz2");
            }
        }
        return baseUrl.toString();
    }
}
