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

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class ReusableRemoteWebDriverExtension {

    @Inject
    @SuiteScoped
    private InstanceProducer<ReusedSessionStore> storeInstance;

    @Inject
    @SuiteScoped
    private InstanceProducer<InitializationParametersMap> initParamsMapInstance;

    @Inject
    @ApplicationScoped
    private InstanceProducer<ReusedSessionPermanentStorage> permanentStorage;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    public void initialize(@Observes BeforeSuite event) {
        initializePermanentStorage();
        initializeStore();
    }

    private void initializePermanentStorage() {
        ReusedSessionPermanentStorage instance = serviceLoader.get().onlyOne(ReusedSessionPermanentStorage.class);
        permanentStorage.set(instance);
    }

    private void initializeStore() {
        ReusedSessionStore store = permanentStorage.get().loadStore();
        if (store == null) {
            store = new ReusedSessionStoreImpl();
        }
        storeInstance.set(store);

        initParamsMapInstance.set(new InitializationParametersMap());
    }

    public void persistStore(@Observes PersistReusedSessionsEvent event) {
        permanentStorage.get().writeStore(storeInstance.get());
    }

    public void destroyLastRemoteWebDriver(@Observes AfterClass event, ReusableRemoteWebDriverToDestroy toDestroy) {
        toDestroy.destroy();
    }
}
