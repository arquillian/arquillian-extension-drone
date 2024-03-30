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
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.drone.webdriver.factory.RemoteWebDriverFactory;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.SessionId;

/**
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class ReusedSession implements Serializable {

    private static final long serialVersionUID = -5332705183394774545L;

    private static final Logger log = Logger.getLogger(RemoteWebDriverFactory.class.getName());

    private String opaqueKey;
    private ReusableCapabilities capabilities;

    ReusedSession(SessionId sessionId, Capabilities capabilities) {
        this.opaqueKey = sessionId.toString();
        this.capabilities = createReusableCapabilities(capabilities);
    }

    @SuppressWarnings("unused") // used for deserialization
    ReusedSession(SessionId sessionId, ReusableCapabilities capabilities) {
        this.opaqueKey = sessionId.toString();
        this.capabilities = capabilities;
    }

    public static ReusedSession createInstance(SessionId sessionId, Capabilities capabilities) {
        ReusableCapabilities reusableCapabilities = createReusableCapabilities(capabilities);
        return new ReusedSession(sessionId, new MutableCapabilities(reusableCapabilities.getCapabilities()));
    }

    static ReusableCapabilities createReusableCapabilities(Capabilities driverCapabilities) {
        ReusableCapabilities capabilitiesForReuse = new ReusableCapabilities();
        for (Entry<String, ?> capability : driverCapabilities.asMap().entrySet()) {
            String key = capability.getKey();
            Object value = capability.getValue();
            if (value instanceof Serializable) {
                // here we try to Serialize object. Serialization utils will apply a whitelist, so it might fail even if
                // object was marked as Serializable. In such case we do not put the object into capabilities
                try {
                    SerializationUtils.serializeToBytes((Serializable) value);
                    capabilitiesForReuse.setCapability(capability.getKey(), capability.getValue());
                } catch (IOException e) {
                    log.log(Level.FINE,
                        "Capability {0} has unserializable value of type {1} and value {2}. Caused by {3}.",
                        new Object[] {
                            key,
                            value != null ? value.getClass().getName() : "unknown",
                            value,
                            e.getCause() == null ? e : e.getCause()});
                }
            } else {
                log.log(Level.FINE,
                    "Ignoring capability {0} of type {1} and value {2}, does not implement Serializable interface",
                    new Object[] {
                        key,
                        value != null ? value.getClass().getName() : "unknown",
                        value});
            }
        }
        return capabilitiesForReuse;
    }

    public SessionId getSessionId() {
        return new SessionId(opaqueKey);
    }

    public Capabilities getCapabilities() {
        return new MutableCapabilities(capabilities.getCapabilities());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
        result = prime * result + ((opaqueKey == null) ? 0 : opaqueKey.hashCode());
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
        ReusedSession other = (ReusedSession) obj;
        if (capabilities == null) {
            if (other.capabilities != null) {
                return false;
            }
        } else if (!capabilities.equals(other.capabilities)) {
            return false;
        }
        if (opaqueKey == null) {
            if (other.opaqueKey != null) {
                return false;
            }
        } else if (!opaqueKey.equals(other.opaqueKey)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ReusedSession [opaqueKey=" + opaqueKey + ", capabilities=" + capabilities + "]";
    }
}
