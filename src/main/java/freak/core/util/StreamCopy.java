/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.util;

import java.io.*;

/**
 * @author Stefan
 */
public class StreamCopy {
	/**
	 * Creates a deep copy of object by serializing and then deserializing it.
	 *
	 * @exception NotSerializableException if an object, reachable by a chain of references from the source, is not serializable.
	 */
	public static Serializable copy(Serializable source) throws NotSerializableException {
		return read(serialize(source));
	}

	public static byte[] serialize(Serializable source) throws NotSerializableException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(source);
			return baos.toByteArray();
		} catch (NotSerializableException exc) {
			throw exc;
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	public static Serializable read(byte[] serializedObject) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
			ObjectInputStream ois = new ObjectInputStream(bais);

			return (Serializable)ois.readObject();
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		} catch (ClassNotFoundException exc) {
			throw new RuntimeException(exc);
		}
	}
}
