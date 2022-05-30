/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.view.plotter;

import java.io.*;

/**
 * A class for simple two-dimensional points that can be compared by 
 * their y values.<br>
 * The class <code>java.awt.geom.Point2D.Double</code> didn't work here, the
 * x and y values were not deserialized properly. 
 * 
 * @author Dirk 
 */
class Point implements Comparable, Serializable {

	int xpos;
	int ypos;

	double x;
	double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public int compareTo(Object o) {
		if (o == null || !(o instanceof Point))
			return 0;
		// ensure consistency with equals
		if (this.equals(o))
			return 0;

		double diff = this.y - ((Point)o).y;
		// Points with equal y value do not result in 0 since 
		// TreeSet then assumes they were equal.
		if (diff == 0)
			return (this.x - ((Point)o).x < 0 ? -1 : 1);

		return (diff < 0 ? -1 : 1);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Point))
			return false;
		return (this.x == ((Point)o).x) && (this.y == ((Point)o).y);
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}

}