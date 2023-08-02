/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.webdriver.factory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.configuration.mapping.ValueMapper;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.Capabilities;

public class CapabilitiesOptionsMapper {

    private static final Gson GSON = new Gson();

    /**
     * Parses capabilities set in {@link Capabilities} and according to set-method names it sets the values into
     * corresponding variables of the given Object instance. It is expected that the parameters defined in arquillian.xml
     * file have a specific browserPrefix; after this prefix then there is the parameter name itself
     * (whole string has to be in camelcase)
     *
     * @param object
     *     An instance of an object the values should be set into
     * @param capabilities
     *     A {@link Capabilities} that contains parameters and its values set in arquillian.xml
     * @param browserPrefix
     *     A prefix the should the mapped parameters should start with
     */
    public static void mapCapabilities(Object object, Capabilities capabilities, String browserPrefix) {

        Method[] methods = object.getClass().getMethods();
        List<String> processedMethods = new ArrayList<String>();

        for (Method method : methods) {
            if (isSetter(method)) {

                method.setAccessible(true);
                String methodName = method.getName();
                if (processedMethods.contains(methodName)) {
                    continue;
                }

                String propertyName = methodName.substring(3);
                String prefixedPropertyName = browserPrefix + propertyName;
                propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                String capability = null;
                try {
                    capability = (String) capabilities.getCapability(propertyName);
                    if (Validate.empty(capability)) {
                        capability = (String) capabilities.getCapability(prefixedPropertyName);
                    }
                } catch (ClassCastException thr) { }

                if (Validate.nonEmpty(capability)) {
                    try {
                        if (method.getParameterTypes().length == 1) {
                            Object converted = convert(method, capability);
                            if (converted != null) {
                                method.invoke(object, converted);
                                processedMethods.add(methodName);
                            }
                        } else if (shouldContainDictionaries(method)) {
                            handleDictionaries(object, method, capability);
                            processedMethods.add(methodName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void handleDictionaries(Object object, Method method, String capability)
        throws InvocationTargetException, IllegalAccessException {

        String trimmedCapability = StringUtils.trimMultiline(capability);
        JsonObject json = new JsonParser().parse(trimmedCapability).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            Object value = null;
            if (entry.getValue().isJsonObject()) {
                value = GSON.fromJson(entry.getValue(), type);

            } else if (entry.getValue().isJsonPrimitive()) {
                value = convertJsonPrimitive2Java((JsonPrimitive) entry.getValue());
            }
            method.invoke(object, key, value);
        }
    }

    private static Object convertJsonPrimitive2Java(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        } else if (primitive.isNumber()) {
            return primitive.getAsNumber();
        } else if (primitive.isString()) {
            return primitive.getAsString();
        }
        throw new RuntimeException("Unhandled json primitive " + primitive);
    }

    private static boolean shouldContainDictionaries(Method method) {
        return
            (method.getParameterTypes().length == 2)
                &&
                (method.getParameterTypes()[0].isAssignableFrom(String.class)
                    || method.getParameterTypes()[0].isAssignableFrom(Object.class))
                &&
                (method.getParameterTypes()[1].isAssignableFrom(Object.class)
                    || method.getParameterTypes()[1].isAssignableFrom(Map.class));
    }

    private static Object convert(Method method, String capability) {
        Class<?> parameterType = method.getParameterTypes()[0];
        Object converted;

        if ((converted = convertToBooleanNumberStringOrFile(parameterType, capability)) != null) {
            return converted;
        } else if (parameterType.isArray()) {
            return handleArray(parameterType.getComponentType(), capability);
        } else if (parameterType.isAssignableFrom(List.class)) {
            return handleList(method, capability);
        }

        return null;
    }

    private static <T> T[] handleArray(Class<T> parameterType, String capability) {
        List<T> convertedList = getConvertedList(parameterType, capability);
        T[] array = (T[]) Array.newInstance(parameterType, convertedList.size());
        return convertedList.toArray(array);
    }

    private static Object handleList(Method method, String capability) {

        Type[] genericParameterTypes = method.getGenericParameterTypes();
        if (genericParameterTypes.length == 1) {
            Type type = genericParameterTypes[0];

            if (type instanceof ParameterizedType) {
                Type[] parameters = ((ParameterizedType) type).getActualTypeArguments();
                if (parameters.length == 1) {
                    return getConvertedList((Class<Object>) parameters[0], capability);
                }
            }
        }
        return null;
    }

    private static <T> List<T> getConvertedList(Class<T> parameter, String capability) {

        List<String> values = StringUtils.tokenize(capability);
        List<T> convertedList = new ArrayList<T>(values.size());
        for (String value : values) {

            convertedList.add(convertToBooleanNumberStringOrFile(parameter, value));
        }

        return convertedList;
    }

    private static <T> T convertToBooleanNumberStringOrFile(Class<T> clazz, String value) {
        for (ValueMapper<?> mapper : ConfigurationMapper.VALUE_MAPPERS) {
            if (mapper.handles(clazz)) {
                return (T) mapper.transform(value);
            }
        }

        return null;
    }

    private static boolean isSetter(Method candidate) {
        return candidate.getName().matches("^(set|add)[A-Z].*")
            && (candidate.getReturnType().equals(Void.TYPE) || candidate.getReturnType().equals(candidate.getDeclaringClass()))
            && candidate.getParameterTypes().length > 0;
    }
}
