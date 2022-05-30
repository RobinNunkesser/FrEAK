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
import freak.core.population.*;
import freak.module.searchspace.*;

/**
 * This class implements the LeadingOnes fitness function. <br>
 * 
 * The fitness value of a given individual is the position of the first null
 * minus 1
 * 
 * Example: <br> 
 * 
 * 0111010 ==> f=0 <br>
 * 1110010 ==> f=3
 * 
 *  @author Christian, Heiko
 */
public class LeadingOnes extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * The constructor of the class.
	 * 
	 * @param schedule a back-link to the currently used schedule.
	 */
	public LeadingOnes(Schedule schedule) {
		super(schedule);
	}

	public double evaluate(Genotype genotype) {
		int number = 0;
		BitSet set = ((BitStringGenotype)genotype).getBitSet();
		int dimension = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
		for (int k = 0; k < dimension; k++) {
			if (set.get(k) == true)
				number++;
			else
				return number;
		}
		return number;
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		BitSet bs = new BitSet(dimension);
		bs.set(0, dimension);
		return new BitStringGenotype(bs, dimension);
	}

	public String getDescription() {
		return "The number of leading ones or the length of the largest coherent block of ones from the first position in the bit string corresponds to the fitness value.";
	}

	public String getName() {
		return "LeadingOnes";
	}

}
