/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.searchspace;

import edu.cornell.lassp.houle.RngPack.*;
import freak.core.control.*;
import freak.core.modulesupport.Configurable;
import freak.core.population.*;
import freak.core.searchspace.*;

/**
 * Cycle represents the search space consisting of all permutations in the symmetric group S_n which form a cycle (e.g.&nbsp;a TSP-tour). 
 * @author  Heiko, Michael
 */
public class Cycle extends AbstractSearchSpace implements Configurable, HasMetric, HasDimension {

	private int dimension;
	
	/**
	 * Creates a new object with default dimension 40.
	 * @param schedule a reference to a schedule object.  
	 */
	public Cycle(Schedule schedule) {
		super(schedule);
		dimension = 100;
	}

	public String getName() {
		return "Cycle";
	}

	public String getDescription() {
		return "Represents all permutations in the symmetric group S_{dimension} which form a cycle (e.g. a TSP-tour).\n" + "That are all permutations of the elements {1,...,dimension} which form a cycle.";
	}

	/**
	 * Calculates n!
	 * @return n!
	 */
	private double factorial(int n) {
		if (n == 1)
			return 1;
		return n * factorial(n - 1);
	}

	/**
	 * This method returns (dimension-1)!. If the dimension is at least 172,
	 * this value becomes that large that it cannot be saved in a <code>double
	 * </code> correctly. Infinity is returned in those cases.  
	 * 
	 * @return (dimension-1)! 
	 */
	public double getSize() {
		if (dimension > 171) {
			return Double.POSITIVE_INFINITY;
		}
		return factorial(dimension - 1);
	}

	public Genotype getRandomGenotype() {
		int[] gt = new int[dimension];
		RandomElement re = schedule.getRandomElement();
		for (int i = 0; i < dimension; i++) {
			gt[i] = i + 1;
		}
		for (int i = 0; i < dimension - 1; i++) {
			int j = re.choose(i, dimension - 1);
			int tmp = gt[i];
			gt[i] = gt[j];
			gt[j] = tmp;
		}
		return new PermutationGenotype(gt);
	}

	/** The distance between the given cycles gt1 and gt2 is defined as the
	 * dimension minus the number of shared edges in the tours.
	 * @return the distance between gt1 and gt2
	 */
	public double getDistance(Genotype gt1, Genotype gt2) {
		int[] cgt1 = ((PermutationGenotype)gt1).getIntArray();
		int[] cgt2 = ((PermutationGenotype)gt2).getIntArray();
		int[][] c = new int[dimension][2];
		int distance = dimension;
		// for each connection of ctg1
		for (int i = 0; i < dimension; i++) {
			//save connection in both directions
			c[cgt1[i] - 1][0] = cgt1[(i + 1) % dimension];
			c[cgt1[(i + 1) % dimension] - 1][1] = cgt1[i];
		}
		//for each connection of cgt2
		for (int i = 0; i < dimension; i++) {
			if (c[cgt2[i] - 1][0] == cgt2[(i + 1) % dimension] || c[cgt2[i] - 1][0] == cgt2[(i - 1 + dimension) % dimension])
				distance--;
		}
		return distance;
	}

	public String getLongDescriptionForDimension() {
		return "Elements in this search space are permutations in the symmetric group S_{dimension} which form a cycle.";
	}
	
	/**
	 * Sets the dimension of this search space.
	 * @param dim the value to which dimension is set.
	 */
	public void setPropertyDimension(Integer dim) {
		if (dim.intValue() > 1)
			dimension = dim.intValue();
	}

	/**
	 * @return the dimension of the search space.
	 */
	public Integer getPropertyDimension() {
		return new Integer(dimension);
	}
	
	/**
	 * Returns the dimension of the seach space.
	 * @return  the dimension of the search space.
	 * @uml.property  name="dimension"
	 */
	public int getDimension() {
		return dimension;
	}
}
