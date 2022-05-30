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
 * The fitness function <code>OneMax</code>. <br>
 * 
 * The fitness value of an <code>Individual</code> is the number of ones in its 
 * genotype.
 * 
 * @author Christian, Michael, Heiko
 */
public class OneMax extends AbstractStaticSingleObjectiveFitnessFunction {
	
	public OneMax(Schedule schedule) {
		super(schedule);
	}

	public double evaluate(Genotype genotype) {
		BitSet result = ((BitStringGenotype)genotype).getBitSet();
		return result.cardinality();
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		int dimension = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
		BitSet bs = new BitSet(dimension);
		bs.set(0, dimension);
		return new BitStringGenotype(bs, dimension);
	}

	public String getDescription() {
		return "The fitness value of an individual is the number of ones in " + "its genotype.";
	}

	public String getName() {
		return "OneMax";
	}

}
