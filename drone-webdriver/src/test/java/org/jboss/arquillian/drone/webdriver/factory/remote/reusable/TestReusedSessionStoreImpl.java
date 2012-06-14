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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class TestReusedSessionStoreImpl {

    @Mock
    InitializationParameter mockKey1;
    @Mock
    InitializationParameter mockKey2;

    @Mock
    ReusedSession mockSession1;
    @Mock
    ReusedSession mockSession2;

    @Test
    public void when_session_is_stored_then_it_can_be_pulled() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        store.store(mockKey1, mockSession1);
        ReusedSession pulled = store.pull(mockKey1);
        // then
        assertSame(mockSession1, pulled);
    }

    @Test
    public void when_session_pulled_then_it_is_removed_from_store() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        store.store(mockKey1, mockSession1);
        store.pull(mockKey1);
        // then
        assertNull(store.pull(mockKey1));
    }

    @Test
    public void when_store_does_not_contain_any_session_for_key_then_null_value_is_pulled() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        // then
        assertNull(store.pull(mockKey1));
    }

    @Test
    public void when_session_is_stored_under_key_then_another_key_pulls_nothing() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        store.store(mockKey1, mockSession1);
        // then
        assertNull(store.pull(mockKey2));
    }

    @Test
    public void when_two_session_are_stored_under_different_keys_then_each_pulls_associated_session_for_given_key() {
        // given
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        // when
        store.store(mockKey1, mockSession1);
        store.store(mockKey2, mockSession2);
        // then
        assertSame(mockSession2, store.pull(mockKey2));
        assertSame(mockSession1, store.pull(mockKey1));
    }
}
