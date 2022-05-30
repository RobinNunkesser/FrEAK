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
 * The fitness function <code>Ridge</code>. <br>
 * 
 * The fitness value of an <code>Individual</code> is n-OneMax, except for
 * individuals with a genotype of the form 1^i 0^(n-i). For these individuals
 * it is n+i.
 *  
 * @author Christian
 */
public class Ridge extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * The constructor of the class.
	 * 
	 * @param schedule a back-link to the schedule.
	 */
	public Ridge(Schedule schedule) {
		super(schedule);
	}

	public double evaluate(Genotype genotype) {
		BitSet bitSet = ((BitStringGenotype)genotype).getBitSet();

		int dimension = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
		int cardinality = bitSet.cardinality();

		int leadingOnes = 0;

		for (; leadingOnes < dimension; leadingOnes++) {
			if (!bitSet.get(leadingOnes))
				break;
		}

		boolean isOfSpecialForm = cardinality - leadingOnes == 0;

		if (isOfSpecialForm) {
			return dimension + leadingOnes;
		} else {
			return dimension - cardinality;
		}
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return 2 * ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {

		// the optimum lies in 1^n ...
		int dimension = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension(); 
		BitSet bs = new BitSet(dimension);
		bs.set(0, dimension);

		return new BitStringGenotype(bs, dimension);
	}

	public String getDescription() {
		return "Contains a path of neighbored search points with strictly monotone fitness values which has to " + "be climbed to reach the optimum. The path consists of all individuals " + "with all ones in the left part and all zeros in the right part of " + "the genotype. The start of the path contains only zeros and the " + "global optimum contains only ones.\n" + "The fitness value for an individual on the path is n + OneMax.\n" + "The fitness value for all other individuals is n-OneMax, thus giving hints to reach " + "the start of the path.";
	}

	public String getName() {
		return "Ridge";
	}

}