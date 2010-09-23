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
package org.jboss.arquillian.selenium.spi;

import org.jboss.arquillian.selenium.SeleniumExtensionConfiguration;
import org.jboss.arquillian.selenium.annotation.Selenium;

/**
 * The instantiator provides a bridge between Arquillian Selenium extension and
 * arbitrary testing driver. Arquillian Selenium provides instantiators to most
 * common frameworks.
 * 
 * Users which require special functionality can provide their own instantiator
 * and pass it to {@link Selenium} annotation configuration. The instantiator
 * must accept contract defined by this interface and provide a zero-argument
 * constructor.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @param <T> The type of driver used by this instantiator
 * @see Selenium
 */
public interface Instantiator<T>
{
   
   /**
    * Returns precedence of this instantiator. If two instantiators for the
    * same class are found, the one with the highest precedence is used. By 
    * default, Instantiators provided with Arquillian Selenium extension have
    * precedence of {@code 0}.
    * 
    * @return the precedence for current instantiator
    */
   int getPrecedence();
   
   /**
    * Creates an instance of the driver.
    * 
    * The instance is created before execution of the first method of the test
    * class automatically by calling this method. The object is then bound to
    * the Arquillian context, where it stays until the execution of the last
    * test method is finished.
    * 
    * @param configuration the configuration object for the extension
    * @return Newly created instance of the driver
    */
   T create(SeleniumExtensionConfiguration configuration);

   /**
    * Destroys an instance of the driver.
    * 
    * After the last method is run, the driver instance is destroyed. This means
    * browser windows, if any, are closed and used resources are freed.
    * 
    * @param instance The instance to be destroyed
    */
   void destroy(T instance);
}
