/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.selenium.configuration;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Configuration for Selenium. This configuration can be fetched from Arquillian Descriptor and overridden by System properties.
 *
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see ArquillianDescriptor
 * @see ConfigurationMapper
 *
 */
public class SeleniumConfiguration implements DroneConfiguration<SeleniumConfiguration> {
    public static final String CONFIGURATION_NAME = "selenium";

    private int serverPort = 14444;

    private String serverHost = "localhost";

    private String url = "http://localhost:8080";

    private int timeout = 60000;

    private int speed = 0;

    private String browser = "*firefox";

    /**
     * Creates default Selenium Configuration
     */
    public SeleniumConfiguration() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.selenium.spi.WebTestConfiguration#getConfigurationName ()
     */
    public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.selenium.spi.WebTestConfiguration#configure(org.jboss
     * .arquillian.impl.configuration.api.ArquillianDescriptor, java.lang.Class)
     */
    public SeleniumConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
        return ConfigurationMapper.fromSystemConfiguration(this, qualifier);
    }

    /**
     * A port where Selenium configuration is started/running
     *
     * @return the serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * @param serverPort the serverPort to set
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * The name of machine where Selenium configuration is started/running
     *
     * @return the serverHost
     */
    public String getServerHost() {
        return serverHost;
    }

    /**
     * @param serverHost the serverHost to set
     */
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    /**
     * Time limit in milliseconds which determines operation failed, either for executing Selenium command or starting Selenium
     * configuration
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Time delay in milliseconds before each Selenium command is sent
     *
     * @return the speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Identification of the browser for needs of Selenium.
     *
     * Use can use variants including path to binary, such as: <i>*firefoxproxy /opt/firefox-3.0/firefox</i>
     *
     * @return the browser
     */
    public String getBrowser() {
        return browser;
    }

    /**
     * @param browser the browser to set
     */
    public void setBrowser(String browser) {
        this.browser = browser;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
}
