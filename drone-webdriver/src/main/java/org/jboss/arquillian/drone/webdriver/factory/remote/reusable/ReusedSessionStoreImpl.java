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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class ReusedSessionStoreImpl implements ReusedSessionStore {

    private static final long serialVersionUID = 9148577993706454735L;
    private Map<InitializationParameter, Queue<ReusedSession>> store = new HashMap<InitializationParameter, Queue<ReusedSession>>();

    @Override
    public ReusedSession pull(InitializationParameter key) {
        synchronized (store) {
            Queue<ReusedSession> queue = store.get(key);
            if (queue == null) {
                return null;
            }
            return queue.poll();
        }
    }

    @Override
    public void store(InitializationParameter key, ReusedSession session) {
        synchronized (store) {
            Queue<ReusedSession> queue = store.get(key);
            if (queue == null) {
                queue = new LinkedList<ReusedSession>();
                store.put(key, queue);
            }
            queue.add(session);
        }
    }
}
