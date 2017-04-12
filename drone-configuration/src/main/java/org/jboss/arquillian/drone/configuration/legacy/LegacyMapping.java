/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.configuration.legacy;

/**
 * Encapsulation for changing a property value into different property or capability value
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public interface LegacyMapping {

    /**
     * Returns {@code true} if remapped property will be backed by capability map
     */
    boolean remapsToCapability();

    /**
     * Returns {@code true} if remapped property will be backed by another property field
     */
    boolean remapsToProperty();

    /**
     * Returns a new value for a property key, e.g. the key where the value will be stored as a capability in the map or a
     * field
     * name
     *
     * @param fieldName
     *     Original field name from legacy Drone configuration
     *
     * @return New name to be used
     */
    String remapKey(String fieldName);

    /**
     * Returns a new value for a property value, e.g. the value which will be stored a the capability in the map or a
     * field
     * value
     *
     * @return New value to be used
     */
    String remapValue(String value);
}
