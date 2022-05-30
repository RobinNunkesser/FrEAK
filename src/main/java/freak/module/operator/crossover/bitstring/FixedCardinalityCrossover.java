/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.operator.crossover.bitstring;

import java.util.BitSet;

import freak.core.graph.OperatorGraph;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.core.searchspace.SearchSpace;
import freak.core.util.FreakMath;
import freak.module.operator.crossover.MultiPairwiseCrossover;
import freak.module.searchspace.BitStringGenotype;

/**
 * This crossover preserves the number of set bits by choosing 
 * set bits from the individuals uniformly at random.
 *
 * This Operator works on bitstring.
 *
 * @author Robin
 */
public class FixedCardinalityCrossover extends MultiPairwiseCrossover {
	
	public FixedCardinalityCrossover(OperatorGraph graph) {
		super(graph);
	}
		
	/**
	 * Performs a uniform crossover of ind1 and ind2.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		SearchSpace searchspace = graph.getSchedule().getGenotypeSearchSpace();
		
		int dimension = ((HasDimension)searchspace).getDimension();
			
		BitSet bs1 = ((BitStringGenotype)ind1.getGenotype()).getBitSet();
		BitSet bs2 = ((BitStringGenotype)ind2.getGenotype()).getBitSet();
		BitSet bsOut = (BitSet)bs1.clone();
		int cardinality=bs1.cardinality();
		bsOut.or(bs2);
		int orCardinality=bsOut.cardinality();
		if (cardinality<orCardinality) {
			int[] indices=new int[orCardinality];
			int j=0;
			for (int i=bsOut.nextSetBit(0);i>=0;i=bsOut.nextSetBit(i+1)) {
				indices[j++]=i;
			}
			int[] chosenIndices=FreakMath.getKofN(graph.getSchedule(), cardinality, orCardinality);
			bsOut.clear();
			for (int i=0;i<chosenIndices.length;i++) {
				bsOut.set(indices[chosenIndices[i]]);
			}
		}	
		return new Individual(graph.getSchedule(), new BitStringGenotype(bsOut, dimension), new Individual[] { ind1, ind2 });	
	}
	
	public String getDescription() {
		return "This crossover preserves the number of set bits by choosing set bits from the individuals uniformly at random.";
	}
	
	public String getName() {
		return "Fixed Cardinality Crossover";
	}
	
}
