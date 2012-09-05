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
 *
 */
public class LegacyPropertyToCapabilityMapper {
    private static final Logger log = Logger.getLogger(LegacyPropertyToCapabilityMapper.class.getName());

    // FIXME this should be in SPI with a proper event MODEL
    private static final Map<String, DefaultCapabilityMapping> LEGACY_MAP;
    static {
        LEGACY_MAP = new HashMap<String, DefaultCapabilityMapping>();
        // firefox
        LEGACY_MAP.put("firefoxBinary", new DefaultCapabilityMapping("firefox_binary"));
        LEGACY_MAP.put("firefoxProfile", new DefaultCapabilityMapping("firefox_profile"));
        // chrome
        LEGACY_MAP.put("chromeBinary", new DefaultCapabilityMapping("chrome.binary"));
        LEGACY_MAP.put("chromeSwitches", new DefaultCapabilityMapping("chrome.switches"));
        // htmlunit
        LEGACY_MAP.put("useJavaScript", new DefaultCapabilityMapping("javascriptEnabled"));

        // opera
        LEGACY_MAP.put("operaArguments", new DefaultCapabilityMapping("opera.arguments"));
        LEGACY_MAP.put("operaAutostart", new DefaultCapabilityMapping("opera.autostart"));
        LEGACY_MAP.put("operaBinary", new DefaultCapabilityMapping("opera.binary"));
        LEGACY_MAP.put("operaDisplay", new DefaultCapabilityMapping("opera.display"));
        LEGACY_MAP.put("operaIdle", new DefaultCapabilityMapping("opera.idle"));
        LEGACY_MAP.put("operaLauncher", new DefaultCapabilityMapping("opera.launcher"));
        LEGACY_MAP.put("operaLoggingFile", new DefaultCapabilityMapping("opera.logging.file"));
        LEGACY_MAP.put("operaLoggingLevel", new DefaultCapabilityMapping("opera.logging.level"));
        LEGACY_MAP.put("operaQuit", new LoggingCapabilityMapping("opera.no_quit", NegatingBooleanValueMapper.INSTANCE,
                "\"operaQuit\" value was negated and stored as \"opera.no_quit\" capability"));
        LEGACY_MAP.put("operaRestart", new LoggingCapabilityMapping("opera.no_restart", NegatingBooleanValueMapper.INSTANCE,
                "\"operaRestart\" value was negated and stored as \"opera.no_restart\" capability"));
        LEGACY_MAP.put("operaPort", new DefaultCapabilityMapping("opera.port"));
        LEGACY_MAP.put("operaProduct", new DefaultCapabilityMapping("opera.product"));
        LEGACY_MAP.put("operaProfile", new DefaultCapabilityMapping("opera.profile"));
    }

    public static boolean isLegacy(String propertyName) {
        return LEGACY_MAP.containsKey(propertyName);
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

    private static class DefaultCapabilityMapping implements CapabilityMapping {
        private final String capabilityName;
        private final ValueMapper<?> mapper;

        public DefaultCapabilityMapping(String capabilityName) {
            this(capabilityName, StringValueMapper.INSTANCE);
        }

        public DefaultCapabilityMapping(String capabilityName, ValueMapper<?> mapper) {
            this.capabilityName = capabilityName;
            this.mapper = mapper;
        }

        public String remapKey(String fieldName) {

            log.log(Level.WARNING,
                    "Configuration property \"{0}\" is deprecated, please replace it with capability based property \"{1}\" instead.",
                    new Object[] { fieldName, capabilityName });

            return capabilityName;
        }

        public String remapValue(String value) {
            return ((Object) mapper.transform(value)).toString();
        }
    }

    private static class LoggingCapabilityMapping extends DefaultCapabilityMapping {

        private final String loggingMessage;

        public LoggingCapabilityMapping(String capabilityName, ValueMapper<?> mapper, String loggingMessage) {
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
