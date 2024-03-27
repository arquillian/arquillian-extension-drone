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
package org.jboss.arquillian.drone.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.drone.configuration.mapping.BooleanValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.IntegerValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.MapValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.ValueMapper;

/**
 * Maps properties to ValuesMappers so that the property value can be cast to desired type.
 *
 * @author <a href="mailto:trepel@redhat.com">Tomas Repel</a>
 */
public class CapabilityTypeMapper {

    private static final Logger log = Logger.getLogger(CapabilityTypeMapper.class.getName());

    // FIXME this should be in SPI with a proper event MODEL
    private static final Map<String, ValueMapper<?>> TYPE_MAP;

    static {
        TYPE_MAP = new HashMap<String, ValueMapper<?>>();

        // Common read-write capabilities (https://code.google.com/p/selenium/wiki/Capabilities#Read-write_capabilities)
        TYPE_MAP.put("acceptSslCerts", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("applicationCacheEnabled", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("browserConnectionEnabled", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("databaseEnabled", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("elementScrollBehavior", IntegerValueMapper.INSTANCE);
        TYPE_MAP.put("javascriptEnabled", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("locationContextEnabled", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("nativeEvents", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("rotatable", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("webStorageEnabled", BooleanValueMapper.INSTANCE);

        // Firefox
        TYPE_MAP.put("loggingPrefs", MapValueMapper.INSTANCE);

        // Internet Explorer (https://code.google.com/p/selenium/wiki/Capabilities#IE_specific)
        TYPE_MAP.put("browserAttachTimeout", IntegerValueMapper.INSTANCE);
        TYPE_MAP.put("enableElementCacheCleanup", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("enablePersistentHover", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("ignoreProtectedModeSettings", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("ignoreZoomSetting", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("ie.ensureCleanSession", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("ie.forceCreateProcessApi", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("ie.setProxyByServer", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("ie.usePerProcessProxy", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("requireWindowFocus", BooleanValueMapper.INSTANCE);
        TYPE_MAP.put("silent", BooleanValueMapper.INSTANCE);
    }

    public static boolean isCastNeeded(String propertyName) {
        return TYPE_MAP.containsKey(propertyName);
    }

    public static Object createTypedObjectFromString(String propertyName, String value) {
        if (TYPE_MAP.containsKey(propertyName)) {
            try {
                return TYPE_MAP.get(propertyName).transform(value);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The property '"
                    + propertyName
                    + "' has value '"
                    + value
                    + "' that cannot be transformed using '"
                    + TYPE_MAP.get(propertyName).getClass().getSimpleName()
                    + "'.", e);
            }
        } else {
            log.log(Level.WARNING, "The type for property \"{0}\" cannot be determined, java.lang.String is used");
            return value;
        }
    }
}
