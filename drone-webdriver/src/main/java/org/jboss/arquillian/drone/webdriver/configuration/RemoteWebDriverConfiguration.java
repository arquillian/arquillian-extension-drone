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
package org.jboss.arquillian.drone.webdriver.configuration;

import java.util.Map;

/**
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 */
public interface RemoteWebDriverConfiguration extends CommonWebDriverConfiguration {

    String getCapability(String name);

    Map<String,String> getCapabilities();

    Map<String, String> getCapabilities(String needle);

    String getRemoteAddress();

    boolean isRemote();

    boolean isRemoteReusable();

    void setCapability(String name, String value);

    void setRemote(boolean remote);

    void setRemoteAddress(String remoteAddress);

    void setRemoteReusable(boolean reusable);
}
