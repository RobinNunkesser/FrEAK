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
 * EXC(pi) equals the minimal number of exchanges to sort the sequence.
 * 
 * @author Heiko
 */
public class Sort_EXC extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * Creates a new object.
	 * 
	 * @param schedule a reference of a schedule.
	 */
	public Sort_EXC(Schedule schedule) {
		super(schedule);
	}

	protected double evaluate(Genotype genotype) {
		return calculateEXC((PermutationGenotype)genotype);
	}

	public String getName() {
		return "Sorting with measure EXC";
	}

	public String getDescription() {
		return "The sorting problem can be viewed as an optimization problem: the sortedness " + "of a sequence has to be maximized. We assume a_1 < a_2 < ... < a_n, so the " + "optimal permutation is the identity. \n" + "The measure of sortedness used by this fitness function is EXC.\n" + "EXC(pi) denotes the minimal number of exchanges (of pairs pi(i) and pi(j)) necessary to sort the sequence.\n" + "This leads to a minimization problem. In order to get a maximization problem, the fitness actually returned by this function is -EXC(pi).";
	}

	/**
	 * This method returns the minimal number of exchanges to sort the sequence. 
	 * @param gt the genotype which specifies the permutation.
	 * @return the minimal number of exchanges to sort the sequence.
	 */
	private int calculateEXC(PermutationGenotype gt) {
		// The minimal number of exchanges to sort the sequence equals the
		// n minus the number of cycles in the permutation.   
		int[] a = gt.getIntArray();
		int cycles = 0;
		boolean[] visited = new boolean[a.length];
		for (int i = 0; i < a.length; i++) {
			visited[i] = false;
		}
		int i = 0;
		while (i < a.length) {
			if (visited[i]) {
				i++;
			} else {
				cycles++;
				// iterate through the cycle
				int j = i;
				while (!visited[j]) {
					visited[j] = true;
					j = a[j] - 1;
				}
			}
		}
		return - (a.length - cycles);
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
		return 0;
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return - (((Permutation)getSchedule().getPhenotypeSearchSpace()).getDimension() - 1);
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return 0;
	}

}
