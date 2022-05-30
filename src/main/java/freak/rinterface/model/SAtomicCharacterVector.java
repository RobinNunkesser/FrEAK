/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import java.util.Iterator;
import java.util.Vector;

public class SAtomicCharacterVector extends SAbstractAtomicVector {
private Vector<String> values;
	
/**
 * 
 */
public SAtomicCharacterVector() {
	super();
	values=new Vector<String>();
}

public String[] getValues() {
	Iterator<String> it=values.iterator();
	String[] returnValue = new String[values.size()];
	int i=0;
	while (it.hasNext()) {
		returnValue[i]=it.next();
		i++;
	}
	return returnValue;	
}

@Override
public String getJNISignature() {
	// TODO Auto-generated method stub
	return "[S";
}

public void add(String value) {
	values.add(new String(value));
}
}
