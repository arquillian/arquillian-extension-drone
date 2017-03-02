package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

public abstract class HoverflyProxyHandler {

    // Cleaning up system properties for proxies
    // We need this until hoverfly cleans up properly
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";

    private static String ORG_HTTP_PROXY_HOST = "";
    private static String ORG_HTTPS_PROXY_HOST = "";
    private static String ORG_HTTP_NON_PROXY_HOSTS = "";
    private static String ORG_HTTP_PROXY_PORT = "";
    private static String ORG_HTTPS_PROXY_PORT = "";

    static { // @BeforeClass won't work due to order of junit execution - hoverfly would start first
        ORG_HTTP_PROXY_HOST = System.getProperty(HTTP_PROXY_HOST, "");
        ORG_HTTPS_PROXY_HOST = System.getProperty(HTTPS_PROXY_HOST, "");
        ORG_HTTP_NON_PROXY_HOSTS = System.getProperty(HTTP_NON_PROXY_HOSTS, "");
        ORG_HTTP_PROXY_PORT = System.getProperty(HTTP_PROXY_PORT, "");
        ORG_HTTPS_PROXY_PORT = System.getProperty(HTTPS_PROXY_PORT, "");
    }

    static void clean() {
        System.setProperty(HTTP_PROXY_HOST, ORG_HTTP_PROXY_HOST);
        System.setProperty(HTTPS_PROXY_HOST, ORG_HTTPS_PROXY_HOST);
        System.setProperty(HTTP_NON_PROXY_HOSTS, ORG_HTTP_NON_PROXY_HOSTS);
        System.setProperty(HTTP_PROXY_PORT, ORG_HTTP_PROXY_PORT);
        System.setProperty(HTTPS_PROXY_PORT, ORG_HTTPS_PROXY_PORT);
    }

}