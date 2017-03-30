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
package org.jboss.arquillian.drone.configuration.mapping;

/**
 * Mapper for double and Double
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public enum DoubleValueMapper implements ValueMapper<Double> {

    INSTANCE;

    @Override
    public boolean handles(Class<?> type, Class<?>... parameters) {
        return Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type);
    }

    @Override
    public Double transform(String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to convert value " + value + " to a double.", e);
        }
    }
}
