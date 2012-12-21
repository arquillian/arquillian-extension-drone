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
package org.jboss.arquillian.drone.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.drone.configuration.legacy.LegacyPropertyToCapabilityMapper;
import org.jboss.arquillian.drone.configuration.mapping.BooleanValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.DoubleValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.IntegerValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.LongValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.StringValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.URIValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.URLValueMapper;
import org.jboss.arquillian.drone.configuration.mapping.ValueMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Utility which maps Arquillian Descriptor and System Properties to a Drone configuration.
 *
 * Configuration mapper does inspect a configuration for available fields and it tries to fill the values according to what is
 * provided in arquillian.xml or in system properties.
 *
 * All properties, which does not have an appropriate fields to be assigned, are stored in each available map, given that
 * configuration provides a {@code Map<String,String>} fields. Properties using their name as a key.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see DroneConfiguration
 */
public class ConfigurationMapper {
    private static final Logger log = Logger.getLogger(ConfigurationMapper.class.getName());

    // FIXME this should not be a static helper class but a proper observer on ArquillianDescriptor
    private ConfigurationMapper() {
        throw new InstantiationError();
    }

    // FIXME this should be in SPI with a proper event model
    public static final List<ValueMapper<?>> VALUE_MAPPERS;
    static {
        VALUE_MAPPERS = new ArrayList<ValueMapper<?>>();
        VALUE_MAPPERS.add(BooleanValueMapper.INSTANCE);
        VALUE_MAPPERS.add(DoubleValueMapper.INSTANCE);
        VALUE_MAPPERS.add(IntegerValueMapper.INSTANCE);
        VALUE_MAPPERS.add(LongValueMapper.INSTANCE);
        VALUE_MAPPERS.add(StringValueMapper.INSTANCE);
        VALUE_MAPPERS.add(URIValueMapper.INSTANCE);
        VALUE_MAPPERS.add(URLValueMapper.INSTANCE);
    }

    /**
     * Maps a configuration using Arquillian Descriptor file
     *
     * @param <T> Type of the configuration
     * @param descriptor Arquillian Descriptor
     * @param configuration Configuration object
     * @param qualifier Qualifier annotation
     * @return Configured configuration
     */
    public static <T extends DroneConfiguration<T>> T fromArquillianDescriptor(ArquillianDescriptor descriptor,
            T configuration, Class<? extends Annotation> qualifier) {
        Validate.notNull(descriptor, "Descriptor must not be null");
        Validate.notNull(configuration, "Configuration object must not be null");
        Validate.notNull(qualifier, "Qualifier object must not be null");

        String descriptorQualifier = configuration.getConfigurationName();
        String qualifierName = qualifier.getSimpleName().toLowerCase();

        Map<String, String> nameValuePairs = loadNameValuePairs(descriptor, descriptorQualifier, qualifierName);

        return mapFromNameValuePairs(configuration, nameValuePairs);
    }

    /**
     * Maps a configuration using System Properties.
     *
     * This method is <b>deprecated</b>. Use Arquillian Core property format instead.
     *
     * @param <T> Type of the configuration
     * @param configuration Configuration object
     * @param qualifier Qualifier annotation
     * @return Configured configuration
     */
    @Deprecated
    public static <T extends DroneConfiguration<T>> T fromSystemConfiguration(T configuration,
            Class<? extends Annotation> qualifier) {
        Validate.notNull(configuration, "Configuration object must not be null");
        Validate.notNull(qualifier, "Qualifier object must not be null");

        String descriptorQualifier = configuration.getConfigurationName();
        String qualifierName = qualifier.getSimpleName().toLowerCase();

        Map<String, String> nameValuePairs = loadNameValuePairs(descriptorQualifier, qualifierName);

        return mapFromNameValuePairs(configuration, nameValuePairs);
    }

    /**
     * Maps configuration values from Arquillian Descriptor
     *
     * @param <T> A type of configuration
     * @param configuration Configuration object
     * @return Configured configuration of given type
     */
    // @SuppressWarnings("unchecked")
    static <T extends DroneConfiguration<T>> T mapFromNameValuePairs(T configuration, Map<String, String> nameValuePairs) {
        Map<String, Field> fields = SecurityActions.getAccessableFields(configuration.getClass());

        // extract all Map<String,Object> in the configuration and initialize them
        List<Field> maps = SecurityActions.getMapFields(configuration.getClass(), String.class, Object.class);
        for (Field mapField : maps) {
            try {
                // get or create a map
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) mapField.get(configuration);
                if (map == null) {
                    map = new HashMap<String, Object>();
                }
                mapField.set(configuration, map);
            } catch (Exception e) {
                throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName()
                        + ") for " + configuration.getClass().getName() + " from Arquillian Descriptor", e);
            }
        }

        // map basic fields
        for (Map.Entry<String, String> nameValue : nameValuePairs.entrySet()) {
            String name = nameValue.getKey();

            // map a field which has a field directly available in the configuration
            if (fields.containsKey(name)) {
                injectField(configuration, maps, fields, name, nameValue.getValue());
            }
            // map a field which comes from a system property which has a field available in the configuration
            else if (fields.containsKey(keyTransformReverse(name))) {
                // we prefer new format arquillian.mockdriver.intField over arquillian.mockdriver.int.field
                log.log(Level.WARNING,
                        "The system property \"{0}\" used in Arquillian \"{1}\" configuration is deprecated, please rather use new format \"{2}\"",
                        new Object[] { name, configuration.getConfigurationName(), keyTransformReverse(name) });
                injectField(configuration, maps, fields, keyTransformReverse(name), nameValue.getValue());
            }
            // map a field which does not have this luck into all available maps in configuration
            else {
                injectMapProperty(configuration, maps, fields, name, nameValue.getValue());
            }

        }

        return configuration;
    }

    /**
     * Parses Arquillian Descriptor into property name - value pairs value
     *
     * @param descriptor An Arquillian Descriptor
     * @param descriptorQualifier A qualifier used for extension configuration in the descriptor
     * @param qualifierName Name of the qualifier passed
     */
    static Map<String, String> loadNameValuePairs(ArquillianDescriptor descriptor, String descriptorQualifier,
            String qualifierName) {
        String fullDescriptorQualifier = new StringBuilder(descriptorQualifier).append("-").append(qualifierName).toString();

        ExtensionDef match = null;
        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (fullDescriptorQualifier.equals(extension.getExtensionName())) {
                Map<String, String> nameValuePairs = extension.getExtensionProperties();
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Using <extension qualifier=\"" + extension.getExtensionName() + "\"> for Drone Configuration");
                }
                return nameValuePairs;
            } else if (descriptorQualifier.equals(extension.getExtensionName())) {
                match = extension;
            }
        }

        // found generic only
        if (match != null) {
            Map<String, String> nameValuePairs = match.getExtensionProperties();
            if (log.isLoggable(Level.FINE)) {
                log.fine("Using <extension qualifier=\"" + match.getExtensionName() + "\"> for Drone Configuration");
            }
            return nameValuePairs;
        }

        return Collections.emptyMap();
    }

    /**
     * Parses System properties into property name - value pairs.
     *
     * This method is now deprecated and should not be used anymore. Arquillian Core contains possibility to load configuration
     * from System properties.
     *
     * @param descriptorQualifier A qualifier used for extension configuration in the descriptor
     * @param qualifierName Name of the qualifier passed
     */
    @Deprecated
    static Map<String, String> loadNameValuePairs(String descriptorQualifier, String qualifierName) {
        String fullQualifiedPrefix = new StringBuilder("arquillian.").append(descriptorQualifier).append(".")
                .append(qualifierName).append(".").toString();

        String qualifiedPrefix = new StringBuilder("arquillian.").append(descriptorQualifier).append(".").toString();

        // try to get fully qualified prefix properties first
        Map<String, String> candidates = SecurityActions.getProperties(fullQualifiedPrefix);
        if (candidates.isEmpty()) {
            candidates.putAll(SecurityActions.getProperties(qualifiedPrefix));
        }

        // properly rename
        Map<String, String> nameValuePairs = new HashMap<String, String>(candidates.size());
        for (Map.Entry<String, String> entry : candidates.entrySet()) {
            String name = entry.getKey();

            // make a nasty warning that this will be removed
            if (log.isLoggable(Level.WARNING)) {
                String propertyName = name.contains(fullQualifiedPrefix) ? name.substring(fullQualifiedPrefix.length()) : name
                        .substring(qualifiedPrefix.length());

                String newSysPropertyKey = new StringBuilder("arq.extension.")
                        .append(name.contains(fullQualifiedPrefix) ? (descriptorQualifier + "-" + qualifierName)
                                : descriptorQualifier).append(".").append(propertyName).toString();

                log.log(Level.WARNING, "Old system property format \"{0}\" is deprecated. You should use \"{1}\" instead.",
                        new Object[] { name, newSysPropertyKey });
            }
            // trim name
            name = name.contains(fullQualifiedPrefix) ? name.substring(fullQualifiedPrefix.length()) : name
                    .substring(qualifiedPrefix.length());
            nameValuePairs.put(name, entry.getValue());
        }

        return nameValuePairs;

    }

    /**
     * Maps a property key to a field name.
     *
     * Replaces dot ('.') and lower case character with an upper case character
     *
     * @param propertyName The name of field
     * @return Corresponding field name
     */
    static String keyTransformReverse(String propertyName) {
        StringBuilder sb = new StringBuilder();

        boolean upperCaseFlag = false;
        for (int i = 0; i < propertyName.length(); i++) {
            char c = propertyName.charAt(i);
            if (c == '.') {
                upperCaseFlag = true;
            } else if (upperCaseFlag && Character.isLowerCase(c)) {
                sb.append(Character.toUpperCase(c));
                upperCaseFlag = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    static <T extends DroneConfiguration<T>> Field injectField(T configuration, List<Field> maps, Map<String, Field> fields,
            String fieldName, String value) {
        try {
            Field f = fields.get(fieldName);
            if (f.getAnnotation(Deprecated.class) != null) {
                log.log(Level.WARNING, "The property \"{0}\" used in Arquillian \"{1}\" configuration is deprecated.",
                        new Object[] { f.getName(), configuration.getConfigurationName() });
            }

            // remap the property into capability if this is a legacy one
            if (LegacyPropertyToCapabilityMapper.isLegacy(fieldName)) {
                String capabilityName = LegacyPropertyToCapabilityMapper.remapKey(fieldName);
                String capabilityValue = LegacyPropertyToCapabilityMapper.remapValue(fieldName, value);
                injectMapProperty(configuration, maps, fields, capabilityName, capabilityValue);
            }

            f.set(configuration, convert(f.getType(), value));
            return f;
        } catch (Exception e) {
            throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName() + ") for "
                    + configuration.getClass().getName() + " from Arquillian Descriptor", e);
        }

    }

    static <T extends DroneConfiguration<T>> void injectMapProperty(T configuration, List<Field> maps,
            Map<String, Field> fields, String propertyName, String value) {

        try {
            for (Field mapField : maps) {
                Object typedValue = value;
                if (CapabilityTypeMapper.isCastNeeded(propertyName)) {
                    typedValue = CapabilityTypeMapper.createTypedObjectFromString(propertyName, value);
                }
                // put property into a map
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) mapField.get(configuration);
                map.put(propertyName, typedValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName() + ") for "
                    + configuration.getClass().getName() + " from Arquillian Descriptor", e);
        }

    }

    /**
     * A helper converting method.
     *
     * Converts string to a class of given type
     *
     * @param <T> Type of returned value
     * @param clazz Type of desired value
     * @param value String value to be converted
     * @return Value converted to a appropriate type
     */
    static Object convert(Class<?> clazz, String value) {

        for (ValueMapper<?> mapper : VALUE_MAPPERS) {
            if (mapper.handles(clazz)) {
                return mapper.transform(value);
            }
        }

        throw new IllegalArgumentException("Unable to convert value " + value + "to a class: " + clazz.getName());
    }
}