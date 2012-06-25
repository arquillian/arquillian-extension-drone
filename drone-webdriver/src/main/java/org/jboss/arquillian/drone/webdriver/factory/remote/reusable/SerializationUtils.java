/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;

/**
 * The utility methods for serializing / deserializing objects
 *
 * @author <a href="mailto:lryc@redhat.com">Lukas Fryc</a>
 */
public class SerializationUtils {

    /**
     * Takes serializable object and serializes it to the byte array
     *
     * @param object object to serialize
     * @return the byte array representing serializable object
     *
     * @throws InvalidClassException Something is wrong with a class used by serialization.
     * @throws NotSerializableException Some object to be serialized does not implement the java.io.Serializable interface.
     * @throws IOException Any exception thrown by the underlying OutputStream.
     */
    public static byte[] serializeToBytes(Serializable object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        return baos.toByteArray();
    }

    /**
     * Takes byte array representing serialized object and constructs its object form
     *
     * @param classType Type of the returned object
     * @param serializedObject byte array representing serialized object
     * @return deserialized object
     * @throws ClassNotFoundException Class of a serialized object cannot be found.
     * @throws InvalidClassException Something is wrong with a class used by serialization.
     * @throws StreamCorruptedException Control information in the stream is inconsistent.
     * @throws OptionalDataException Primitive data was found in the stream instead of objects.
     * @throws IOException Any of the usual Input/Output related exceptions.
     */
    public static <T extends Serializable> T deserializeFromBytes(Class<T> classType, byte[] serializedObject)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return classType.cast(ois.readObject());
    }

}
