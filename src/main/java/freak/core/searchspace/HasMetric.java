/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.searchspace;

import freak.core.population.*;

/**
 * This interface can be implemented by search spaces in order to make a metric
 * available. A metric allows the use of additional operators and fitness
 * transformers on the search space, e.g. fitness sharing.
 * 
 * The metric is assumed to be symmetric, so that
 * <code>getDistance(x, y) == getDistance(y, x)</code>
 * for alle genotypes <code>x, y</code>.
 * 
 * @author Heiko
 */
public interface HasMetric {

	/**
	 * This method calculates the distance between two genotypes.
	 * All implementations have to ensure that
	 * <code>getDistance(x, y) == getDistance(y, x)</code>. 
	 * for alle genotypes <code>x, y</code>.
	 *  
	 * @return the distance between the two given genotypes. 
	 */
	double getDistance(Genotype gt1, Genotype gt2);
}
