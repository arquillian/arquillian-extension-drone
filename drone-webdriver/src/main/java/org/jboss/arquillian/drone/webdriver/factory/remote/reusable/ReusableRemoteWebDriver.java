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
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

/**
 * Reusable remote driver provides same functionality like {@link RemoteWebDriver}, but it additionally allows to reuse
 * browser
 * session.
 * <p>
 * Provides reusing of {@link RemoteWebDriver} session by allowing to setup {@link
 * SessionId}
 * from previous session.
 *
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class ReusableRemoteWebDriver extends RemoteWebDriver {

    public ReusableRemoteWebDriver() {
        super();
    }

    protected ReusableRemoteWebDriver(CommandExecutor executor, Capabilities capabilities, SessionId sessionId) {
        super();
        setCommandExecutor(executor);
        setReusedCapabilities(capabilities);
        setSessionId(sessionId.toString());
    }

    protected ReusableRemoteWebDriver(URL remoteAddress, Capabilities capabilities, SessionId sessionId) {
        super();
        HttpCommandExecutor httpCommandExecutor = new HttpCommandExecutor(remoteAddress);
        setCommandExecutor(httpCommandExecutor);
        setReusedCapabilities(capabilities);

        setValueToFieldInHttpCommandExecutor(httpCommandExecutor, "commandCodec", Dialect.W3C.getCommandCodec());
        setValueToFieldInHttpCommandExecutor(httpCommandExecutor, "responseCodec", Dialect.W3C.getResponseCodec());

        setSessionId(sessionId.toString());
    }

    /**
     * Creates the {@link ReusableRemoteWebDriver} from valid {@link RemoteWebDriver} instance.
     *
     * @param remoteWebDriver
     *     valid {@link RemoteWebDriver} instance.
     *
     * @return the {@link RemoteWebDriver} wrapped as {@link ReusableRemoteWebDriver}
     */
    public static RemoteWebDriver fromRemoteWebDriver(RemoteWebDriver remoteWebDriver) {

        RemoteWebDriver driver = new ReusableRemoteWebDriver(remoteWebDriver.getCommandExecutor(),
            remoteWebDriver.getCapabilities(), remoteWebDriver.getSessionId());
        try {
            checkReusability(remoteWebDriver.getSessionId(), driver);
            return driver;
        } catch (UnableReuseSessionException e) {
            throw new IllegalStateException("Reusing RemoteWebDriver session unexpectedly failed", e);
        }
    }

    /**
     * Reuses browser session using sessionId and capabilities as fully-initialized {@link Capabilities} object
     * from the
     * previous {@link RemoteWebDriver} session.
     *
     * @param remoteAddress
     *     address of the remote Selenium Server hub
     * @param capabilities
     *     fully-initialized capabilities returned from previous {@link RemoteWebDriver} session
     * @param sessionId
     *     sessionId from previous {@link RemoteWebDriver} session
     */
    public static RemoteWebDriver fromReusedSession(URL remoteAddress, Capabilities capabilities,
        SessionId sessionId)
        throws UnableReuseSessionException {

        RemoteWebDriver driver = new ReusableRemoteWebDriver(remoteAddress, capabilities,
            sessionId);
        checkReusability(sessionId, driver);
        return driver;
    }

    /**
     * Check that reused session can be controlled.
     * <p>
     * If it cannot be controlled (API calls throw exception), throw {@link UnableReuseSessionException}.
     *
     * @throws UnableReuseSessionException
     */
    private static void checkReusability(SessionId oldSessionId, RemoteWebDriver driver)
        throws UnableReuseSessionException {
        if (!oldSessionId.equals(driver.getSessionId())) {
            throw new UnableReuseSessionException("The created session has another id then session we tried to reuse");
        }
        try {
            driver.getCurrentUrl();
        } catch (WebDriverException e) {
            throw new UnableReuseSessionException(e);
        }
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

    void setReusedCapabilities(Capabilities capabilities) {
        Field capabilitiesField = getFieldSafely(this, RemoteWebDriver.class, "capabilities");
        writeValueToField(this, capabilitiesField, capabilities);
    }

    void setValueToFieldInHttpCommandExecutor(Object instance, String fieldName, Object value) {
        Field field = getFieldSafely(instance, HttpCommandExecutor.class, fieldName);
        writeValueToField(instance, field, value);
    }
}
