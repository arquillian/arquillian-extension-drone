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

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.remote.SessionId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Lukas Fryc
 */
public class TestReusedSessionStoreImpl {

    InitializationParameter key1;
    InitializationParameter key2;

    ReusedSession session1;
    ReusedSession session2;

    @Before
    public void initialize() {
        URL url1;
        URL url2;
        try {
            url1 = new URL("http://localhost:8080/1/");
            url2 = new URL("http://localhost:8080/2/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        ImmutableCapabilities capabilities1 = new ImmutableCapabilities();
        ImmutableCapabilities capabilities2 = new ImmutableCapabilities();
        SessionId sessionId1 = new SessionId("1");
        SessionId sessionId2 = new SessionId("2");

        key1 = new InitializationParameter(url1, capabilities1);
        key2 = new InitializationParameter(url2, capabilities2);
        session1 = new ReusedSession(sessionId1, capabilities1);
        session2 = new ReusedSession(sessionId2, capabilities2);
    }

    @Test
    public void when_session_is_stored_then_it_can_be_pulled() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        store.store(key1, session1);
        ReusedSession pulled = store.pull(key1);
        // then
        assertEquals(session1, pulled);
    }

    @Test
    public void when_session_pulled_then_it_is_removed_from_store() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        store.store(key1, session1);
        store.pull(key1);
        // then
        assertNull(store.pull(key1));
    }

    @Test
    public void when_store_does_not_contain_any_session_for_key_then_null_value_is_pulled() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        // then
        assertNull(store.pull(key1));
    }

    @Test
    public void when_session_is_stored_under_key_then_another_key_pulls_nothing() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        store.store(key1, session1);
        // then
        assertNull(store.pull(key2));
    }

    @Test
    public void when_two_session_are_stored_under_different_keys_then_each_pulls_associated_session_for_given_key() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        store.store(key1, session1);
        store.store(key2, session2);
        // then
        assertEquals(session2, store.pull(key2));
        assertEquals(session1, store.pull(key1));
    }
}
