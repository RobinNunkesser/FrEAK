/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.common;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.graph.OperatorGraph;
import freak.core.population.Individual;
import freak.module.searchspace.PermutationGenotype;

/**
 * This operator works on permutations and cycles and performs a number of 
 * exchange operations which is poisson distributed with the specified parameter
 * <code>lambda</code>.
 * With every exchange operation, two elements at randomly chosen positions are
 * exchanged.
 *
 * @author Michael, Heiko
 */
public class Exchange extends PoissonOperation {
	/**
	 * Creates a new Exchange object.
	 */
	public Exchange(OperatorGraph graph) {
		super(graph);
	}

	/**
	 * Chooses two positions at random and performs an exchange.
	 */
	protected Individual performSingleMutation(Individual ind) {
		RandomElement re = graph.getSchedule().getRandomElement();
		int[] perm = (int[]) ((PermutationGenotype)ind.getGenotype()).getIntArray().clone();
		// get both positions
		int i = re.choose(0, perm.length - 1);
		int j = re.choose(0, perm.length - 2);
		if (i <= j)
			j++;
		// exchange the numbers at position i and j
		int temp = perm[i];
		perm[i] = perm[j];
		perm[j] = temp;
		return new Individual(graph.getSchedule(), new PermutationGenotype(perm), new Individual[] { ind });
	}

	public String getDescription() {
		return "Exchanges two randomly chosen elements.";
	}

	public String getName() {
		return "Exchange";
	}

}
