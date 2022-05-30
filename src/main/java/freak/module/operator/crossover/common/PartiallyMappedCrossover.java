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
 *
 * Partially Mapped (or Matched) Crossover (PMX) is a well known crossover
 * operator for cycles. It works with different semantic on cycles and
 * permutations, but it actually is designed to work on cycles.
 * 
 * @author Heiko
 */
public class PartiallyMappedCrossover extends MultiPairwiseCrossover implements CompatibleWithDifferentSearchSpaces {
	
	/**
	 * Creates a new PartiallyMappedCrossover object
	 */
	public PartiallyMappedCrossover(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof Permutation) || (searchspace instanceof Cycle)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}

	/**
	 * This method calculates the inverse of the mapping i -> a[i]-1. <br>
	 * Example: a[0] = 2, a[1] = 3, a[2] = 1 <br>
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
	 * Performs a partially mapped crossover of ind1 and ind2.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		RandomElement re = graph.getSchedule().getRandomElement();
		int[] gt1 = ((PermutationGenotype)ind1.getGenotype()).getIntArray();
		int[] gt2 = ((PermutationGenotype)ind2.getGenotype()).getIntArray();
		int[] newGt = new int[gt1.length];
		int[] gt2Inv = calculateInverse(gt2);
		// First, two crossing points are chosen at random.
		int xover1 = re.choose(0, gt1.length - 2);
		int xover2 = re.choose(xover1 + 1, gt1.length - 1);

		// The cycles are cut into three parts by these crossing points.
		// The middle part of the first individual is now taken over.

		// We mark the cities which are in this part.
		boolean[] inserted = new boolean[gt1.length];
		for (int i = 0; i < gt1.length; i++) {
			inserted[i] = false;
		}
		for (int i = xover1 + 1; i <= xover2; i++) {
			newGt[i] = gt1[i];
			inserted[newGt[i] - 1] = true;
		}
		// Now we iterate through the middle part of the second individual and
		// insert all elements in this section into their correct position in
		// the child of ind1 and ind2. The determination of this position is
		// a little complicated and can be looked up in any description of the
		// PMX operator.
		for (int i = xover1 + 1; i <= xover2; i++) {
			if (!inserted[gt2[i] - 1]) {
				// The element gt2[i] does not lie in the middle part of the
				// first individual and therefore it wasn't insert yet.
				// Now we determine its correct position in newGt.
				int pos = i;
				// As long as the current position lies in the middle part...
				while ((xover1 < pos) && (pos <= xover2)) {
					// ...we try the next position given by the PMX algorithm.
					pos = gt2Inv[gt1[pos] - 1];
				}
				newGt[pos] = gt2[i];
			}
		}
		// Now we can copy the remaining positions from ind2.
		for (int i = 0; i <= xover1; i++) {
			if (!inserted[gt2[i] - 1]) {
				newGt[i] = gt2[i];
			}
		}
		for (int i = xover2 + 1; i < gt1.length; i++) {
			if (!inserted[gt2[i] - 1]) {
				newGt[i] = gt2[i];
			}
		}
		return new Individual(graph.getSchedule(), new PermutationGenotype(newGt), new Individual[] { ind1, ind2 });
	}

	public String getDescription() {
		return "Partially Mapped (or Matched) Crossover (PMX) is a well known crossover " + "operator for cycles. It works with different semantic on cycles and permutations, " + "but it actually is designed to work on cycles.";
	}

	public String getName() {
		return "Partially Mapped Crossover";
	}
}
