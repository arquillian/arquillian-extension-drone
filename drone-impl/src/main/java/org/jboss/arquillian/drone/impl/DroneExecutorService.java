/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStopping;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.core.spi.context.IdBoundContext;
import org.jboss.arquillian.core.spi.context.NonIdBoundContext;
import org.jboss.arquillian.drone.spi.DroneRegistry;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;

// ARQ-1653: Should be replaced by Core service when the time is right, ARQ-1654
public class DroneExecutorService {

    @Inject @ApplicationScoped
    private InstanceProducer<DroneExecutorService> serviceProducer;

    private ExecutorService executorService;

    @SuppressWarnings("unused")
    private void register(@Observes DroneRegistry registry, Injector injector) {
        executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ContextualThreadFactory(injector.inject(new ContextHolder())),
                new ThreadPoolExecutor.CallerRunsPolicy());

        serviceProducer.set(this);
    }

    @SuppressWarnings("unused")
    private void shutdown(@Observes ManagerStopping event) {
        if(executorService != null) {
            executorService.shutdownNow();
        }
    }

    // Public API
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(task, result);
    }

    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    private class ContextualThreadFactory implements ThreadFactory {

        private ThreadFactory delegate;
        private ContextHolder holder;

        public ContextualThreadFactory(ContextHolder holder) {
            this.delegate = Executors.defaultThreadFactory();
            this.holder = holder;
        }

        @Override
        public Thread newThread(Runnable r) {
            holder.setState();
            return delegate.newThread(new ContextualRunnable(r, holder));
        }
    }

    // Private Thread Impl
    private class ContextualRunnable implements Runnable {
        private Runnable delegate;
        private ContextHolder holder;

        public ContextualRunnable(Runnable r, ContextHolder holder) {
            this.delegate = r;
            this.holder = holder;
        }

        @Override
        public void run() {

            try {
                holder.activate();
                delegate.run();
            } finally {
                holder.deactivate();
            }
        }
    }

    private static class ContextHolder {

        @Inject
        private Instance<ApplicationContext> applicationContextInst;
        private ApplicationContext applicationContext;
        private Object applicationContextActive;

        @Inject
        private Instance<SuiteContext> suiteContextInst;
        private SuiteContext suiteContext;
        private Object suiteContextActive;

        @Inject
        private Instance<ClassContext> classContextInst;
        private ClassContext classContext;
        private Class<?> classContextId;

        @Inject
        private Instance<TestContext> testContextInst;
        private TestContext testContext;
        private Object testContextId;

        public void setState() {
            applicationContext = applicationContextInst.get();
            applicationContextActive = applicationContext.isActive() ? "":null;

            suiteContext = suiteContextInst.get();
            suiteContextActive = suiteContext.isActive() ? "":null;

            classContext = classContextInst.get();
            classContextId = classContext.getActiveId();

            testContext = testContextInst.get();
            testContextId = testContext.getActiveId();
        }

        public void activate() {
            activateIfPreviouslyActive(applicationContext,
                    applicationContextActive);
            activateIfPreviouslyActive(suiteContext, suiteContextActive);
            activateIfPreviouslyActive(classContext, classContextId);
            activateIfPreviouslyActive(testContext, testContextId);
        }

        public void deactivate() {
            deactivateIfActive(testContext);
            deactivateIfActive(classContext);
            deactivateIfActive(suiteContext);
            deactivateIfActive(applicationContext);
        }

        private <T> void activateIfPreviouslyActive(IdBoundContext<T> context,
                T id) {
            if (id != null) {
                context.activate(id);
            }
        }

        private void activateIfPreviouslyActive(NonIdBoundContext context,
                Object id) {
            if (id != null) {
                context.activate();
            }
        }

        private void deactivateIfActive(IdBoundContext<?> context) {
            if (context.isActive()) {
                context.deactivate();
            }
        }

        private void deactivateIfActive(NonIdBoundContext context) {
            if (context.isActive()) {
                context.deactivate();
            }
        }
    }
}
