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
 * Permutation represents the search space consisting of all permutations in the symmetric group S_n. 
 * @author  Heiko, Michael
 */
public class Permutation extends AbstractSearchSpace implements Configurable, HasDimension {

	private int dimension;
	/**
	 * Creates a new object with default dimension 40.
	 * @param schedule a reference to a schedule object. 
	 */
	public Permutation(Schedule schedule) {
		super(schedule);
		dimension = 40;
	}

	public String getName() {
		return "Permutation";
	}

	public String getDescription() {
		return "Represents all permutations in the symmetric group S_{dimension}. \n" + "That are all permutations of the elements {1,...,dimension}.";
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
	 * This method returns dimension!. If the dimension is at least 171,
	 * this value becomes that large that it cannot be saved in a <code>double
	 * </code> correctly. Infinity is returned in those cases.
	 * 
	 * @return dimension!   
	 */
	public double getSize() {
		if (dimension > 170) {
			return Double.POSITIVE_INFINITY;
		}
		return factorial(dimension);
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

	public String getLongDescriptionForDimension() {
		return "Elements in this search space are permutations in the symmetric group S_{dimension}.";
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