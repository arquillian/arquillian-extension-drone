/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;

/**
 * The utility methods for serializing / deserializing objects
 *
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class SerializationUtils {

    private static final SerializationWhitelist whitelist = new SerializationWhitelist();

    static {

        whitelist.enableClass(URL.class.getName());
        whitelist.enableClass(URI.class.getName());
        whitelist.enableClass(Date.class.getName());
        whitelist.enableClass(File.class.getName());

        // collections
        whitelist.enableClass(LinkedHashMap.class.getName());
        whitelist.enableClass(HashMap.class.getName());
        whitelist.enableClass(Map.class.getName());

        // lists
        whitelist.enableClass(List.class.getName());
        whitelist.enableClass(ArrayList.class.getName());
        whitelist.enableClass(LinkedList.class.getName());

        // webdriver
        whitelist.enableClass(MutableCapabilities.class.getName());
        whitelist.enableClass(Capabilities.class.getName());
        whitelist.enableClass(Platform.class.getName());

        // internal implementation
        whitelist.enableClass(ReusableCapabilities.class.getName());
        whitelist.enableClass(ReusedSession.class.getName());
        whitelist.enableClass(ReusedSessionStore.class.getName());
        whitelist.enableClass(ReusedSessionStoreImpl.class.getName());
        whitelist.enableClass(ReusedSessionStoreImpl.ByteArray.class.getName());
        whitelist.enableClass(ReusedSessionStoreImpl.TimeStampedSession.class.getName());
        whitelist.enableClass(InitializationParameter.class.getName());
    }

    /**
     * Takes serializable object and serializes it to the byte array
     *
     * @param object
     *     object to serialize
     *
     * @return the byte array representing serializable object
     *
     * @throws InvalidClassException
     *     Something is wrong with a class used by serialization.
     * @throws NotSerializableException
     *     Some object to be serialized does not implement the java.io.Serializable interface.
     * @throws IOException
     *     Any exception thrown by the underlying OutputStream.
     */
    public static byte[] serializeToBytes(Serializable object) throws IOException {

        // we need to check whether class is whitelisted. If not, do not attempt to serialize it at after deserialization
        // we will not be able to adhere to equals(Object) contract
        if (!whitelist.isEnabled(object.getClass().getName())) {
            throw new InvalidClassException("Rejecting request to serialize " + object.getClass().getName()
                + ", class is not whitelisted for serialization.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        return baos.toByteArray();
    }

    /**
     * Takes byte array representing serialized object and constructs its object form
     *
     * @param classType
     *     Type of the returned object
     * @param serializedObject
     *     byte array representing serialized object
     *
     * @return deserialized object
     *
     * @throws ClassNotFoundException
     *     Class of a serialized object cannot be found.
     * @throws InvalidClassException
     *     Something is wrong with a class used by serialization.
     * @throws StreamCorruptedException
     *     Control information in the stream is inconsistent.
     * @throws OptionalDataException
     *     Primitive data was found in the stream instead of objects.
     * @throws IOException
     *     Any of the usual Input/Output related exceptions.
     */
    public static <T extends Serializable> T deserializeFromBytes(Class<T> classType, byte[] serializedObject)
        throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
        ObjectInputStream ois = new LookAheadObjectInputStream(bais, whitelist);
        return classType.cast(ois.readObject());
    }

    public static class LookAheadObjectInputStream extends ObjectInputStream {

        private final SerializationWhitelist whitelist;

        public LookAheadObjectInputStream(InputStream inputStream, SerializationWhitelist whitelist)
            throws IOException {
            super(inputStream);
            this.whitelist = whitelist;
        }

        /**
         * Only deserialize instances of our expected Bicycle class
         */
        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
            ClassNotFoundException {

            if (!whitelist.isEnabled(desc.getName())) {
                throw new InvalidClassException("Unauthorized deserialization attempt", desc.getName());
            }

            return super.resolveClass(desc);
        }
    }
}
