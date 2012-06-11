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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Utility which maps Arquillian Descriptor and System Properties to a configuration.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see DroneConfiguration
 */
public class ConfigurationMapper {
    private static final Logger log = Logger.getLogger(ConfigurationMapper.class.getName());

    private static final String MAP_SUFFIX = "Map";

    private ConfigurationMapper() {
        throw new InstantiationError();
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
     * Maps a configuration using System Properties
     *
     * @param <T> Type of the configuration
     * @param configuration Configuration object
     * @param qualifier Qualifier annotation
     * @return Configured configuration
     */
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
    static <T extends DroneConfiguration<T>> T mapFromNameValuePairs(T configuration, Map<String, String> nameValuePairs) {
        Map<String, Field> fields = SecurityActions.getAccessableFields(configuration.getClass());

        // map basic fields
        for (Map.Entry<String, String> nameValue : nameValuePairs.entrySet()) {
            String name = nameValue.getKey();
            // does not process any nameValue that ends with MAP_SUFFIX
            if (name.endsWith(MAP_SUFFIX)) {
                continue;
            }
            // map a field
            if (fields.containsKey(name)) {
                try {
                    Field f = fields.get(name);
                    f.set(configuration, convert(box(f.getType()), nameValue.getValue()));
                } catch (Exception e) {
                    throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName()
                            + ") for " + configuration.getClass().getName() + " from Arquillian Descriptor", e);
                }
            }
        }

        // map map-based fields
        for (Field f : fields.values()) {
            String name = f.getName();
            // we got a map
            if (name.endsWith(MAP_SUFFIX) && Map.class.isAssignableFrom(f.getType())) {
                Map<String, String> map = emulateMap(name.substring(0, name.lastIndexOf(MAP_SUFFIX)), nameValuePairs);
                try {
                    f.set(configuration, map);
                } catch (Exception e) {
                    throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName()
                            + ") for " + configuration.getClass().getName() + " from Arquillian Descriptor", e);
                }
            }
        }

        return configuration;
    }

    /**
     * Fills a map based on prefix.
     *
     *
     *
     * @param prefix Prefix
     * @param nameValuePairs Name value pairs
     * @return
     */
    static Map<String, String> emulateMap(String prefix, Map<String, String> nameValuePairs) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String> nameValueEntry : nameValuePairs.entrySet()) {
            String propertyName = nameValueEntry.getKey();
            if (nameValueEntry.getKey().startsWith(prefix)) {
                StringBuilder key = new StringBuilder(propertyName.substring(prefix.length()));
                key.setCharAt(0, Character.toLowerCase(key.charAt(0)));
                map.put(keyTransform(key), nameValueEntry.getValue());
            }
        }

        return map;
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
     * Parses System properties into property name - value pairs
     *
     * @param descriptorQualifier A qualifier used for extension configuration in the descriptor
     * @param qualifierName Name of the qualifier passed
     */
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

            // trim name
            name = name.contains(fullQualifiedPrefix) ? name.substring(fullQualifiedPrefix.length()) : name
                    .substring(qualifiedPrefix.length());
            // transform to a field name

            nameValuePairs.put(keyTransformReverse(name), entry.getValue());
        }

        return nameValuePairs;

    }

    /**
     * Maps a field name to a property.
     *
     * Replaces camel case with a dot ('.') and lower case character, replaces other non digit and non letter characters with a
     * dot (').
     *
     * @param fieldName The name of field
     * @return Corresponding property name
     */
    static String keyTransform(String fieldName) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('.').append(Character.toLowerCase(c));
            } else if (!Character.isLetterOrDigit(c)) {
                sb.append('.');
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    static String keyTransform(StringBuilder fieldName) {
        return keyTransform(fieldName.toString());
    }

    /**
     * Maps a property to a field name.
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

    /**
     * A helper boxing method. Returns boxed class for a primitive class
     *
     * @param primitive A primitive class
     * @return Boxed class if class was primitive, unchanged class in other cases
     */
    static Class<?> box(Class<?> primitive) {
        if (!primitive.isPrimitive()) {
            return primitive;
        }

        if (int.class.equals(primitive)) {
            return Integer.class;
        } else if (long.class.equals(primitive)) {
            return Long.class;
        } else if (float.class.equals(primitive)) {
            return Float.class;
        } else if (double.class.equals(primitive)) {
            return Double.class;
        } else if (short.class.equals(primitive)) {
            return Short.class;
        } else if (boolean.class.equals(primitive)) {
            return Boolean.class;
        } else if (char.class.equals(primitive)) {
            return Character.class;
        } else if (byte.class.equals(primitive)) {
            return Byte.class;
        }

        throw new IllegalArgumentException("Unknown primitive type " + primitive);
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
    static <T> T convert(Class<T> clazz, String value) {
        if (String.class.equals(clazz)) {
            return clazz.cast(value);
        } else if (Integer.class.equals(clazz)) {
            return clazz.cast(Integer.valueOf(value));
        } else if (Double.class.equals(clazz)) {
            return clazz.cast(Double.valueOf(value));
        } else if (Long.class.equals(clazz)) {
            return clazz.cast(Long.valueOf(value));
        } else if (Boolean.class.equals(clazz)) {
            return clazz.cast(Boolean.valueOf(value));
        } else if (URL.class.equals(clazz)) {
            try {
                return clazz.cast(new URI(value).toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            }
        } else if (URI.class.equals(clazz)) {
            try {
                return clazz.cast(new URI(value));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Unable to convert value " + value + " to URL", e);
            }
        }

        throw new IllegalArgumentException("Unable to convert value " + value + "to a class: " + clazz.getName());
    }
}