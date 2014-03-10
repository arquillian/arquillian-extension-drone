/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.drone.spi;

import java.lang.annotation.Annotation;

/**
 * Injection point is an unique description of a drone in code.
 *
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public interface InjectionPoint<DRONE> {

    /**
     * @return Type of drone to be injected.
     */
    Class<DRONE> getDroneType();

    /**
     * @return Qualifier the drone is annotated with.
     */
    Class<? extends Annotation> getQualifier();

    /**
     * @return Lifecycle of the drone.
     */
    Lifecycle getLifecycle();

    public static enum Lifecycle {
        /**
         * Method lifecycle means the drone will be prepared in {@link Before} and destroyed in {@link After}.
         */
        METHOD,

        /**
         * Class lifecycle means the drone will be prepared in {@link BeforeClass} and destroyed in {@link AfterClass}.
         */
        CLASS,

        /**
         * Deployment lifecycle means the drone will be prepared in {@link BeforeClass} and destroyed in
         * {@link BeforeUnDeploy}.
         */
        DEPLOYMENT
    }
}