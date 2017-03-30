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
package org.jboss.arquillian.drone.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.drone.api.annotation.DroneLifecycle;
import org.jboss.arquillian.drone.api.annotation.Qualifier;

/**
 * SecurityActions
 * <p/>
 * A set of privileged actions that are not to leak out of this package
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
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

    /**
     * Obtains the Thread Context ClassLoader
     */
    static ClassLoader getThreadContextClassLoader() {
        return AccessController.doPrivileged(GetTcclAction.INSTANCE);
    }

    /**
     * Obtains the Constructor specified from the given Class and argument types
     *
     * @throws NoSuchMethodException
     */
    static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... argumentTypes) throws
        NoSuchMethodException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor<?>>() {
                public Constructor<?> run() throws NoSuchMethodException {
                    return clazz.getConstructor(argumentTypes);
                }
            });
        }
        // Unwrap
        catch (final PrivilegedActionException pae) {
            final Throwable t = pae.getCause();
            // Rethrow
            if (t instanceof NoSuchMethodException) {
                throw (NoSuchMethodException) t;
            } else {
                // No other checked Exception thrown by Class.getConstructor
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

    /**
     * Create a new instance by finding a constructor that matches the argumentTypes signature using the arguments for
     * instantiation.
     *
     * @param className
     *     Full classname of class to create
     * @param argumentTypes
     *     The constructor argument types
     * @param arguments
     *     The constructor arguments
     *
     * @return a new instance
     *
     * @throws IllegalArgumentException
     *     if className, argumentTypes, or arguments are null
     * @throws RuntimeException
     *     if any exceptions during creation
     * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
     * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
     */
    static <T> T newInstance(final String className, final Class<?>[] argumentTypes, final Object[] arguments,
        final Class<T> expectedType) {
        if (className == null) {
            throw new IllegalArgumentException("ClassName must be specified");
        }
        if (argumentTypes == null) {
            throw new IllegalArgumentException("ArgumentTypes must be specified. Use empty array if no arguments");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments must be specified. Use empty array if no arguments");
        }
        final Object obj;
        try {
            final ClassLoader tccl = getThreadContextClassLoader();
            final Class<?> implClass = Class.forName(className, false, tccl);
            Constructor<?> constructor = getConstructor(implClass, argumentTypes);
            obj = constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeException("Could not create new instance of " + className + ", " +
                "missing package from classpath?", e);
        }

        // Cast
        try {
            return expectedType.cast(obj);
        } catch (final ClassCastException cce) {
            // Reconstruct so we get some useful information
            throw new ClassCastException("Incorrect expected type, " + expectedType.getName() + ", defined for "
                + obj.getClass().getName());
        }
    }

    static boolean isClassPresent(String name) {
        try {
            ClassLoader classLoader = getThreadContextClassLoader();
            classLoader.loadClass(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static Map<Integer, Annotation[]> getParametersWithAnnotation(final Method method,
        final Class<? extends Annotation> annotationClass) {

        Map<Integer, Annotation[]> declaredParameters = AccessController
            .doPrivileged(new PrivilegedAction<Map<Integer, Annotation[]>>() {

                @Override
                public Map<Integer, Annotation[]> run() {
                    Map<Integer, Annotation[]> foundParameters = new LinkedHashMap<Integer, Annotation[]>();

                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Annotation[][] parameterAnnotations = method.getParameterAnnotations();

                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (isAnnotationPresent(parameterAnnotations[i], annotationClass)) {
                            foundParameters.put(i, parameterAnnotations[i]);
                        }
                    }
                    return foundParameters;
                }
            });
        return declaredParameters;
    }

    static List<Field> getFieldsWithAnnotation(final Class<?> source, final Class<? extends Annotation>
        annotationClass) {
        List<Field> declaredAccessableFields = AccessController.doPrivileged(new PrivilegedAction<List<Field>>() {
            public List<Field> run() {
                List<Field> foundFields = new ArrayList<Field>();
                Class<?> nextSource = source;
                while (nextSource != Object.class) {
                    for (Field field : nextSource.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotationClass)) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            foundFields.add(field);
                        }
                    }
                    nextSource = nextSource.getSuperclass();
                }
                return foundFields;
            }
        });
        return declaredAccessableFields;
    }

    static boolean isAnnotationPresent(final Annotation[] annotations, final Class<? extends Annotation> needle) {
        return findAnnotation(annotations, needle) != null;
    }

    @SuppressWarnings("unchecked")
    static <T extends Annotation> T findAnnotation(final Annotation[] annotations, final Class<T> needle) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == needle) {
                return (T) annotation;
            }
        }
        return null;
    }

    static Annotation[] getAnnotations(final AnnotatedElement element) {
        return AccessController.doPrivileged(new PrivilegedAction<Annotation[]>() {
            @Override
            public Annotation[] run() {
                return element.getDeclaredAnnotations();
            }
        });
    }

    static <T extends Annotation> T getAnnotation(final AnnotatedElement element, final Class<T> annotationClass) {
        return AccessController.doPrivileged(new PrivilegedAction<T>() {
            @Override
            public T run() {
                return element.getAnnotation(annotationClass);
            }
        });
    }

    static Class<? extends Annotation> getQualifier(final Field field) {
        Annotation[] annotations = AccessController.doPrivileged(new PrivilegedAction<Annotation[]>() {
            public Annotation[] run() {
                return field.getAnnotations();
            }
        });

        return getQualifier(annotations);
    }

    static Class<? extends Annotation> getScope(final AnnotatedElement element) {
        Annotation[] annotations = AccessController.doPrivileged(new PrivilegedAction<Annotation[]>() {
            @Override
            public Annotation[] run() {
                return element.getAnnotations();
            }
        });

        return getScope(annotations);
    }

    static Object getFieldValue(final Object instance, final Field field) {
        try {
            Object value = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws IllegalArgumentException, IllegalAccessException {
                    return field.get(instance);
                }
            });
            return value;
        } catch (PrivilegedActionException e) {
            final Throwable t = e.getCause();
            // Rethrow
            if (t instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) t;
            } else if (t instanceof IllegalAccessException) {
                throw new IllegalStateException("Unable to get field value of " + field.getName() + " due to: "
                    + t.getMessage(), t.getCause());
            } else {
                // No other checked Exception thrown by Class.getConstructor
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

    static void setFieldValue(final Object instance, final Field field, final Object value) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws IllegalArgumentException, IllegalAccessException {
                    field.set(instance, value);
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            final Throwable t = e.getCause();
            // Rethrow
            if (t instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) t;
            } else if (t instanceof IllegalAccessException) {
                throw new IllegalStateException("Unable to set field value of " + field.getName() + " due to: "
                    + t.getMessage(), t.getCause());
            } else {
                // No other checked Exception thrown by Class.getConstructor
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

    static List<Class<? extends Annotation>> findAnnotationAnnotatedWith(Class<? extends Annotation> parentAnnotation,
        Annotation[] annotations) {
        List<Class<? extends Annotation>> candidates = new ArrayList<Class<? extends Annotation>>();

        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(parentAnnotation)) {
                candidates.add(annotation.annotationType());
            }
        }

        return candidates;
    }

    static Class<? extends Annotation> getQualifier(Annotation[] annotations) {
        List<Class<? extends Annotation>> candidates = findAnnotationAnnotatedWith(Qualifier.class, annotations);

        if (candidates.isEmpty()) {
            return Default.class;
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        }

        throw new IllegalStateException("Unable to determine Qualifier, multiple (" + candidates.size()
            + ") Qualifier annotations were present");
    }

    static Class<? extends Annotation> getScope(Annotation[] annotations) {
        List<Class<? extends Annotation>> candidates = findAnnotationAnnotatedWith(DroneLifecycle.class, annotations);

        if (candidates.isEmpty()) {
            return null;
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        }

        throw new IllegalStateException("Unable to determine Lifecycle, multiple (" + candidates.size() + ") Lifecycle " +
            "annotations were present");
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

    // -------------------------------------------------------------------------------||
    // Inner Classes
    // ----------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * Single instance to get the TCCL
     */
    private enum GetTcclAction implements PrivilegedAction<ClassLoader> {
        INSTANCE;

        public ClassLoader run() {
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
