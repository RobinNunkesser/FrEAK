/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.bitstring;
import java.util.*;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.module.searchspace.*;

/**
 * This class implements the fitness function H-IFF. <br>
 *
 * It searches in a Bit string of length 2^k all blocks of size 2^i, 0<=i<=k. If
 * all bits within such a block 2^i are equal (set or unset) 2^i is added to the
 * fitness value. But the blocks has also to start at positions 2^j, for a j in 
 * the natural numbers.<br>
 * 
 * You can think of this fitness function as a complete binary tree. Each node
 * is a block as described above. The leaves are the bits itself (block of size
 * 2^0=1) and the root is the complete bit string (block of size 2^k=dimension).<br>
 * 
 * @author Christian, Heiko, Michael
 */

public class HIFF extends AbstractStaticSingleObjectiveFitnessFunction {
	
	/**
	 * The constructor of the class.
	 *
	 * @param schedule a back-link to the currently used schedule
	 */
	public HIFF(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		int n = ((BitString)schedule.getPhenotypeSearchSpace()).getDimension();
		if (Math.round(Math.log(n) / Math.log(2)) != Math.log(n) / Math.log(2)) {
			throw new UnsupportedEnvironmentException("Size of search space doesn´t match with fitness function (has to be 2^k)");
		}
	}

	public double evaluate(Genotype genotype) {
		BitSet set = ((BitStringGenotype)genotype).getBitSet();

		// now search for the blocks and check them
		int n = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();

		//if (Math.round(Math.log(n)/Math.log(2)) != Math.log(n)/Math.log(2))
		//	throw new RuntimeException("Size of search space does not match with fitness function (has to be 2^k)");
		double fitness = n;

		int range = n;
		int[] temp = new int[n];

		for (int k = 0; k < n; k++) {
			temp[k] = (set.get(k) == true ? 1 : 0);
		}

		while (range > 1) {
			// compare to neighbored blocks
			for (int k = 0; k < (range / 2); k++) {
				if (temp[2 * k] == temp[2 * k + 1] && temp[2 * k] != -1) {
					temp[k] = temp[2 * k];
					fitness += 2 * n / range;
				} else {
					temp[k] = -1;
				}
			}

			range /= 2;
		}

		return fitness;
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		int n = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
		return n * (Math.log(n) / Math.log(2) + 1);
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		// the optimum lies in 1^n ...
		BitSet bs = new BitSet(((BitString)getSchedule().getGenotypeSearchSpace()).getDimension());
		bs.set(0, ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension());
		return new BitStringGenotype(bs, ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension());
	}

	public String getName() {
		return "H-IFF";
	}

	public String getDescription() {
		return "There is a bit string of length n=2^k. All possible blocks with " + "length powers of 2 are constructed and checked if their bits are " + "all equal. If they are equal the length of the block is added to " + "the fitness value. But these blocks has also to start at positions 2^j, " + "for a j in the natural numbers.\r\n" + "You can think of this fitness function as a complete binary tree. Each node " + "is a block as described above. The leaves are the bits itself (block of size " + "2^0=1) and the root is the complete bit string (block of size 2^k=dimension).";
	}
}
