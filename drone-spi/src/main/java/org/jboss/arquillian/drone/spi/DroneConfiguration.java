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
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.api.annotation.Default;

/**
 * Configuration of arbitrary Drone Web user interface test framework. It allows to get configured
 * from both Arquillian Descriptor and System properties which takes precedence.
 * <p>
 * <p>
 * A matching extension in the descriptor with is searched by using
 * configuration name. If {@link org.jboss.arquillian.drone.spi.Qualifier} extension differs from
 * {@link Default}, it is used to get the configuration if it exists, otherwise the
 * default one is used.
 * </p>
 * <p>
 * <p>
 * See following example which explains the mapping.
 * </p>
 * <p>
 * <p>
 * If configuration is named {@code webdriver}, then
 * {@code <extension qualifier="webdriver">} is looked in the descriptor
 * file and properties are mapped to configuration values. Then System
 * properties with prefix {@code arq.extension.webdriver.*} are mapped to the
 * configuration values, possibly overriding values provided within descriptor
 * file.
 * </p>
 * <p>
 * <pre>
 *    &lt;extension qualifier="webdriver"&gt;
 *    &lt;configuration&gt;
 *       &lt;property name="remoteAddress"&gt;http://localhost:5000&lt;/property&gt;
 *    &lt;/configuration&gt;
 *    &lt;/extension&gt;
 * </pre>
 * <p>
 * <p>
 * A system property is passed as
 * {@code -Darq.extension.webdriver.remoteAddresss=http://localhost:6000}
 * </p>
 * <p>
 * Then configuration will read descriptor and set remote address value to http://localhost:5000  which
 * becomes later overridden by http://localhost:6000 passed from the command line.
 * </p>
 * <p>
 * <p>
 * <p>
 * For more complex example, see following rules are applied to compute
 * {@code qualifier} value, system property prefix and property name:
 * </p>
 * <p>
 * <ul>
 * <li>qualifier = {@link DroneConfiguration#getConfigurationName()} + - + @Qualifier, where all letters are lower
 * case</li>
 * <li>System property prefix = arq.extension. + {@link DroneConfiguration#getConfigurationName()} + . + qualifier</li>
 * </ul>
 *
 * @param <C>
 *     Configuration type
 */
public interface DroneConfiguration<C extends DroneConfiguration<C>> {
    /**
     * Returns the name of the configuration.
     *
     * @return Name of the configuration
     */
    String getConfigurationName();

    /**
     * Configures configuration from descriptor and System properties
     *
     * @param descriptor
     *     Arquillian Descriptor
     * @param qualifier
     *     Qualifier
     *
     * @return Configured configuration instance
     */
    @Deprecated
    C configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier);
}
