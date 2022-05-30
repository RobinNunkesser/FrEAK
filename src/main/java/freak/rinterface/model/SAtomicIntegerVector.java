/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import java.util.Iterator;
import java.util.Vector;

public class SAtomicIntegerVector extends SAbstractAtomicVector {
private Vector<Integer> values;
	
/**
 * 
 */
public SAtomicIntegerVector() {
	super();
	values=new Vector<Integer>();
}

public int[] getValues() {
	Iterator<Integer> it=values.iterator();
	int[] returnValue = new int[values.size()];
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
	return "[I";
}

public void add(int value) {
	values.add(new Integer(value));
}
}
