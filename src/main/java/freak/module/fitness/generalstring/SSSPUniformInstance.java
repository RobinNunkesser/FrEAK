/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.fitness.generalstring;

import edu.cornell.lassp.houle.RngPack.RandomElement;

/**
 * This class created symmetric SSSP instances in which each distance is choosen uniformly at random out from the set {1,...,maxDistance}.
 * @author  Heiko
 */
public class SSSPUniformInstance extends SSSPInstance {

	private int maxDistance = 10;

	public SSSPUniformInstance(int dimension, RandomElement re, int maxDistance) {
		super(dimension,re);
		this.maxDistance = maxDistance;
	}

	protected double[][] getNextDistanceMatrix() {
		double[][] result = new double[dimension][dimension];
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				result[i][j] = re.choose(1,maxDistance);
			}
		}
		return result;
	}

	protected double getLargestMatrixEntry() {
		return maxDistance;
	}
	
	/**
	 * @param maxDistance  the maxDistance to set
	 * @uml.property  name="maxDistance"
	 */
	public void setMaxDistance(int maxDistance) {
		this.maxDistance = maxDistance;
	}

}
