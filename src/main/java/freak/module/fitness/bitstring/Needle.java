/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.bitstring;

import freak.core.control.Schedule;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.population.Genotype;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import java.util.BitSet;

/**
 * This class implements the fitness function Needle. <br> The only search point with value one is the position of the needle, the whole rest of the search space gets the value 0.
 * @author  Christian, Stefan, Dirk
 */

public class Needle extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * The position of the needle.
	 */
	private BitStringGenotype needlePos;

	/**
	 * The constructor of the class.
	 * 
	 * @param schedule a back-link to the currently used schedule.
	 */
	public Needle(Schedule schedule) {
		super(schedule);
	}
	
	public void initialize() {
		super.initialize();

		int dim = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		// choose new needle if the search space dimension has changed
		if (needlePos != null && needlePos.size() != dim) {
			// set needlePos to 1^n
			BitSet bs = new BitSet(dim);
			bs.set(0, dim);
			needlePos = new BitStringGenotype(bs, dim);
		}
	}

	public double evaluate(Genotype genotype) {
		if (((BitStringGenotype)genotype).getBitSet().equals(needlePos.getBitSet()))
			return 1;
		else
			return 0;
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return needlePos.getBitSet().length();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {

		// the optimum lies at position ...
		return needlePos;
	}

	public String getDescription() {
		return "The fitness value of each individual is zero except for one " + "point in the whole search space. There is no clue to the direction " + "of the optimum, so we search a needle in a haystack. This needle has got a fitness value of 1.";
	}

	public String getName() {
		return "Needle";
	}

}
