/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.io.Serializable;
import java.net.URL;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;

public class InitializationParameter implements Serializable {
    private static final long serialVersionUID = 7249154351129725606L;

    private final URL url;

    private final ReusableCapabilities capabilities;

    public InitializationParameter(URL url, Capabilities Capabilities) {
        this.url = url;
        // we need to identify what capabilities cannot be serialized/deserialized and reject those from Initialization key
        this.capabilities = ReusedSession.createReusableCapabilities(Capabilities);
    }

    public URL getUrl() {
        return url;
    }

    public Capabilities getCapabilities() {
        return new ImmutableCapabilities(capabilities.getCapabilities());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InitializationParameter other = (InitializationParameter) obj;
        if (capabilities == null) {
            if (other.capabilities != null) {
                return false;
            }
        } else if (!capabilities.equals(other.capabilities)) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return url.toString() + "#" + capabilities.toString();
    }
}
