/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.view.plotter;

import java.io.*;

/**
 * Contains all individuals with the same x coordinate.  Two <code>PointList</code>s can be compared by their x values.
 * @author  Dirk
 */
class PointList implements Serializable {

	int x;
	/**
	 * @uml.property  name="data"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	Point[] data;

	public PointList(int x, Point[] data) {
		this.x = x;
		this.data = data;
	}

	public boolean equals(Object o) {
		if (!(o instanceof PointList))
			return super.equals(o);
		return x == ((PointList)o).x;
	}

	public int hashCode() {
		return x;
	}
}