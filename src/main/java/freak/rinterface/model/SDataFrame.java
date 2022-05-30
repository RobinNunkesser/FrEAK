/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import java.util.Iterator;
import java.util.Vector;


public class SDataFrame extends SList {
private Vector<String> colnames;

public SDataFrame() {
	super();
	colnames=new Vector<String>();
}

public String[] getColnames() {
	Iterator<String> it=colnames.iterator();
	String[] returnValue = new String[colnames.size()];
	int i=0;
	while (it.hasNext()) {
		returnValue[i]=it.next();
		i++;
	}
	return returnValue;	
}

public void addColname(String colname) {
	colnames.add(colname);
}
public void addAllColnames(Vector<String> colname) {
	colnames.addAll(colname);
}

}
