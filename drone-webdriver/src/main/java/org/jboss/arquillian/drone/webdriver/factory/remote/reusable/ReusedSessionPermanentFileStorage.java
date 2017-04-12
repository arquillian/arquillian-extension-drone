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
import java.io.InvalidClassException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Loads and writes {@link ReusedSessionStore} from/to file.
 *
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class ReusedSessionPermanentFileStorage implements ReusedSessionPermanentStorage {

    private static final Logger log = Logger.getLogger(ReusedSessionPermanentFileStorage.class.getName());

    public static String FILE_STORE_PROPERTY = "drone.webdriver.session.store";
    public static File DEFAULT_FILE = new File(System.getProperty("user.home"), ".drone-webdriver-session-store");

    private final File permanentFile;

    {
        String storeFileProperty = System.getProperty(FILE_STORE_PROPERTY);
        if (storeFileProperty != null) {
            permanentFile = new File(storeFileProperty);
        } else {
            permanentFile = DEFAULT_FILE;
        }
    }

    public ReusedSessionStore loadStore() {
        try {
            byte[] readStore = readStore(permanentFile);

            if (readStore == null) {
                return null;
            }

            ReusedSessionStore loadedSession =
                SerializationUtils.deserializeFromBytes(ReusedSessionStore.class, readStore);
            return loadedSession;
        } catch (InvalidClassException e) {
            log.log(Level.WARNING,
                "Unable to get reused session store from file storage, likely it is due to its internal format change. "
                    + "Drone will replace file " + permanentFile + " with recent implementation. Cause: ", e);
            return null;
        } catch (ClassNotFoundException e) {
            log.log(Level.WARNING, "Unable to get reused session store from file storage. " + "Drone will replace file "
                + permanentFile + " with recent implementation. Cause: ", e);
            return null;
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to get reused session store from file storage. " + "Drone will replace file "
                + permanentFile + " with recent implementation. Cause: ", e);
            return null;
        }
    }

    public void writeStore(ReusedSessionStore store) {
        try {
            byte[] serialized = SerializationUtils.serializeToBytes(store);
            writeStore(permanentFile, serialized);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to persist reused session store, session reuse will not work", e);
        }
    }

    private byte[] readStore(File file) throws IOException {
        if (Validate.readable(file)) {
            return FileUtils.readFileToByteArray(file);
        }
        log.info("Reused session store is not available at " + file + ", a new one will be created.");
        return null;
    }

    private void writeStore(File file, byte[] data) throws IOException {
        if (Validate.writeable(file)) {
            FileUtils.writeByteArrayToFile(file, data);
            return;
        }
        log.severe("Reused session store cannot be persisted to file, session reuse will not work");
    }
}
