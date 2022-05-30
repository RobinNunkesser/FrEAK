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
 * INV(pi) measures the number of pairs (i,j), such that pi(i) < pi(j) (pairs in
 * correct order)
 * 
 * @author Heiko
 */
public class Sort_INV extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * Creates a new object.
	 * 
	 * @param schedule a reference of a schedule.
	 */
	public Sort_INV(Schedule schedule) {
		super(schedule);
	}

	protected double evaluate(Genotype genotype) {
		return calculateINV((PermutationGenotype)genotype);
	}

	public String getName() {
		return "Sorting with measure INV";
	}

	public String getDescription() {
		return "The sorting problem can be viewed as an optimization problem: the sortedness " + "of a sequence has to be maximized. We assume a_1 < a_2 < ... < a_n, so the " + "optimal permutation is the identity. \n" + "The measure of sortedness used by this fitness function is INV.\n" + "INV(pi) measures the number of pairs (i,j) such that pi(i) < pi(j) (pairs in correct order).";
	}

	/**
	 * This method returns the number of pairs in correct order. 
	 * @param gt the genotype which specifies the permutation
	 * @return the number of pairs in correct order
	 */
	private int calculateINV(PermutationGenotype gt) {
		int[] a = gt.getIntArray();
		int count = 0;
		for (int i = 0; i < a.length - 1; i++) {
			for (int j = i + 1; j < a.length; j++) {
				if (a[i] < a[j]) {
					count++;
				}
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
		int n = ((Permutation)getSchedule().getPhenotypeSearchSpace()).getDimension();
		return n * (n - 1) / 2;
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

}