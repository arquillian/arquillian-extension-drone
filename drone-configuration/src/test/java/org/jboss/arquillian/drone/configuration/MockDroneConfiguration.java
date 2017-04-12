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
package org.jboss.arquillian.drone.configuration;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Sample configuration
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class MockDroneConfiguration implements DroneConfiguration<MockDroneConfiguration> {

    private String stringField;

    private int intField;

    private boolean booleanField;

    private long longField;

    private URL urlField;

    private URI uriField;

    private Integer integerField;

    private Map<String, Object> mapMap;

    private String browser;

    private String browserCapabilities;

    private String seleniumServerArgs;

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.DroneConfiguration#getConfigurationName()
     */
    public String getConfigurationName() {
        return "mockdrone";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.arquillian.drone.spi.DroneConfiguration#configure(org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor
     * , java.lang.Class)
     */
    @Override
    public MockDroneConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        return ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public boolean isBooleanField() {
        return booleanField;
    }

    public void setBooleanField(boolean booleanField) {
        this.booleanField = booleanField;
    }

    public Map<String, Object> getMapMap() {
        return mapMap;
    }

    public void setMapMap(Map<String, Object> mapMap) {
        this.mapMap = mapMap;
    }

    public long getLongField() {
        return longField;
    }

    public void setLongField(long longField) {
        this.longField = longField;
    }

    public URL getUrlField() {
        return urlField;
    }

    public void setUrlField(URL urlField) {
        this.urlField = urlField;
    }

    public URI getUriField() {
        return uriField;
    }

    public void setUriField(URI uriField) {
        this.uriField = uriField;
    }

    public Integer getIntegerField() {
        return integerField;
    }

    public void setIntegerField(Integer integerField) {
        this.integerField = integerField;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserCapabilities() {
        return browserCapabilities;
    }

    public void setBrowserCapabilities(String browserCapabilities) {
        this.browserCapabilities = browserCapabilities;
    }

    public String getSeleniumServerArgs() {
        return seleniumServerArgs;
    }

    public void setSeleniumServerArgs(String seleniumServerArgs) {
        this.seleniumServerArgs = seleniumServerArgs;
    }
}
