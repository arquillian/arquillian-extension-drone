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

import java.lang.reflect.Field;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

/**
 * Reusable remote driver provides same functionality like {@link RemoteWebDriver}, but it additionally allows to reuse browser
 * session.
 *
 * Provides reusing of {@link RemoteWebDriver} session by allowing to setup {@link DesiredCapabilities} and {@link SessionId}
 * from previous session.
 *
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class ReusableRemoteWebDriver extends RemoteWebDriver {

    ReusableRemoteWebDriver() {
    }

    /**
     * Creates the {@link ReusableRemoteWebDriver} from valid {@link RemoteWebDriver} instance.
     *
     * @param remoteWebDriver valid {@link RemoteWebDriver} instance.
     * @return the {@link RemoteWebDriver} wrapped as {@link ReusableRemoteWebDriver}
     */
    public static ReusableRemoteWebDriver fromRemoteWebDriver(RemoteWebDriver remoteWebDriver) {
        return new ReusableRemoteWebDriver(remoteWebDriver);
    }

    /**
     * Reuses browser session using sessionId and desiredCapabilities as fully-initialized {@link Capabilities} object from the
     * previous {@link RemoteWebDriver} session.
     *
     * @param remoteAddress address of the remote Selenium Server hub
     * @param desiredCapabilities fully-initialized capabilities returned from previous {@link RemoteWebDriver} session
     * @param sessionId sessionId from previous {@link RemoteWebDriver} session
     */
    public static ReusableRemoteWebDriver fromReusedSession(URL remoteAddress, Capabilities desiredCapabilities,
            SessionId sessionId) throws UnableReuseSessionException {
        return new ReusableRemoteWebDriver(remoteAddress, desiredCapabilities, sessionId);
    }

    private ReusableRemoteWebDriver(RemoteWebDriver remoteWebDriver) {
        super();
        setCommandExecutor(remoteWebDriver.getCommandExecutor());

        reuseSession(remoteWebDriver.getSessionId(), remoteWebDriver.getCapabilities());
        try {
            checkReusability();
        } catch (UnableReuseSessionException e) {
            throw new IllegalStateException("Reusing RemoteWebDriver session unexpectedly failed", e);
        }
    }

    private ReusableRemoteWebDriver(URL remoteAddress, Capabilities desiredCapabilities, SessionId sessionId)
            throws UnableReuseSessionException {
        super();
        setCommandExecutor(new HttpCommandExecutor(remoteAddress));
        startClient();

        reuseSession(sessionId, desiredCapabilities);
        checkReusability();
    }

    /**
     * Check that reused session can be controlled.
     *
     * If it cannot be controlled (API calls throw exception), throw {@link UnableReuseSessionException}.
     *
     * @throws UnableReuseSessionException
     */
    private void checkReusability() throws UnableReuseSessionException {
        try {
            this.getCurrentUrl();
        } catch (WebDriverException e) {
            throw new UnableReuseSessionException(e);
        }
    }

    protected final void reuseSession(SessionId sessionId, Capabilities desiredCapabilities) {
        setReusedSessionId(sessionId);
        setReusedCapabilities(desiredCapabilities);
    }

    void setReusedSessionId(SessionId sessionId) {
        Field sessionIdField = getFieldSafely(this, RemoteWebDriver.class, "sessionId");
        writeValueToField(this, sessionIdField, sessionId);
    }

    void setReusedCapabilities(Capabilities capabilities) {
        Field capabilitiesField = getFieldSafely(this, RemoteWebDriver.class, "capabilities");
        writeValueToField(this, capabilitiesField, capabilities);
    }

    private static Field getFieldSafely(Object object, Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeValueToField(Object object, Field field, Object value) {
        boolean wasAccessible = field.isAccessible();
        if (!wasAccessible) {
            field.setAccessible(true);
        }
        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        if (!wasAccessible) {
            field.setAccessible(false);
        }
    }
}
