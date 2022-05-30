/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import java.util.Iterator;
import java.util.Vector;

public class SAtomicDoubleVector extends SAbstractAtomicVector {
private Vector<Double> values;
	
/**
 * 
 */
public SAtomicDoubleVector() {
	super();
	values=new Vector<Double>();
}

public double[] getValues() {
	Iterator<Double> it=values.iterator();
	double[] returnValue = new double[values.size()];
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
	return "[D";
}

public void add(double value) {
	values.add(new Double(value));
}
}
