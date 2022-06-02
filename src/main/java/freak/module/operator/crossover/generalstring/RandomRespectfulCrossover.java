/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover.generalstring;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.graph.*;
import freak.core.population.*;
import freak.module.operator.crossover.MultiPairwiseCrossover;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * The positions which are identical in both parents are taken over.
 * The characters at the other positions are chosen at random.
 *
 * @author Michael
 */
public class RandomRespectfulCrossover extends MultiPairwiseCrossover {
	
	/**
	 * Creates a new RandomRespectfulCrosssover object.
	 */
	public RandomRespectfulCrossover(OperatorGraph graph) {
		super(graph);
	}

	/**
	 * Performs a random respectful crossover.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		RandomElement re = graph.getSchedule().getRandomElement();
		int[] gs1 = ((GeneralStringGenotype)ind1.getGenotype()).getIntArray();
		int[] gs2 = ((GeneralStringGenotype)ind2.getGenotype()).getIntArray();
		int[] gsOut = (int[])gs1.clone();
		int n = ((GeneralString)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		int numChars = ((GeneralString)graph.getSchedule().getGenotypeSearchSpace()).getNumChars();
		// for each bit
		for (int i = 0; i < n; i++) {
			if (gs1[i] != gs2[i])
				gsOut[i] = re.choose(0, numChars - 1);
		}
		return new Individual(graph.getSchedule(), new GeneralStringGenotype(gsOut, numChars), new Individual[] { ind1, ind2 });
	}

	public String getDescription() {
		return "The positions which are identical in both parents are taken over. The characters at the other positions are chosen at random.";
	}

	public String getName() {
		return "Random Respectful Crossover";
	}

}
