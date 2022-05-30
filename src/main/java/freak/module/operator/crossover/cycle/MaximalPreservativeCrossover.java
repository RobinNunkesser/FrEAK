/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover.cycle;

import edu.cornell.lassp.houle.RngPack.*;
import freak.core.graph.*;
import freak.core.population.*;
import freak.module.operator.crossover.*;
import freak.module.searchspace.*;

/**
 * Two crossing points are chosen at random. The subtour in the middle part of
 * the second individual is taken over. Now we try to use edges in the parents
 * to complete the tour. Edges in the first individual have a greater priority
 * than those in the second individual. If both parent don't have permitted
 * edges starting at the last city visited, then the first following city in the
 * first individual which hasn't been visited becomes the next city on the tour.
 * <p>
 * Example:
 * ind1 = 12|3456|789
 * ind2 = 84|1593|627
 * <br>
 * The subtour 1593 is taken from ind2. <br>
 * 3->4 is taken from ind1, so we have the subtour 15934 <br>
 * Both 4->5 from ind1 and 4->1 are not allowed, so we continue with 6. <br>
 * Now we have the subtour: 159346. <br>
 * We can add 6->7->8 from ind1. <br>
 * Now just city number 2 isn't visited yet. <br>
 * We get: 159346782 
 * 
 * @author Heiko
 */
public class MaximalPreservativeCrossover extends MultiPairwiseCrossover {
	
	/**
	 * Creates a new MaximalPreservativeCrossover object.
	 * @param a backlink to the OperatorGraph 
	 */
	public MaximalPreservativeCrossover(OperatorGraph graph) {
		super(graph);
	}

	/**
	 * This method transforms a cycle which is given in cyclic notation into
	 * a function table representation.
	 * @param cycle a permutation given in cyclic notation
	 * @return the given permutation given in a function table representation
	 */
	private int[] transformToFunctionTable(int[] cycle) {
		// This array will contain the function table representation.
		int[] ft = new int[cycle.length];
		for (int i = 0; i < cycle.length - 1; i++) {
			// The array starts with position 0 but cycle is a cycle on the
			// elements 1,...,n. That's the reason for "-1". ft[i] = j means
			// that i+1 is mapped to j+1 by the given permutation.			
			ft[cycle[i] - 1] = cycle[i + 1];
		}
		ft[cycle[cycle.length - 1] - 1] = cycle[0];
		return ft;
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
	 * This method defines how a maximal preservative crossover is performed.
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
		// The middle part of the second individual is now taken over.
		for (int i = 0; i < xover2 - xover1; i++) {
			newGt[i] = gt2[xover1 + i + 1];
			visited[gt2[xover1 + i + 1] - 1] = true;
		}
		numVis = xover2 - xover1;
		// Now we calculate a representation of gt1 and gt2 as function tables.
		int[] ft1 = transformToFunctionTable(gt1);
		int[] ft2 = transformToFunctionTable(gt2);
		// We also need the inverse of gt1.
		int[] inv1 = calculateInverse(gt1);
		// Now we try to complete the tour by following edges from ind1 or ind2.
		while (numVis < gt1.length) {
			int nextCityOnTour1 = ft1[newGt[numVis - 1] - 1];
			// Can we use the edge from ind1?
			if (!(visited[nextCityOnTour1 - 1])) {
				visited[nextCityOnTour1 - 1] = true;
				newGt[numVis] = nextCityOnTour1;
				numVis++;
			} else {
				int nextCityOnTour2 = ft2[newGt[numVis - 1] - 1];
				// Can we use the edge from ind2?
				if (!(visited[nextCityOnTour2 - 1])) {
					visited[nextCityOnTour2 - 1] = true;
					newGt[numVis] = nextCityOnTour2;
					numVis++;
				} else {
					// We cannot use one of the edges from ind1 or ind2.
					// So we look for the first city in ind1 which hasn't been
					// visited yet.
					int i = inv1[newGt[numVis - 1] - 1];
					while (visited[gt1[i] - 1]) {
						if (i < gt1.length - 1) {
							i++;
						} else {
							i = 0;
						}
					}
					visited[gt1[i] - 1] = true;
					newGt[numVis] = gt1[i];
					numVis++;
				}
			}
		}
		return new Individual(graph.getSchedule(), new PermutationGenotype(newGt), new Individual[] { ind1, ind2 });
	}

	public String getDescription() {
		return "Two crossing points are chosen at random. The subtour in the middle part of " + "the second individual is taken over. Now we try to use edges in the parents " + "to complete the tour. Edges in the first individual have a greater priority " + "than those in the second individual. If both parent don't have permitted " + "edges starting at the last city visited, then the first following city in the " + "first individual which hasn't been visited becomes the next city on the tour.\n" + "Example: ind1 = 12|3456|789, ind2 = 84|1593|627\n" + "The subtour 1593 is taken from ind2. 3->4 is taken from ind1, so we have the subtour 15934. " + "Both 4->5 from ind1 and 4->1 are not allowed, so we continue with 6. Now we have the subtour: 159346. " + "We can add 6->7->8 from ind1. Now just city number 2 isn't visited yet. We get: 159346782.";
	}

	public String getName() {
		return "Maximal Preservative Crossover";
	}
	
}
