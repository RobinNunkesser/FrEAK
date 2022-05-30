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
 * The fitness function <code>Plateau</code>. <br>
 * 
 * The fitness value of an <code>Individual</code> is n-OneMax, except for 
 * individuals with a genotype 1^i 0^(n-i). For these individuals it is n+1.
 * 
 * The genotype 1^n has the fitness value 2n.
 * 
 * @author Christian
 */
public class Plateau extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * The constructor of the class.
	 * 
	 * @param schedule a back-link to the currently used schedule.
	 */
	public Plateau(Schedule schedule) {
		super(schedule);
	}

	public double evaluate(Genotype genotype) {
		BitSet set = ((BitStringGenotype)genotype).getBitSet();

		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int cardinality = set.cardinality();

		int leadingOnes = 0;

		for (; leadingOnes < n; leadingOnes++) {
			if (!set.get(leadingOnes))
				break;
		}

		boolean isOfSpecialForm = cardinality - leadingOnes == 0;

		if (isOfSpecialForm) {
			if (leadingOnes == n)
				return 2 * n; // optimal value
			else
				return n + 1; // on plateau
		}

		return n - cardinality; // not on plateau
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return 2 * ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		// the optimum lies in 1^n ...
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension(); 
		BitSet bs = new BitSet(dimension);
		bs.set(0, dimension);

		return new BitStringGenotype(bs, dimension);
	}

	public String getDescription() {
		return "A plateau of search points with the same fitness value has to be " + "crossed to reach the optimum. The plateau consists of all individuals " + "with all ones in the left part and all zeroes in the right part of " + "the genotype. An exception is the global optimum: the individual " + "containing only ones gets a fitness value of 2n.\n" + "All individuals on the plateau get fitness n+1, the other individuals " + "get fitness n - OneMax, thus giving hints to reach the plateau at the opposite " + "side of the optimum.";
	}

	public String getName() {
		return "Plateau";
	}

}
