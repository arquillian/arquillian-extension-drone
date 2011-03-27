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
package org.jboss.arquillian.drone.spi;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;

/**
 * Configuration of arbitrary drone web UI test framework. It allows to get configured
 * from both Arquillian Descriptor and System properties which takes precedence.
 * 
 * <p>
 * A matching extension in the descriptor with is searched by using
 * configuration name. If {@link Qualifier} extension differs from {@see
 * Default}, it is used to get the configuration if it exists, otherwise the
 * default one is used.
 * </p>
 * 
 * <p>
 * See following example which explains the mapping
 * </p>
 * 
 * <p>
 * If configuration is named {@code ajocado}, then
 * {@code &lt;extension qualifier="selenium"&gt;} is looked in the descriptor
 * file and properties are mapped to configuration values. Then System
 * properties with prefix {@code arquillian.ajocado.*} are mapped to the
 * configuration values, possibly overriding values provided within descriptor
 * file.
 * </p>
 * 
 * <pre>
 *    &lt;extension qualifier="ajocado"&gt;
 *    &lt;configuration&gt;
 *       &lt;property name="seleniumPort"&gt;5000&lt;/property&gt;      
 *    &lt;/configuration&gt;
 *    &lt;/extension&gt;
 * </pre>
 * 
 * <p>
 * A system property is passed as
 * {@code -Darquillian.ajocado.selenium.port=6000}
 * </p>
 * <p>
 * Then configuration will read descriptor and set value 5000 for port which
 * becomes later overridden by value 6000 passed from the command line.
 * </p>
 * 
 * 
 * <p>
 * For more complex example, see following rules are applied to compute
 * {@code qualifier} value, system property prefix and property name:
 * </p>
 * 
 * <ul>
 *     
 *    <li>qualifier = {@code WebTestConfiguration#getConfigurationName() + - + @Qualifier, where
 *        all letters are lower case</li>
 *    <li>System property prefix = arquillian. + {@code WebTestConfiguration#getConfigurationName() + ., where
 *         all configuration name is converted to lower case characters and or non-letter characters are
 *         replaced with a dot (.) </li>
 *    <li>property names for descriptors = fields (getters) are not modified </i>    
 *    <li>property names for System properties = fields (getters) of configuration are converted from camel case to replacing
 *       every upper case latter with a dot (.) and lower case equivalent</li>
 * </ul>
 * 
 * @param <C> Configuration type
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 */
public interface DroneConfiguration<C extends DroneConfiguration<C>>
{
   /**
    * Returns the name of the configuration.
    * 
    * @return Name of the configuration
    */
   String getConfigurationName();

   /**
    * Configures configuration from descriptor and System properties
    * 
    * @param descriptor Arquillian Descriptor
    * @param qualifier Qualifier
    * @return Configured configuration instance
    */
   C configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier);
}
