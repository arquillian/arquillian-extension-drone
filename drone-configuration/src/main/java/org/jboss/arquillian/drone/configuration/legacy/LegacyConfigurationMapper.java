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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.drone.configuration.mapping.StringValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.ValueMapper;

/**
 * Utility to move legacy configuration to capability based configuration. This greatly simplifies configuration in Drone.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class LegacyConfigurationMapper {
    private static final Logger log = Logger.getLogger(LegacyConfigurationMapper.class.getName());

    // FIXME this should be in SPI with a proper event MODEL
    private static final Map<String, LegacyMapping> LEGACY_MAP;

    static {
        LEGACY_MAP = new HashMap<>();

        // all legacy mappings were removed in Drone 2.0.0.Alpha1
    }

    public static boolean isLegacy(String propertyName) {
        return LEGACY_MAP.containsKey(propertyName);
    }

    private static boolean remapsToProperty(String propertyName) {
        LegacyMapping mapping = LEGACY_MAP.get(propertyName);
        if (mapping == null) {
            throw new IllegalStateException("Legacy mapping for property name " + propertyName + " is not defined.");
        }

        return mapping.remapsToProperty();
    }

    public static boolean remapsToCapability(String propertyName) {
        return !remapsToProperty(propertyName);
    }

    public static String remapKey(String propertyKey) {
        if (isLegacy(propertyKey)) {
            return LEGACY_MAP.get(propertyKey).remapKey(propertyKey);
        }

        // return original value
        return propertyKey;
    }

    public static String remapValue(String propertyKey, String propertyValue) {
        if (isLegacy(propertyValue)) {
            return LEGACY_MAP.get(propertyKey).remapValue(propertyValue);
        }

        // return original value
        return propertyValue;
    }

    @SuppressWarnings("unused")
    private static class FieldMapping implements LegacyMapping {
        private final String fieldName;
        private final ValueMapper<?> mapper;

        FieldMapping(String fieldName) {
            this(fieldName, StringValueMapper.INSTANCE);
        }

        FieldMapping(String fieldName, ValueMapper<?> mapper) {
            this.fieldName = fieldName;
            this.mapper = mapper;
        }

        @Override
        public boolean remapsToProperty() {
            return true;
        }

        @Override
        public boolean remapsToCapability() {
            return !remapsToProperty();
        }

        @Override
        public String remapKey(String oldFieldName) {

            log.log(Level.WARNING,
                "Configuration property \"{0}\" is deprecated, please replace it with property \"{1}\" instead.",
                new Object[] {oldFieldName, fieldName});
            return fieldName;
        }

        @Override
        public String remapValue(String value) {
            return ((Object) mapper.transform(value)).toString();
        }
    }

    private static class DefaultCapabilityMapping implements LegacyMapping {
        private final String capabilityName;
        private final ValueMapper<?> mapper;

        @SuppressWarnings("unused")
        DefaultCapabilityMapping(String capabilityName) {
            this(capabilityName, StringValueMapper.INSTANCE);
        }

        DefaultCapabilityMapping(String capabilityName, ValueMapper<?> mapper) {
            this.capabilityName = capabilityName;
            this.mapper = mapper;
        }

        @Override
        public boolean remapsToCapability() {
            return true;
        }

        @Override
        public boolean remapsToProperty() {
            return !remapsToCapability();
        }

        public String remapKey(String fieldName) {

            log.log(Level.WARNING,
                "Configuration property \"{0}\" is deprecated, please replace it with capability based property \"{1}\" instead.",
                new Object[] {fieldName, capabilityName});

            return capabilityName;
        }

        public String remapValue(String value) {
            return ((Object) mapper.transform(value)).toString();
        }
    }

    @SuppressWarnings("unused")
    private static class LoggingCapabilityMapping extends DefaultCapabilityMapping {

        private final String loggingMessage;

        LoggingCapabilityMapping(String capabilityName, ValueMapper<?> mapper, String loggingMessage) {
            super(capabilityName, mapper);
            this.loggingMessage = loggingMessage;
        }

        @Override
        public String remapValue(String value) {
            log.log(Level.WARNING, loggingMessage);
            return super.remapValue(value);
        }
    }
}
