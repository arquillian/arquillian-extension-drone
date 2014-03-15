/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.drone.selenium.server.configuration;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Configuration for Selenium Server. This configuration can be fetched from Arquillian Descriptor and overridden by System
 * properties.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see ConfigurationMapper
 *
 */
public class SeleniumServerConfiguration implements DroneConfiguration<SeleniumServerConfiguration> {
    public static final String CONFIGURATION_NAME = "selenium-server";

    private boolean avoidProxy = false;

    private boolean browserSessionReuse = false;

    private boolean browserSideLog = false;

    private boolean debug = false;

    private boolean dontTouchLogging = false;

    private boolean ensureCleanSession = false;

    private String firefoxProfileTemplate;

    private String forcedBrowserMode;

    private boolean honorSystemProxy = false;

    private String host = "localhost";

    private String logFile;

    private String nonProxyHosts = SecurityActions.getProperty("http.nonProxyHosts");

    private int port = 14444;

    private String profilesLocation;

    private String proxyHost = SecurityActions.getProperty("http.proxyHost");

    private boolean proxyInjectionMode = false;

    private String proxyPort = SecurityActions.getProperty("http.proxyPort");

    private int retryTimeoutInSeconds = 10;

    private boolean singleWindow = false;

    private boolean skip = false;

    private String systemProperties;

    private int timeoutInSeconds = Integer.MAX_VALUE;

    private boolean trustAllSSLCertificates = false;

    private String trustStore = SecurityActions.getProperty("javax.net.ssl.trustStore");

    private String trustStorePassword = SecurityActions.getProperty("javax.net.ssl.trustStorePassword");

    private String userExtensions;

    /**
     * Creates default Selenium Server Configuration
     */
    public SeleniumServerConfiguration() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.selenium.spi.WebTestConfiguration#configure(org.jboss
     * .arquillian.impl.configuration.api.ArquillianDescriptor, java.lang.Class)
     */
    public SeleniumServerConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        return ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.selenium.spi.WebTestConfiguration#getConfigurationName ()
     */
    public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }

    /**
     * Gets the port where Selenium server is listening for requests
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port where Selenium server is listening for requests
     *
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the host where Selenium server is listening for requests
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host where Selenium server is listening for requests
     *
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the name of log file where Selenium output is logged
     *
     * @return the output
     * @see SeleniumServerConfiguration.getLogFile()
     * @deprecated
     */
    @Deprecated
    public String getOutput() {
        return logFile;
    }

    /**
     * Sets the name of log file where Selenium output is logged
     *
     * @param output the output to set
     * @see SeleniumServerConfiguration.setLogFile(String)
     * @deprecated
     */
    @Deprecated
    public void setOutput(String output) {
        this.logFile = output;
    }

    /**
     * Gets the flag which enable running Selenium server
     *
     * @return the enable flag
     * @see SeleniumServerConfiguration.isSkip()
     * @deprecated
     */
    @Deprecated
    public boolean isEnable() {
        return !skip;
    }

    /**
     * Sets Selenium server start to be enabled
     *
     * @param enable the enable to set
     * @see SeleniumServerConfiguration.setSkip(boolean)
     * @deprecated
     */
    @Deprecated
    public void setEnable(boolean enable) {
        this.skip = !enable;
    }

    /**
     * Gets path to the log file
     *
     * @return the logFile
     */
    public String getLogFile() {
        return logFile;
    }

    /**
     * Sets the path to the log file
     *
     * @param logFile the logFile to set
     */
    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    /**
     * Gets flag marking Selenium server to avoid proxy
     *
     * @return the avoidProxy
     */
    public boolean isAvoidProxy() {
        return avoidProxy;
    }

    /**
     * Sets Selenium Server to avoid proxy server
     *
     * @param avoidProxy the avoidProxy to set
     */
    public void setAvoidProxy(boolean avoidProxy) {
        this.avoidProxy = avoidProxy;
    }

    /**
     * Reuse browser session between browsers
     *
     * @return the browserSessionReuse
     */
    public boolean isBrowserSessionReuse() {
        return browserSessionReuse;
    }

    /**
     * Sets browser session to be reused between browsers
     *
     * @param browserSessionReuse the browserSessionReuse to set
     */
    public void setBrowserSessionReuse(boolean browserSessionReuse) {
        this.browserSessionReuse = browserSessionReuse;
    }

    /**
     * Gets flag whether logging in browser window is enabled
     *
     * @return the browserSideLog
     */
    public boolean isBrowserSideLog() {
        return browserSideLog;
    }

    /**
     * Enables or disables logging on server side log
     *
     * @param browserSideLog the browserSideLog to set
     */
    public void setBrowserSideLog(boolean browserSideLog) {
        this.browserSideLog = browserSideLog;
    }

    /**
     * Gets debug flag
     *
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Enables or disabled debug messages
     *
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Sets logging not to be done by Selenium loggers
     *
     * @param dontTouchLogging the dontTouchLogging to set
     */
    public void setDontTouchLogging(boolean dontTouchLogging) {
        this.dontTouchLogging = dontTouchLogging;
    }

    /**
     * Gets logging type flag
     *
     * @return the dontTouchLogging
     */
    public boolean isDontTouchLogging() {
        return dontTouchLogging;
    }

    /**
     * Gets flag ensuring session is clean
     *
     * @return the ensureCleanSession
     */
    public boolean isEnsureCleanSession() {
        return ensureCleanSession;
    }

    /**
     * Forces or disabled cleaning browser session
     *
     * @param ensureCleanSession the ensureCleanSession to set
     */
    public void setEnsureCleanSession(boolean ensureCleanSession) {
        this.ensureCleanSession = ensureCleanSession;
    }

    /**
     * Gets the path to the directory where a Firefox profile is stored
     *
     * @return the firefoxProfileTemplate
     */
    public String getFirefoxProfileTemplate() {
        return firefoxProfileTemplate;
    }

    /**
     * Sets the path to the directory where a Firefox profile is stored.
     *
     * @param firefoxProfileTemplate the firefoxProfileTemplate to set
     */
    public void setFirefoxProfileTemplate(String firefoxProfileTemplate) {
        this.firefoxProfileTemplate = firefoxProfileTemplate;
    }

    /**
     * Gets enforced browser name
     *
     * @return the forcedBrowserMode
     */
    public String getForcedBrowserMode() {
        return forcedBrowserMode;
    }

    /**
     * Forces browser to be of given type
     *
     * @param forcedBrowserMode the forcedBrowserMode to set
     */
    public void setForcedBrowserMode(String forcedBrowserMode) {
        this.forcedBrowserMode = forcedBrowserMode;
    }

    /**
     * Sets Selenium server to use system proxy settings
     *
     * @param honorSystemProxy the honorSystemProxy to set
     */
    public void setHonorSystemProxy(boolean honorSystemProxy) {
        this.honorSystemProxy = honorSystemProxy;
    }

    /**
     * Gets flag whether Selenium server is honoring the system proxy
     *
     * @return the honorSystemProxy
     */
    public boolean isHonorSystemProxy() {
        return honorSystemProxy;
    }

    /**
     * Gets list of server not proxied, comma separated
     *
     * @return the nonProxyHosts
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    /**
     * Sets list (comma separated) of host where proxying is not applied
     *
     * @param nonProxyHosts the nonProxyHosts to set
     */
    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    /**
     * Gets location of the Firefox profiles
     *
     * @return the profilesLocation
     */
    public String getProfilesLocation() {
        return profilesLocation;
    }

    /**
     * Sets location of the Firefox profiles
     *
     * @param profilesLocation the profilesLocation to set
     */
    public void setProfilesLocation(String profilesLocation) {
        this.profilesLocation = profilesLocation;
    }

    /**
     * Gets host name of proxy
     *
     * @return the proxyHost
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Sets host name of proxy
     *
     * @param proxyHost the proxyHost to set
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * Allows proxy injection on Selenium server side
     *
     * @param proxyInjectionMode the proxyInjectionMode to set
     */
    public void setProxyInjectionMode(boolean proxyInjectionMode) {
        this.proxyInjectionMode = proxyInjectionMode;
    }

    /**
     * Gets proxy injection flag
     *
     * @return the proxyInjectionMode
     */
    public boolean isProxyInjectionMode() {
        return proxyInjectionMode;
    }

    /**
     * Gets port of the proxy
     *
     * @return the proxyPort
     */
    public String getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets port of the proxy
     *
     * @param proxyPort the proxyPort to set
     */
    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Sets timeout for retrying a request
     *
     * @param retryTimeoutInSeconds the retryTimeoutInSeconds to set
     */
    public void setRetryTimeoutInSeconds(int retryTimeoutInSeconds) {
        this.retryTimeoutInSeconds = retryTimeoutInSeconds;
    }

    /**
     * Gets timeout for retrying a request
     *
     * @return the retryTimeoutInSeconds
     */
    public int getRetryTimeoutInSeconds() {
        return retryTimeoutInSeconds;
    }

    /**
     * Gets flag whether Selenium server is using single window
     *
     * @return the singleWindow
     */
    public boolean isSingleWindow() {
        return singleWindow;
    }

    /**
     * Enforces server to use a single window
     *
     * @param singleWindow the singleWindow to set
     */
    public void setSingleWindow(boolean singleWindow) {
        this.singleWindow = singleWindow;
    }

    /**
     * Checks whether start of Selenium server is enabled
     *
     * @return the skip
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * Enables or disabled start of Selenium server
     *
     * @param skip the skip to set
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    /**
     * Sets additional system properties.
     *
     * <p>
     * This properties should be specified in format:
     *
     * <pre>
     * -Dfoo=bar -Dfoo2=bar2
     * </pre>
     *
     * </p>
     *
     * @param systemProperties the systemProperties to set
     */
    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
    }

    /**
     * Gets additional system properties
     *
     * @return the systemProperties
     */
    public String getSystemProperties() {
        return systemProperties;
    }

    /**
     * Sets global timeout for Selenium server.
     *
     * Default value is {@link Integer.MAX_VALUE}
     *
     * <strong>After this timeout all request will fail</strong>
     *
     * @param timeoutInSeconds the timeoutInSeconds to set
     */
    public void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    /**
     * Gets global timeout for Selenium server.
     *
     * @return the timeoutInSeconds
     */
    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    /**
     * Gets flag whether Selenium server is trusting all SSL certificates
     *
     * @return the trustAllSSLCertificates
     */
    public boolean isTrustAllSSLCertificates() {
        return trustAllSSLCertificates;
    }

    /**
     * Sets Selenium server to trust all certificates
     *
     * @param trustAllSSLCertificates the trustAllSSLCertificates to set
     */
    public void setTrustAllSSLCertificates(boolean trustAllSSLCertificates) {
        this.trustAllSSLCertificates = trustAllSSLCertificates;
    }

    /**
     * Gets path to the trust store
     *
     * @return the trustStore
     */
    public String getTrustStore() {
        return trustStore;
    }

    /**
     * Sets path to the trust store
     *
     * @param trustStore the trustStore to set
     */
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * Gets password to the trust store
     *
     * @return the trustStorePassword
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * Sets path to the trust store
     *
     * @param trustStorePassword the trustStorePassword to set
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Gets path to the file where user specified JavaScript extensions
     *
     * @return the userExtensions
     */
    public String getUserExtensions() {
        return userExtensions;
    }

    /**
     * Sets path to the file where user specified JavaScript extensions
     *
     * @param userExtensions the userExtensions to set
     */
    public void setUserExtensions(String userExtensions) {
        this.userExtensions = userExtensions;
    }

}
