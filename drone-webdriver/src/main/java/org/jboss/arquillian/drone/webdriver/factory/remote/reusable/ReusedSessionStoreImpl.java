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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Storage for ReusedSession. It allows to work with sessions stored with different versions of Drones in a single place.
 *
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class ReusedSessionStoreImpl implements ReusedSessionStore {
    private static final Logger log = Logger.getLogger(ReusedSessionStoreImpl.class.getName());

    private static final long serialVersionUID = 914857799370645455L;

    // session is valid for two days
    private static final int SESSION_VALID_IN_SECONDS = 3600 * 48;

    // represents a "raw" list of reused sessions, storing sessions with timeout information
    // we cannot use Deque, since it is 1.6+
    private final Map<ByteArray, LinkedList<ByteArray>> rawStore;

    public ReusedSessionStoreImpl() {
        this.rawStore = new LinkedHashMap<ByteArray, LinkedList<ByteArray>>();
    }

    @Override
    public ReusedSession pull(InitializationParameter key) {
        synchronized (rawStore) {

            LinkedList<ByteArray> queue = null;

            log.log(Level.FINER, "Pulling key {0} from Session Store", key);

            // find key
            for (Entry<ByteArray, LinkedList<ByteArray>> entry : rawStore.entrySet()) {
                InitializationParameter candidate = entry.getKey().as(InitializationParameter.class);

                if (candidate != null && candidate.equals(key)) {
                    queue = entry.getValue();
                    break;
                }
            }

            // there is no such queue
            if (queue == null || queue.isEmpty()) {
                return null;
            }

            // map the view to available queues
            LinkedList<RawDisposableReusedSession> sessions = getValidSessions(queue);
            if (sessions == null || sessions.isEmpty()) {
                return null;
            }

            // get session and dispose it
            RawDisposableReusedSession disposableSession = sessions.getLast();
            disposableSession.dispose();

            log.log(Level.FINE, "Reusing session {0} ", disposableSession.getSession().getSessionId());

            return disposableSession.getSession();
        }
    }

    @Override
    public void store(InitializationParameter key, ReusedSession session) {
        synchronized (rawStore) {

            // update map of raw data
            ByteArray rawKey = ByteArray.fromObject(key);
            if (rawKey == null) {
                log.log(Level.SEVERE,
                    "Unable to store browser initialization parameter in ReusedSessionStore for browser :{0}",
                    key.getCapabilities().getBrowserName());
                return;
            }

            LinkedList<ByteArray> rawList = rawStore.get(rawKey);
            if (rawList == null) {
                rawList = new LinkedList<ByteArray>();
                rawStore.put(rawKey, rawList);
            }
            ByteArray rawSession = ByteArray.fromObject(session);
            if (rawSession == null) {
                log.log(Level.SEVERE,
                    "Unable to store browser initialization parameter in ReusedSessionStore for browser :{0}",
                    key.getCapabilities().getBrowserName());
                return;
            }
            // add a timestamp to the session and store in the list
            TimeStampedSession timeStampedSession = new TimeStampedSession(rawSession);
            rawList.add(ByteArray.fromObject(timeStampedSession));

            log.log(Level.FINE, "Stored session {0} within {1}", new Object[] {
                timeStampedSession.getSession().getSessionId(),
                key});
        }
    }

    private LinkedList<RawDisposableReusedSession> getValidSessions(LinkedList<ByteArray> rawQueue) {

        if (rawQueue == null || rawQueue.size() == 0) {
            return new LinkedList<RawDisposableReusedSession>();
        }

        LinkedList<RawDisposableReusedSession> sessions = new LinkedList<RawDisposableReusedSession>();
        Iterator<ByteArray> byteArrayIterator = rawQueue.iterator();
        while (byteArrayIterator.hasNext()) {
            TimeStampedSession session = byteArrayIterator.next().as(TimeStampedSession.class);
            // add session if valid and it can be deserialized
            ReusedSession reusedSession = session.getSession();
            if (session.isValid(SESSION_VALID_IN_SECONDS) && reusedSession != null) {
                sessions.add(new RawDisposableReusedSession(session.getRawSession(), rawQueue, reusedSession));
            }
            // remove completely if session is not valid
            else if (!session.isValid(SESSION_VALID_IN_SECONDS)) {
                byteArrayIterator.remove();
            }
        }
        return sessions;
    }

    /**
     * Wrapper for array of bytes to act as a key/value in a map. This abstraction allows as to have stored sessions for
     * Drones
     * with incompatible serialVersionUID, for instance Drones based on different Selenium version.
     * <p>
     * This implementation ignores invalid content, it simply returns null when a object cannot be deserialized
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     */
    static class ByteArray implements Serializable {
        private static final long serialVersionUID = 1L;

        private byte[] raw = new byte[0];

        static ByteArray fromObject(Serializable object) {
            ByteArray bytes = new ByteArray();
            try {
                bytes.raw = SerializationUtils.serializeToBytes(object);
                return bytes;
            } catch (IOException e) {
                log.log(Level.FINE, "Unable to deserialize object of " + object.getClass().getName(), e);
            }
            return null;
        }

        <T extends Serializable> T as(Class<T> classType) {
            try {
                return SerializationUtils.deserializeFromBytes(classType, raw);
            } catch (ClassNotFoundException e) {
                log.log(Level.FINE, "Unable to deserialize object of " + classType.getName(), e);
            } catch (IOException e) {
                log.log(Level.FINE, "Unable to deserialize object of " + classType.getName(), e);
            } catch (ClassCastException e) {
                log.log(Level.FINE, "Unable to deserialize object of " + classType.getName(), e);
            }

            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(raw);
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
            ByteArray other = (ByteArray) obj;
            if (!Arrays.equals(raw, other.raw)) {
                return false;
            }
            return true;
        }
    }

    /**
     * Wrapper for ReusedSession. This session is stored in binary format including a timestamp.
     * <p>
     * This allows implementation to invalidate a session without actually trying to deserialize it.
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     */
    static class TimeStampedSession implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Date timestamp;

        private final ByteArray rawSession;

        TimeStampedSession(ByteArray rawSession) {
            this.timestamp = new Date();
            this.rawSession = rawSession;
        }

        public boolean isValid(int timeoutInSeconds) {

            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timestamp);
            calendar.add(Calendar.SECOND, timeoutInSeconds);

            return calendar.getTime().after(now);
        }

        public ReusedSession getSession() {
            return rawSession.as(ReusedSession.class);
        }

        public ByteArray getRawSession() {
            return rawSession;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((rawSession == null) ? 0 : rawSession.hashCode());
            result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
            TimeStampedSession other = (TimeStampedSession) obj;
            if (rawSession == null) {
                if (other.rawSession != null) {
                    return false;
                }
            } else if (!rawSession.equals(other.rawSession)) {
                return false;
            }
            if (timestamp == null) {
                if (other.timestamp != null) {
                    return false;
                }
            } else if (!timestamp.equals(other.timestamp)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(timestamp).append(" ").append(getSession());
            return sb.toString();
        }
    }

    /**
     * Wrapper for reusable session with ability to dispose data from rawStore.
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     */
    private static class RawDisposableReusedSession {
        private final ByteArray key;
        private final ReusedSession session;
        private final LinkedList<ByteArray> parentList;

        RawDisposableReusedSession(ByteArray key, LinkedList<ByteArray> parentList, ReusedSession session) {
            this.key = key;
            this.parentList = parentList;
            this.session = session;
        }

        /**
         * Removes current session from queue of relevant raw data
         */
        public void dispose() {
            synchronized (parentList) {
                Iterator<ByteArray> iterator = parentList.iterator();
                ReusedSession wrappedKey = key.as(ReusedSession.class);
                if (wrappedKey == null) {
                    throw new IllegalStateException(
                        "Could not dispose a session from the storage, current session cannot be deserialized.");
                }

                // find appropriate object from parentList to be removed
                while (iterator.hasNext()) {
                    TimeStampedSession candidate = iterator.next().as(TimeStampedSession.class);
                    // check if both point to the same session
                    if (candidate != null && candidate.getSession() != null && candidate.getSession()
                        .equals(wrappedKey)) {
                        iterator.remove();
                    }
                }
            }
        }

        public ReusedSession getSession() {
            return session;
        }
    }
}
