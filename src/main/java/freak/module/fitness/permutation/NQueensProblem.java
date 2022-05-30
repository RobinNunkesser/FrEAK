/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.permutation;

import freak.core.control.Schedule;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.population.Genotype;
import freak.module.searchspace.Permutation;
import freak.module.searchspace.PermutationGenotype;

/**
 * Implements the N Queens Problem.
 * 
 * @author Dirk
 */
public class NQueensProblem extends AbstractStaticSingleObjectiveFitnessFunction {

	private int n;

	public NQueensProblem(Schedule schedule) {
		super(schedule);
	}
	
	public void initialize() {
		super.initialize();

		n = ((Permutation)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	protected double evaluate(Genotype genotype) {
		int hits = 0;
		int[] perm = ((PermutationGenotype)genotype).getIntArray();

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (perm[i] - perm[j] == j - i || perm[j] - perm[i] == j - i) {
					hits++;
				}
			}
		}
		return -hits;
	}

	public String getName() {
		return "N Queens Problem";
	}

	public String getDescription() {
		return "The N Queens Problem asks how N queens can be places on a NxN chess board so that none of them can hit any other in one move. " + "Every queen is placed in a separate row, the column is determined by the permutation. " + "The fitness is the number of pairs of queens that can hit each other, multiplied by -1 to gain a maximization problem.";
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return n * (n - 1) / 2;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return 0;
	}

}
