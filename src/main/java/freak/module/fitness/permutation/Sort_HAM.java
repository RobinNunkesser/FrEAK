/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.permutation;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.population.*;
import freak.module.searchspace.*;

/**
 * The sorting problem can be viewed as an optimization problem: the sortedness
 * of a sequence has to be maximized. We assume a_1 < a_2 < ... < a_n, so the
 * optimal permutation is the identity. <br>
 * HAM(pi) measures the number of indices i such that pi(i) = i (elements at the
 * correct position)
 * 
 * @author Heiko
 */
public class Sort_HAM extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * Creates a new object.
	 * 
	 * @param schedule a reference of a schedule.
	 */
	public Sort_HAM(Schedule schedule) {
		super(schedule);
	}

	protected double evaluate(Genotype genotype) {
		return calculateHAM((PermutationGenotype)genotype);
	}

	public String getName() {
		return "Sorting with measure HAM";
	}

	public String getDescription() {
		return "The sorting problem can be viewed as an optimization problem: the sortedness " + "of a sequence has to be maximized. We assume a_1 < a_2 < ... < a_n, so the " + "optimal permutation is the identity. \n" + "The measure of sortedness used by this fitness function is HAM.\n" + "HAM(pi) measures the number of indices i such that pi(i)=i (elements at the correct position).";
	}

	/**
	 * This method returns the number of elements at the correct position. 
	 * @param gt the genotype which specifies the permutation
	 * @return the number of elements at the correct position
	 */
	private int calculateHAM(PermutationGenotype gt) {
		int[] a = gt.getIntArray();
		int count = 0;
		for (int i = 0; i < a.length; i++) {
			// Permutation 1,...n
			if (a[i] == i + 1) {
				count++;
			}
		}
		return count;
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		int n = ((Permutation)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int[] a = new int[n];
		for (int i = 0; i < n; i++) {
			a[i] = i;
		}
		return new PermutationGenotype(a);
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return ((Permutation)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

}