/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List of classes that can be serialized and deserialized. This implementation reduces possibility of a security breach.
 * See
 * ARQ-1450 for more details.
 */
public class SerializationWhitelist {
    private static final Logger log = Logger.getLogger(SerializationWhitelist.class.getName());

    private final Set<String> whitelist;

    public SerializationWhitelist() {
        this.whitelist = new HashSet<String>();

        // add java.lang
        whitelist.add(Boolean.class.getName());
        whitelist.add(Byte.class.getName());
        whitelist.add(Character.class.getName());
        whitelist.add(Character.Subset.class.getName());
        whitelist.add(Character.UnicodeBlock.class.getName());
        whitelist.add(Class.class.getName());
        whitelist.add(ClassLoader.class.getName());
        whitelist.add(Double.class.getName());
        whitelist.add(Enum.class.getName());
        whitelist.add(Float.class.getName());
        whitelist.add(InheritableThreadLocal.class.getName());
        whitelist.add(Integer.class.getName());
        whitelist.add(Long.class.getName());
        whitelist.add(Math.class.getName());
        whitelist.add(Number.class.getName());
        whitelist.add(Object.class.getName());
        whitelist.add(Package.class.getName());
        whitelist.add(Process.class.getName());
        whitelist.add(ProcessBuilder.class.getName());
        whitelist.add(Runtime.class.getName());
        whitelist.add(RuntimePermission.class.getName());
        whitelist.add(SecurityManager.class.getName());
        whitelist.add(Short.class.getName());
        whitelist.add(StackTraceElement.class.getName());
        whitelist.add(StrictMath.class.getName());
        whitelist.add(String.class.getName());
        whitelist.add(StringBuffer.class.getName());
        whitelist.add(StringBuilder.class.getName());
        whitelist.add(System.class.getName());
        whitelist.add(Thread.class.getName());
        whitelist.add(ThreadGroup.class.getName());
        whitelist.add(ThreadLocal.class.getName());
        whitelist.add(Throwable.class.getName());
        whitelist.add(Void.class.getName());

        // add primitive
        whitelist.add("B");
        whitelist.add("C");
        whitelist.add("D");
        whitelist.add("F");
        whitelist.add("I");
        whitelist.add("J");
        whitelist.add("S");
        whitelist.add("Z");

        // add arrays of primitives
        whitelist.add("[B");
        whitelist.add("[C");
        whitelist.add("[D");
        whitelist.add("[F");
        whitelist.add("[I");
        whitelist.add("[J");
        whitelist.add("[S");
        whitelist.add("[Z");
    }

    public SerializationWhitelist enableClass(String fqcn) {
        whitelist.add(fqcn);
        return this;
    }

    public boolean isEnabled(String fqcn) {

        try {
            Class<?> clazz = Class.forName(fqcn);
            if (!Serializable.class.isAssignableFrom(clazz)) {
                log.log(Level.FINER, "Ignoring class {0} from Serialization, it is not Serializable", fqcn);
                return false;
            }
            if (!whitelist.contains(fqcn)) {
                log.log(Level.FINER, "Ignoring class {0} from Serialization, it was not whitelisted", fqcn);
                return false;
            }
        } catch (ClassNotFoundException e) {
            log.log(Level.WARNING, "Ignoring class {0} from Serialization, it was not found on classpath", fqcn);
            return false;
        }

        // class was in the whitelist and it is serializable, proceed
        return true;
    }
}
