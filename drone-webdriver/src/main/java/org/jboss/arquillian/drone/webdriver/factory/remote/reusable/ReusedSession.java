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

import java.io.Serializable;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.SessionId;

/**
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class ReusedSession implements Serializable {

    private static final long serialVersionUID = 4363274772718639918L;

    private String opaqueKey;
    private Capabilities capabilities;

    public ReusedSession(SessionId sessionId, Capabilities capabilities) {
        this.opaqueKey = sessionId.toString();
        this.capabilities = capabilities;
    }

    public SessionId getSessionId() {
        return new SessionId(opaqueKey);
    }

    public Capabilities getCapabilities() {
        return capabilities;
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReusedSession other = (ReusedSession) obj;
        if (capabilities == null) {
            if (other.capabilities != null)
                return false;
        } else if (!capabilities.equals(other.capabilities))
            return false;
        if (opaqueKey == null) {
            if (other.opaqueKey != null)
                return false;
        } else if (!opaqueKey.equals(other.opaqueKey))
            return false;
        return true;
    }

}
