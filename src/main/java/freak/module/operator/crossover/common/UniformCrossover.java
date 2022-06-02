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
import freak.core.searchspace.HasDimension;
import freak.core.searchspace.SearchSpace;
import freak.module.operator.crossover.MultiPairwiseCrossover;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

import java.util.BitSet;

/**
 * Each character is taken with probability 1/2 from the first parent and with
 * probability 1/2 from the second.
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael
 */
public class UniformCrossover extends MultiPairwiseCrossover implements CompatibleWithDifferentSearchSpaces {
	
	public UniformCrossover(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof GeneralString) || (searchspace instanceof BitString)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}
	
	/**
	 * Performs a uniform crossover of ind1 and ind2.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		SearchSpace searchspace = graph.getSchedule().getGenotypeSearchSpace();
		
		int dimension = ((HasDimension)searchspace).getDimension();
		RandomElement re = graph.getSchedule().getRandomElement();
		
		if (searchspace instanceof BitString) {
			
			BitSet bs1 = ((BitStringGenotype)ind1.getGenotype()).getBitSet();
			BitSet bs2 = ((BitStringGenotype)ind2.getGenotype()).getBitSet();
			BitSet bsOut = (BitSet)bs1.clone();
			// for each bit
			for (int i = 0; i < dimension; i++) {
				// if 0 and 1 is possible, then randomly choose the bit
				if ((bs1.get(i) != bs2.get(i)) && re.choose(2) == 1)
					bsOut.set(i, bs2.get(i));
			}
			return new Individual(graph.getSchedule(), new BitStringGenotype(bsOut, dimension), new Individual[] { ind1, ind2 });
			
		} else {
			
			int[] gs1 = ((GeneralStringGenotype)ind1.getGenotype()).getIntArray();
			int[] gs2 = ((GeneralStringGenotype)ind2.getGenotype()).getIntArray();
			int[] gsOut = (int[])gs1.clone();
			int numChars = ((GeneralString)searchspace).getNumChars();
			// for each bit
			for (int i = 0; i < dimension; i++) {
				if ((gs1[i] != gs2[i]) && re.choose(2) == 1)
					gsOut[i] = gs2[i];
			}
			return new Individual(graph.getSchedule(), new GeneralStringGenotype(gsOut, numChars), new Individual[] { ind1, ind2 });
		}
	}
	
	public String getDescription() {
		if (graph.getSchedule().getGenotypeSearchSpace() instanceof BitString) {
			return "Each bit is taken with probability 1/2 from the first parent and with probability 1/2 from the second parent.";
		} else {
			return "Each char is taken with probability 1/2 from the first parent and with probability 1/2 from the second parent.";
		}
	}
	
	public String getName() {
		return "Uniform Crossover";
	}
	
}
