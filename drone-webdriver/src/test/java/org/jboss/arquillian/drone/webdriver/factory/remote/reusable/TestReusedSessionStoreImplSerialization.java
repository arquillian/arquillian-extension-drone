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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class TestReusedSessionStoreImplSerialization {

    ReusedSessionPermanentFileStorage fileStore;

    @Test
    public void when_store_is_serialized_to_file_then_it_can_be_deserialized_and_reused() throws IOException {

        // given
        File tmpFile = File.createTempFile("drone-webdriver-session-store", "");
        System.setProperty(ReusedSessionPermanentFileStorage.FILE_STORE_PROPERTY, tmpFile.getAbsolutePath());

        fileStore = new ReusedSessionPermanentFileStorage();
        ReusedSessionStoreImpl store = new ReusedSessionStoreImpl();
        URL url = new URL("http://localhost/");
        InitializationParameter key = new InitializationParameter(url, DesiredCapabilities.firefox());
        ReusedSession session = new ReusedSession(new SessionId("opaqueKey"), DesiredCapabilities.firefox());

        try {
            // when
            store.store(key, session);
            fileStore.writeStore(store);

            // then
            ReusedSessionStore restoredStore = fileStore.loadStore();
            assertNotNull(restoredStore);
            ReusedSession restoredSession = restoredStore.pull(key);
            assertEquals(session, restoredSession);
        } finally {
            if (tmpFile != null && tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }

    @Test
    public void when_store_file_does_not_exists_then_null_is_returned() {

        // given
        File nonExistent = new File(UUID.randomUUID().toString());
        System.setProperty(ReusedSessionPermanentFileStorage.FILE_STORE_PROPERTY, nonExistent.getAbsolutePath());
        fileStore = new ReusedSessionPermanentFileStorage();

        // when
        ReusedSessionStore loadedStore = fileStore.loadStore();
        System.clearProperty(ReusedSessionPermanentFileStorage.FILE_STORE_PROPERTY);

        // then
        assertNull(loadedStore);
    }
}
