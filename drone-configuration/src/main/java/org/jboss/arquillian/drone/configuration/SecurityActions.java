/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * SecurityActions
 * <p>
 * A set of privileged actions that are not to leak out of this package
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @version $Revision: $
 */
final class SecurityActions {

    // -------------------------------------------------------------------------------||
    // Constructor
    // ------------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * No instantiation
     */
    private SecurityActions() {
        throw new UnsupportedOperationException("No instantiation");
    }

    // -------------------------------------------------------------------------------||
    // Utility Methods
    // --------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    static Map<String, Field> getAccessableFields(final Class<?> source) {
        Map<String, Field> declaredAccessableFields =
            AccessController.doPrivileged(new PrivilegedAction<Map<String, Field>>() {
                public Map<String, Field> run() {
                    Map<String, Field> foundFields = new LinkedHashMap<String, Field>();
                    for (Field field : source.getDeclaredFields()) {
                        // omit final fields
                        if (Modifier.isFinal(field.getModifiers())) {
                            continue;
                        }

                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        foundFields.put(field.getName(), field);
                    }
                    return foundFields;
                }
            });
        return declaredAccessableFields;
    }

    static Map<String, String> getProperties(final String prefix) {
        try {
            Map<String, String> value =
                AccessController.doPrivileged(new PrivilegedExceptionAction<Map<String, String>>() {
                    public Map<String, String> run() {
                        Properties props = System.getProperties();
                        Map<String, String> subset = new LinkedHashMap<String, String>();
                        for (Map.Entry<Object, Object> entry : props.entrySet()) {
                            String name = entry.getKey().toString();
                            if (name.startsWith(prefix)) {
                                subset.put(name, entry.getValue().toString());
                            }
                        }
                        return subset;
                    }
                });
            return value;
        }
        // Unwrap
        catch (final PrivilegedActionException pae) {
            final Throwable t = pae.getCause();
            // Rethrow
            if (t instanceof SecurityException) {
                throw (SecurityException) t;
            }
            if (t instanceof NullPointerException) {
                throw (NullPointerException) t;
            } else if (t instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) t;
            } else {
                // No other checked Exception thrown by System.getProperty
                try {
                    throw (RuntimeException) t;
                }
                // Just in case we've really messed up
                catch (final ClassCastException cce) {
                    throw new RuntimeException("Obtained unchecked Exception; this code should never be reached", t);
                }
            }
        }
    }

    // Finds all fields of source class that are of generic type implementing java.util.Map parameterized by parameters.
    static List<Field> getMapFields(final Class<?> source, final Class<?>... parameters) {
        try {
            List<Field> value = AccessController.doPrivileged(new PrivilegedExceptionAction<List<Field>>() {
                public List<Field> run() {
                    Map<String, Field> accesableFields = getAccessableFields(source);

                    List<Field> fields = new ArrayList<Field>();
                    for (Field field : accesableFields.values()) {
                        // check for map
                        if (Map.class.isAssignableFrom(field.getType())) {
                            Type genericType = field.getGenericType();
                            boolean parameterMatched = true;
                            // check generic parameters
                            if (genericType instanceof ParameterizedType) {
                                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                                if (parameterizedType.getActualTypeArguments().length != parameters.length) {
                                    parameterMatched = false;
                                } else {
                                    for (int i = 0; i < parameterizedType.getActualTypeArguments().length; i++) {
                                        Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[i];
                                        if (!actualType.equals(parameters[i])) {
                                            parameterMatched = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (parameterMatched == true) {
                                fields.add(field);
                            }
                        }
                    }
                    return fields;
                }
            });
            return value;
        }
        // Unwrap
        catch (final PrivilegedActionException pae) {
            final Throwable t = pae.getCause();
            // Rethrow
            if (t instanceof SecurityException) {
                throw (SecurityException) t;
            }
            if (t instanceof NullPointerException) {
                throw (NullPointerException) t;
            } else if (t instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) t;
            } else {
                // No other checked Exception thrown by System.getProperty
                try {
                    throw (RuntimeException) t;
                }
                // Just in case we've really messed up
                catch (final ClassCastException cce) {
                    throw new RuntimeException("Obtained unchecked Exception; this code should never be reached", t);
                }
            }
        }
    }

    static String getProperty(final String key) {
        try {
            String value = AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                public String run() {
                    return System.getProperty(key);
                }
            });
            return value;
        }
        // Unwrap
        catch (final PrivilegedActionException pae) {
            final Throwable t = pae.getCause();
            // Rethrow
            if (t instanceof SecurityException) {
                throw (SecurityException) t;
            }
            if (t instanceof NullPointerException) {
                throw (NullPointerException) t;
            } else if (t instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) t;
            } else {
                // No other checked Exception thrown by System.getProperty
                try {
                    throw (RuntimeException) t;
                }
                // Just in case we've really messed up
                catch (final ClassCastException cce) {
                    throw new RuntimeException("Obtained unchecked Exception; this code should never be reached", t);
                }
            }
        }
    }
}
