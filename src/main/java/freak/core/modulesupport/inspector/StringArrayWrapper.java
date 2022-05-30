/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.modulesupport.inspector;

import java.io.*;

/**
 * @author   Michael
 */
public class StringArrayWrapper implements Serializable {

	private String[] stringArray;
	int arrayIndex;

	/** Creates a new instance of StringArrayWrapper */
	public StringArrayWrapper(String[] s, int index) {
		stringArray = s;
		arrayIndex = index;
	}

	/**
	 * @return  the stringArray
	 * @uml.property  name="stringArray"
	 */
	public String[] getStringArray() {
		return stringArray;
	}

	public int getIndex() {
		return arrayIndex;
	}
	
	public String toString() {
		if (arrayIndex < 0 || arrayIndex >= stringArray.length) {
			return "";
		}
		return stringArray[arrayIndex];
	}
}
