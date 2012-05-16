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

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class ReusableRemoteWebDriverExtension {

    public static final File DEFAULT_FILE = new File(System.getProperty("user.home"), ".drone-driver-session-store");

    @Inject
    @SuiteScoped
    private InstanceProducer<ReusedSessionStore> storeInstance;

    @Inject
    @SuiteScoped
    private InstanceProducer<InitializationParametersMap> initParamsMapInstance;

    private ReusedSessionFileStore fileStore = new ReusedSessionFileStore();

    public void initializeStore(@Observes BeforeSuite event) {
        ReusedSessionStore store = fileStore.loadStoreFromFile(DEFAULT_FILE);
        if (store == null) {
            store = new ReusedSessionStoreImpl();
        }
        storeInstance.set(store);

        initParamsMapInstance.set(new InitializationParametersMap());
    }

    public void persistStore(@Observes PersistReusedSessionsEvent event) {
        fileStore.writeStoreToFile(DEFAULT_FILE, storeInstance.get());
    }
}
