package org.jboss.arquillian.drone.selenium.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration;

public class SystemEnvHolder {

    private String proxyHost = SecurityActions.getProperty("http.proxyHost");
    private String proxyPort = SecurityActions.getProperty("http.proxyPort");
    private String nonProxyHosts = SecurityActions.getProperty("http.nonProxyHosts");
    private String truststore = SecurityActions.getProperty("javax.net.ssl.trustStore");
    private String truststorePass = SecurityActions.getProperty("javax.net.ssl.trustStorePassword");

    public void modifyEnvBy(SeleniumServerConfiguration configuration) {

        String proxyHost = configuration.getProxyHost();
        if (proxyHost != null) {
            SecurityActions.setProperty("http.proxyHost", proxyHost);
        }

        String proxyPort = configuration.getProxyPort();
        if (proxyPort != null) {
            SecurityActions.setProperty("http.proxyPort", proxyPort);
        }

        String nonProxyHosts = configuration.getNonProxyHosts();
        if (nonProxyHosts != null) {
            SecurityActions.setProperty("http.nonProxyHosts", nonProxyHosts);
        }

        String trustStore = configuration.getTrustStore();
        if (Validate.isNotNullOrEmpty(trustStore)) {
            Validate.isValidFile(trustStore, "Truststore file must exist: " + trustStore);
            SecurityActions.setProperty("javax.net.ssl.trustStore", trustStore);
        }

        String trustStorePass = configuration.getTrustStorePassword();
        if (trustStorePass != null) {
            SecurityActions.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
        }

        String systemProperties = configuration.getSystemProperties();

        if (systemProperties != null) {
            for (String systemProperty : getSystemProperties(systemProperties)) {
                String property = systemProperty.replaceFirst("-D", "").replaceFirst("=.*", "");
                String value = systemProperty.replaceFirst("[^=]*=", "");
                SecurityActions.setProperty(property, value);
            }
        }
    }

    private List<String> getSystemProperties(String valueString) {
        List<String> properties = new ArrayList<String>();

        // FIXME this should accept properties encapsulated in quotes as well
        StringTokenizer tokenizer = new StringTokenizer(valueString, " ");
        while (tokenizer.hasMoreTokens()) {
            String property = tokenizer.nextToken().trim();

            if (property.indexOf('=') == -1) {
                continue;
            }

            properties.add(property);
        }

        return properties;
    }

    public void restore() {
        SecurityActions.setProperty("http.proxyHost", proxyHost);
        SecurityActions.setProperty("http.proxyPort", proxyPort);
        SecurityActions.setProperty("http.nonProxyHosts", nonProxyHosts);
        SecurityActions.setProperty("javax.net.ssl.trustStore", truststore);
        SecurityActions.setProperty("javax.net.ssl.trustStorePassword", truststorePass);
    }

}