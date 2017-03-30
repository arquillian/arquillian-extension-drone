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
package org.jboss.arquillian.drone.configuration.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Mapper for Map<String, String>
 *
 * @author <a href="mailto:trepel@redhat.com">Tomas Repel</a>
 */
public enum MapValueMapper implements ValueMapper<Map<String, String>> {

    INSTANCE;

    @Override
    public boolean handles(Class<?> type, Class<?>... parameters) {
        if (!Map.class.isAssignableFrom(type)) {
            return false;
        }

        if (parameters.length != 2) {
            return false;
        }

        for (Class<?> param : parameters) {
            if (!param.equals(String.class)) {
                return false;
            }
        }
        return true;
    }

    /**
     * It expects the string of format key=value,key1=value1,key2=value2.
     *
     * @param value
     *     - the string to be parsed and transformed to Map.
     */
    @Override
    public Map<String, String> transform(String value) throws IllegalArgumentException {
        Map<String, String> map = new HashMap<String, String>();

        try {
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreTokens()) {
                StringTokenizer subSt = new StringTokenizer(st.nextToken().trim(), "=");
                map.put(subSt.nextToken().trim(), subSt.nextToken().trim());
            }
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Invalid format of '"
                + value
                + "', cannot be parsed to Map<String,String>. The expected format is 'key=value,key1=value1,key2=value2'",
                e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Invalid format of '"
                + value
                + "', cannot be parsed to Map<String,String>. The expected format is 'key=value,key1=value1,key2=value2'",
                e);
        }

        return map;
    }

}
