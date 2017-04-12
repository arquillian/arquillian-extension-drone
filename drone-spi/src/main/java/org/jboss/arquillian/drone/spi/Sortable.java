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

/**
 * Contract which allows to sort object based an precedence value. This allows to choose between different implementations
 * for
 * same types.
 * <p>
 * Support for Ajocado, Selenium and WebDriver included in the extension always has a precedence of {@code 0}, so it can
 * be
 * easily overridden by providing {@link org.jboss.arquillian.drone.spi.Configurator}, etc. with bigger precedence value.
 *
 * @see org.jboss.arquillian.drone.spi.Configurator
 * @see org.jboss.arquillian.drone.spi.Instantiator
 * @see org.jboss.arquillian.drone.spi.Destructor
 */
public interface Sortable {
    /**
     * Returns precedence of this implementation
     *
     * @return the precedence for current instance
     */
    int getPrecedence();
}
