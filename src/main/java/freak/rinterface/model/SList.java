/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import java.util.Iterator;
import java.util.Vector;

public class SList extends SAbstractList {
protected Vector<SAbstractAtomicVector> values;

/**
 * 
 */
public SList() {
	super();
	values=new Vector<SAbstractAtomicVector>();
}

public SAbstractAtomicVector[] getValues() {
	Iterator<SAbstractAtomicVector> it=values.iterator();
	SAbstractAtomicVector[] returnValue = new SAbstractAtomicVector[values.size()];
	int i=0;
	while (it.hasNext()) {
		returnValue[i]=it.next();
		i++;
	}
	return returnValue;	
}

public void add(SAbstractAtomicVector value) {
	values.add(value);
}


}
