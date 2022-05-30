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
 * This class implements the fitness function Real Royal Road for one point
 * crossover. This function was defined by Ingo Wegener in "Real Royal Road
 * Functions - Functions Where Crossover Provably is Essential". <br>
 * The fitness of the bitstring x in {0,1}^n is defined as follows: <br>
 * 1. RRR(x) = 2n^2 if x = 1^n <br>
 * 2. RRR(x) = n*OneMax(x)+b(x) if OneMax(x) <= n-m <br>
 * 3. RRR(x) = 0 otherwise <br>
 * b(x) is the length of the longest block consisting of ones only.
 * 
 * @author Heiko
 */
public class RRR1ptXover extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * Creates a new object. 
	 * @param schedule a reference to a schedule.
	 */
	public RRR1ptXover(Schedule schedule) {
		super(schedule);
	}

	protected double evaluate(Genotype genotype) {
		BitSet bs = ((BitStringGenotype)genotype).getBitSet();
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int cardinality = bs.cardinality();
		if (cardinality == dimension) {
			return 2 * dimension * dimension;
		}
		int m = dimension / 3;
		if (cardinality > dimension - m) {
			return 0;
		}
		// get longest 1 block
		int longest = 0;
		int count = 0;
		for (int i = 0; i < dimension; i++) {
			if (bs.get(i)) {
				count++;
			} else {
				if (count > longest) {
					longest = count;
				}
				count = 0;
			}
		}
		if (count > longest) {
			longest = count;
		}
		return dimension * cardinality + longest;
	}

	public String getName() {
		return "Real Royal Road for 1pt Crossover";
	}

	public String getDescription() {
		return "This Real Royal Road function was designed in order to demonstrate that crossover can be essential.\n" + "The fitness of a search point x in {0,1}^n is defined as follows:\n" + "RRR(x) = 2n^2 if x = 1^n\n" + "RRR(x) = n*OneMax(x)+b(x) if OneMax(x) <= 2n/3\n" + "RRR(x) = 0 otherwise\n" + "OneMax(x) denotes the number of ones in x.\n" + "b(x) denotes the length of the longest block consisting of ones only.\n\n" + "This is the first function for which could be proven that a genetic algorithm with crossover can optimize it in expected polynomial time " + "while all evolution strategies based only on mutation and selection need expected exponential time.\n\n" + "For further details see \"Real Royal Road Functions - Functions Where Crossover Provably is Essential\" by Ingo Wegener.";
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		return 2 * n * n;
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension(); 
		BitSet bs = new BitSet(dimension);
		bs.set(0, dimension);
		return new BitStringGenotype(bs, dimension);
	}

}
