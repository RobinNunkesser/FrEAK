/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover.common;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.searchspace.SearchSpace;
import freak.module.operator.crossover.MultiPairwiseCrossover;
import freak.module.searchspace.Cycle;
import freak.module.searchspace.Permutation;
import freak.module.searchspace.PermutationGenotype;

/**
 * This operator works (with different semantic) on permutations and on cycles.
 * <p>
 * First k is chosen uniformly at random from {1,...,n-1} (for permutations /
 * cycles out of S_n). Then k randomly chosen positions are marked. We take
 * over these k positions from the second individual in the order in which they
 * are in the first individual.
 * <p>
 * Example: <br>
 * ind1 = 123456789 <br>
 * ind2 = 841593627 <br>
 * k=4, marked are the positions 2,4,6,8 <br>
 *
 * We take from the second individual 4,5,3,2. <br>
 * The order of these elements in the first individual is 2,3,4,5. That's why
 * the child is of the form <br>
 * ?2?3?4?5? <br>
 * Now we add the remaining positions from ind2: <br>
 * 821394657.
 * 
 * @author Heiko
 */
public class OrderCrossover2 extends MultiPairwiseCrossover implements CompatibleWithDifferentSearchSpaces {

	/**
	 * Creates a new OrderCrossover2 object
	 */
	public OrderCrossover2(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof Permutation) || (searchspace instanceof Cycle)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}

	/**
	 * This method calculates the inverse of the mapping i -> a[i]-1. <b>
	 * Example: a[0] = 2, a[1] = 3, a[2] = 1 <b>
	 * Output: b[0] = 2, b[1] = 0, b[2] = 1
	 * @param perm a permutation
	 * @return the inverse of perm
	 */
	private int[] calculateInverse(int[] perm) {
		int[] inv = new int[perm.length];
		for (int i = 0; i < perm.length; i++) {
			inv[perm[i] - 1] = i;
		}
		return inv;
	}

	/**
	 * Performs an order crossover 2 of ind1 and ind2.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		RandomElement re = graph.getSchedule().getRandomElement();
		int[] gt1 = ((PermutationGenotype)ind1.getGenotype()).getIntArray();
		int[] gt2 = ((PermutationGenotype)ind2.getGenotype()).getIntArray();
		int[] newGt = new int[gt1.length];

		// mark each position with probability 1/2
		boolean[] marked = new boolean[gt1.length];
		for (int i = 0; i < gt1.length; i++) {
			if (re.choose(1, 2) == 1) {
				marked[i] = false;
			} else {
				marked[i] = true;
			}
		}
		// calculate the inverse of gt2
		int[] inv2 = calculateInverse(gt2);
		// pointer to the first marked position which isn't filled in newGt yet
		int nextMarkedPos = 0;
		while (!marked[nextMarkedPos]) {
			nextMarkedPos++;
		}
		// now we fill the marked positions of gt2 into newGt in the order in
		// which they appear in gt1
		for (int i = 0; i < gt1.length; i++) {
			// first check wheather the element gt1[i] is marked in gt2
			boolean isMarked = marked[inv2[gt1[i] - 1]];
			if (isMarked) {
				newGt[nextMarkedPos] = gt1[i];
				if (nextMarkedPos < gt1.length - 1) {
					nextMarkedPos++;
				}
				while ((!marked[nextMarkedPos]) && (nextMarkedPos < gt1.length - 1)) {
					nextMarkedPos++;
				}
			}
		}
		// now we copy the positions from gt2 which are not marked into newGt
		for (int i = 0; i < gt1.length; i++) {
			if (!marked[i]) {
				newGt[i] = gt2[i];
			}
		}
		return new Individual(graph.getSchedule(), new PermutationGenotype(newGt), new Individual[] { ind1, ind2 });
	}

	public String getDescription() {
		return "First k is chosen uniformly at random from {1,...,n-1} (n denotes the dimension of the search space). " + "Then k randomly chosen positions are marked. These k positions are taken over from the second individual in the order in which they " + "are in the first individual.\n" + "Example: ind1 = 123456789, ind2 = 841593627, k=4, marked are the positions 2,4,6,8\n" + "We take from the second individual 4,5,3,2. The order of these elements in the first individual is 2,3,4,5. That's why " + "the child is of the form ?2?3?4?5?. Now we add the remaining positions from ind2: 821394657.";
	}

	public String getName() {
		return "Order crossover-2";
	}
}
