/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.cycle;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.core.util.FreakMath;
import freak.module.searchspace.Cycle;
import freak.module.searchspace.PermutationGenotype;

/**
 * The k-opt operator cuts the permutation into k segments and then reorders
 * the segments. Each segment can be placed in a forward or reverse order.
 *
 * @author Michael
 */
public class KOpt extends Mutation implements Configurable {

	private int k;

	public KOpt(OperatorGraph graph) {
		super(graph);
		k = 2;
	}

	public void initialize() {
		super.initialize();
		
		int dim = ((HasDimension)getGenotypeSearchSpace()).getDimension();
		if (k > dim) k = dim; 
	}
	
	public Integer getPropertyK() {
		return new Integer(k);
	}

	public void setPropertyK(Integer k) {
		int dim = ((Cycle)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		int tmp = k.intValue();
		if (tmp > 2 && tmp <= dim) {
			this.k = tmp;
		}
	}

	public String getLongDescriptionForK() {
		return "Number of segments for reordering in the cycle.";
	}

	public String getShortDescriptionForK() {
		return "k";
	}

	/**
	 * do k-opt
	 */
	protected Individual doMutation(Individual ind) {
		RandomElement re = graph.getSchedule().getRandomElement();
		int[] tour = ((PermutationGenotype)ind.getGenotype()).getIntArray();
		int[] mutTour = new int[tour.length];
		// get k position to cut
		int[] pos = FreakMath.getKofN(graph.getSchedule(), k, tour.length);
		// get new order of segments
		int[] perm = FreakMath.getKofN(graph.getSchedule(), k, k);
		boolean[] reverse = new boolean[k];
		// each segment can be placed in a forward or reverse order
		for (int i = 0; i < k; i++) {
			reverse[i] = re.choose(2) == 1;
		}
		//first new connection must be realy new, so preventing old connection
		int big = (perm[0] > perm[1]) ? perm[0] : perm[1];
		int small = (perm[0] < perm[1]) ? perm[0] : perm[1];
		// if the segments are old neighbours and the old connection will conserve
		if ((big - small == 1 || big - small == perm.length - 1) && reverse[0] == reverse[1]) {
			//only one of both must be reversed
			reverse[0] = !reverse[0];
		}
		int intervalSize;
		int mutI = 0;
		// for each segment
		for (int i = 0; i < k; i++) {
			// estimate the segment length
			intervalSize = (pos[(perm[i] + 1) % k] - pos[perm[i]] + tour.length) % tour.length;
			// write the segement in the output permutation
			for (int j = 0; j < intervalSize; j++) {
				if (reverse[i]) {
					mutTour[mutI++] = tour[(pos[(perm[i] + 1) % k] - j - 1 + tour.length) % tour.length];
				} else {
					mutTour[mutI++] = tour[(pos[perm[i]] + j) % tour.length];
				}
			}
		}
		return new Individual(graph.getSchedule(), new PermutationGenotype(mutTour), new Individual[] { ind });
	}

	public String getDescription() {
		return "The k-Opt operator cuts the permutation into k segments and then reorders the segments. Each segment can be placed in a forward or reverse order.";
	}

	public String getName() {
		return "k-Opt";
	}
}
