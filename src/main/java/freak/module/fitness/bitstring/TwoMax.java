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
 * The fitness function <code>TwoMax</code>. <br>
 * 
 * The fitness value of an <code>Individual</code> I is the number of ones in its 
 * genotype if there are more ones than zeros in the genotype and the fitness value
 * is the number of zeros if there are more zeros than ones in the genotype.
 * 
 *     TwoMax(I) = |n/2 - u| + n/2
 * 
 * with n: the dimension of the Search Space
 *      u: the number of ones in the genotype of I
 * 
 * @author Dimo
 */
public class TwoMax extends AbstractStaticSingleObjectiveFitnessFunction {
	int dimension;
	
	public TwoMax(Schedule schedule) {
		super(schedule);
		dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	public double evaluate(Genotype genotype) {
		BitSet result = ((BitStringGenotype)genotype).getBitSet();
		return (Math.abs(dimension/2 - result.cardinality()) + (dimension/2));					
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return (((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension()/2);
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public String getDescription() {
		return "The fitness value of an individual I is the number of ones " + 
               "in its genotype if there are more ones than zeros in the " +
               "genotype and the fitness value is the number of zeros if " +
               "there are more zeros than ones in the genotype.\n\n" +
		 	   "TwoMax(I) = |n/2 - u| + n/2\n\n"+
		 	   "with n: the dimension of the Search Space\n" +
		 	   "u: the number of ones in the genotype of I"; 
	}

	public String getName() {
		return "TwoMax";
	}
}

