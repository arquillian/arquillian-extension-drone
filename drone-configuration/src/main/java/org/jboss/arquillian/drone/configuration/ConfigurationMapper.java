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
import java.util.List;
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
 * Configuration mapper does inspect a configuration for available fields and it tries to fill the values according to what is
 * provided in arquillian.xml or in system properties.
 *
 * All properties, which does not have an appropriate fields to be assigned, are stored in a map, given that configuration
 * provides a {@code Map<String,String>} field using their name as a key.
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * @see DroneConfiguration
 */
public class ConfigurationMapper {
    private static final Logger log = Logger.getLogger(ConfigurationMapper.class.getName());

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
    @SuppressWarnings("unchecked")
    static <T extends DroneConfiguration<T>> T mapFromNameValuePairs(T configuration, Map<String, String> nameValuePairs) {
        Map<String, Field> fields = SecurityActions.getAccessableFields(configuration.getClass());

        // extract all Map<String,String> in the configuration
        List<Field> maps = SecurityActions.getMapFields(configuration.getClass(), String.class, String.class);

        // map basic fields
        for (Map.Entry<String, String> nameValue : nameValuePairs.entrySet()) {
            String name = nameValue.getKey();

            // map a field which has a field directly available in the configuration
            if (fields.containsKey(name)) {
                try {
                    Field f = fields.get(name);
                    if (f.getAnnotation(Deprecated.class) != null) {
                        log.log(Level.WARNING, "The property \"{0}\" used Arquillian \"{1}\" configuration is deprecated.",
                                new Object[] { f.getName(), configuration.getConfigurationName() });
                    }
                    f.set(configuration, convert(box(f.getType()), nameValue.getValue()));
                } catch (Exception e) {
                    throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName()
                            + ") for " + configuration.getClass().getName() + " from Arquillian Descriptor", e);
                }
            }
            // map a field which comes from a system property which has a field available in the configuration
            else if (fields.containsKey(keyTransformReverse(name))) {
                try {
                    Field f = fields.get(keyTransformReverse(name));
                    if (f.getAnnotation(Deprecated.class) != null) {
                        log.log(Level.WARNING, "The property \"{0}\" used Arquillian \"{1}\" configuration is deprecated.",
                                new Object[] { f.getName(), configuration.getConfigurationName() });
                    }
                    f.set(configuration, convert(box(f.getType()), nameValue.getValue()));
                } catch (Exception e) {
                    throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName()
                            + ") for " + configuration.getClass().getName() + " from Arquillian Descriptor", e);
                }
            }
            // map a field which does not have this luck into all available maps in configuration
            else {
                for (Field mapField : maps) {
                    try {
                        // get or create a map
                        Map<String, String> map = (Map<String, String>) mapField.get(configuration);
                        if (map == null) {
                            map = new HashMap<String, String>();
                        }
                        map.put(name, nameValue.getValue());
                        mapField.set(configuration, map);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not map Drone configuration(" + configuration.getConfigurationName()
                                + ") for " + configuration.getClass().getName() + " from Arquillian Descriptor", e);
                    }
                }
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