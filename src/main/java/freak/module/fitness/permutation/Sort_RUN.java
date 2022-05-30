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
 * RUN(pi) is by 1 larger than the number of indices i such that pi(i+1) < pi(i)
 * (number of maximal sorted blocks called runs) leading to a minimization
 * problem
 * 
 * @author Heiko
 */
public class Sort_RUN extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * Creates a new object.
	 * 
	 * @param schedule a reference of a schedule.
	 */
	public Sort_RUN(Schedule schedule) {
		super(schedule);
	}

	protected double evaluate(Genotype genotype) {
		return calculateRUN((PermutationGenotype)genotype);
	}

	public String getName() {
		return "Sorting with measure RUN";
	}

	public String getDescription() {
		return "The sorting problem can be viewed as an optimization problem: the sortedness " + "of a sequence has to be maximized. We assume a_1 < a_2 < ... < a_n, so the " + "optimal permutation is the identity. \n" + "The measure of sortedness used by this fitness function is RUN.\n" + "RUN(pi) is by 1 larger than the number of indices i such that pi(i+1) < pi(i) " + "(number of maximal sorted blocks called runs) leading to a minimization problem.\n" + "In order to get a maximization problem, the fitness actually returned by this function is -RUN(pi).";
	}

	/**
	 * This method returns the number runs 
	 * @param gt the genotype which specifies the permutation
	 * @return the number of runs
	 */
	private int calculateRUN(PermutationGenotype gt) {
		int[] a = gt.getIntArray();
		int count = 0;
		for (int i = 0; i < a.length - 1; i++) {
			if (a[i] > a[i + 1]) {
				count++;
			}
		}
		return - (count + 1);
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
		return -1;
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return -((Permutation)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return -1;
	}

}
