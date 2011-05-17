/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Determines an annotation to acts as qualified for telling different Drone Drivers apart.
 * 
 * <p>Usage:</p>
 * <pre>
 * <code>
 * @Drone AjaxSelenium instance1;
 * @Drone @RemoteMachine AjaxSelenium instance2;
 * </code>
 * </pre>
 * 
 * <p>
 * If {@code @RemoteMachine} is an annotation marked with {@link Qualifier}, then two different
 * instances of AjaxSelenium object will be configured and instantiated before execution of the 
 * first test method.
 * </p>
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Qualifier
{  
}
