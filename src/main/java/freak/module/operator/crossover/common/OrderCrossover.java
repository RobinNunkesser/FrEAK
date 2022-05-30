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
 * Order crossover is based on 2 point crossover.  At first 2 crossing points
 * are chosen at random. These crossing points divide each individual in 3
 * parts. <p>
 * For example <br>
 * 12|3456|789 and <br>
 * 84|1593|627. <br>
 * The information in the middle part of the first individual is taken over, so
 * we get the permutation <br>
 * ??|3456|??? <br>
 * The remaining values (1,2,7,8,9) are inserted (starting in the right part) in
 * the order (2,7,8,1,9) in which they appear in the second individual starting
 * in the right part. <br>
 * So, the created child is: <br>
 * 193456278.
 * 
 * @author Heiko
 */
public class OrderCrossover extends MultiPairwiseCrossover implements CompatibleWithDifferentSearchSpaces {

	/**
	 * Creates a new OrderCrossover object
	 */
	public OrderCrossover(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof Permutation) || (searchspace instanceof Cycle)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}

	/**
	 * Performs an order crossover of ind1 and ind2.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		RandomElement re = graph.getSchedule().getRandomElement();
		int[] gt1 = ((PermutationGenotype)ind1.getGenotype()).getIntArray();
		int[] gt2 = ((PermutationGenotype)ind2.getGenotype()).getIntArray();
		int[] newGt = new int[gt1.length];
		// contains the information which city has been visited yet
		boolean[] visited = new boolean[gt1.length];
		// the number of cities visited yet
		int numVis = 0;
		for (int i = 0; i < gt1.length; i++) {
			visited[i] = false;
		}
		// First, two crossing points are chosen at random.
		int xover1 = re.choose(0, gt1.length - 2);
		int xover2 = re.choose(xover1 + 1, gt1.length - 1);
		// The cycles are cut into three parts by these crossing points.
		// The middle part of the first individual is now taken over.
		for (int i = 0; i < xover2 - xover1; i++) {
			newGt[i] = gt1[xover1 + i + 1];
			visited[gt1[xover1 + i + 1] - 1] = true;
		}
		numVis = xover2 - xover1;
		// Now we complete the tour by adding the missing cities in the order in
		// which they appear in the second individual.

		// We iterate the position in the second individual. i is the iterator.
		int i = xover2;
		while (numVis < gt1.length) {
			if (i == gt1.length - 1) {
				i = 0;
			} else {
				i++;
			}
			if (!visited[gt2[i] - 1]) {
				visited[gt2[i] - 1] = true;
				newGt[numVis] = gt2[i];
				numVis++;
			}
		}
		return new Individual(graph.getSchedule(), new PermutationGenotype(newGt), new Individual[] { ind1, ind2 });
	}

	public String getDescription() {
		return "Order crossover is based on 2 point crossover.  At first, 2 crossing points " + "are chosen at random. These crossing points divide each individual in 3 parts.\n" + "For example, 12|3456|789 and 84|1593|627. The information in the middle part of the " + "first individual is taken over, so we get the permutation ??|3456|??? \n" + "The remaining values (1,2,7,8,9) are inserted (starting in the right part) in " + "the order (2,7,8,1,9) in which they appear in the second individual starting " + "in the right part. So, the created child is: 193456278.";
	}

	public String getName() {
		return "Order Crossover";
	}
}
