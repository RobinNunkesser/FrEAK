/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.bitstring;

import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.core.util.FreakMath;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;

/**
 * @author Michael
 */
public class KBitMover extends Mutation implements Configurable {
	
	protected int k;
	
	public KBitMover(OperatorGraph graph) {
		super(graph);
		k = 1;
	}
	
	public void initialize() {
		super.initialize();
		
		int dim = ((HasDimension)getGenotypeSearchSpace()).getDimension();
		if (k > dim) k = dim; 
	}
	
	public void setPropertyK(Integer num) {
		if ((num.intValue() >= 0) && (num.intValue() <= ((BitString)getGenotypeSearchSpace()).getDimension()/2))
			k = num.intValue();
	}
	
	public Integer getPropertyK() {
		return new Integer(k);
	}
	
	public String getLongDescriptionForkK() {
		return "Number of positions of one-bits to move.";
	}
	
	public String getShortDescriptionForK() {
		return "k";
	}
	
	protected Individual doMutation(Individual ind) {
		int dimension = ((BitString)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		BitStringGenotype bs = (BitStringGenotype) ((BitStringGenotype)ind.getGenotype()).clone();
		int kLocal = k;
		int numberOfOnes = bs.getBitSet().cardinality();
		if (numberOfOnes < kLocal) {
			// can't move more ones than exists
			kLocal = numberOfOnes;
		}
		if (dimension-numberOfOnes < kLocal) {
			// not enough space for the Ones
			kLocal = dimension-numberOfOnes;
		}

		int onesCount = 0; int zeroesCount = 0;
		int[] ones = new int[numberOfOnes];
		int[] zeroes = new int[dimension-numberOfOnes];
		for (int i = 0; i < dimension; i++) {
			if (bs.get(i)) {
				// ones
				ones[onesCount++] = i;
			} else {
				// zeroes
				zeroes[zeroesCount++] = i;				
			}
		}
		
		int[] flipOnes = FreakMath.getKofN(graph.getSchedule(), kLocal, numberOfOnes);
		int[] flipzeroes = FreakMath.getKofN(graph.getSchedule(), kLocal, dimension-numberOfOnes);

		for (int i = 0; i < kLocal; i++) {
			bs.flip(ones[flipOnes[i]]);
			bs.flip(zeroes[flipzeroes[i]]);
		}
		
		return new Individual(graph.getSchedule(), bs, new Individual[] { ind });
	}
	
	public String getDescription() {
		return "Exactly k randomly chosen one-bits and k randomly chosen zero-bits are flipped.";
	}
	
	public String getName() {
		return "k-Bit Mover";
	}
}
