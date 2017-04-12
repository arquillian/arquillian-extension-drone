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
package org.jboss.arquillian.drone.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Drone annotation is used to inject Selenium's WebDriver, Selenium's DefaultSelenium, Arquillian Ajocado AjaxSelenium or
 * other
 * web ui browser implementation into your test.
 * <p/>
 * <p>
 * Any framework can be extended to support usage of this annotation to inject a web browser. See SPI package of Drone for
 * more
 * details
 * <p>
 * {@code @Drone} can be used for both field in a class, thus class scoped instance of the browser or for parameter in a
 * method,
 * where injected instance will be method scoped.
 * </p>
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Inherited
public @interface Drone {
}
