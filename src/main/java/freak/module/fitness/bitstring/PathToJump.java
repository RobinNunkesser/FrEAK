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
 * This class represents the fitness function Path To Jump, which is defined in
 * "On the Analysis of a Dynamic Evolutionary Algorithm" by Thomas Jansen and
 * Ingo Wegener. <br>
 * The fitness of the bitstring x in {0,1}^n is defined as follows: <br>
 * 1. PTJ(x) = n+i if x = 1^i0^{n-i} <br>
 * 2. PTJ(x) = 3n if x lies in I <br>
 * 3. PTJ(x) = n - ONEMAX(x) otherwise <br>
 * I is an island which contains the optimal search points. x lies in I if
 * 3n/4 <= ONEMAX(x) <= 7n/8 and the distance of x to every point on the path
 * 1^i0^{n-i} is at least n/16. <br>
 * This function demonstrates that the dynamic (1+1)EA can outperform each
 * static (1+1) EA drastically.      
 * 
 * @author Heiko
 */
public class PathToJump extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * Creates a new <code>PathToJump</code> object.
	 * 
	 * @param schedule a reference to a schedule object.
	 */
	public PathToJump(Schedule schedule) {
		super(schedule);
	}

	/**
	 * Checks whether the given search points lies on the island.
	 * @param bs the search point to be checked.
	 * @return true iff <code>bs</code> lies on the island. 
	 */
	private boolean onIsland(BitSet bs) {
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int onemax = bs.cardinality();
		if ((onemax < 3 * n / 4) || (onemax > 7 * n / 8)) {
			return false;
		}
		int onesInFirstPart = 0;
		for (int i = 0; i < n; i++) {
			int zeroesInFirstPart = i - onesInFirstPart;
			int differenceInSecondPart = onemax - onesInFirstPart;
			if (zeroesInFirstPart + differenceInSecondPart < n / 16) {
				return false;
			}
			if (bs.get(i)) {
				onesInFirstPart++;
			}
		}
		return true;
	}

	protected double evaluate(Genotype genotype) {
		BitSet bs = ((BitStringGenotype)genotype).getBitSet();
		boolean onPath = true;
		boolean firstPart = true;
		int leadingOnes = 0;
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		// First, we check if the search point lies on the path.
		for (int i = 0; i < dimension; i++) {
			boolean bit = bs.get(i);
			if ((!firstPart) && bit) {
				onPath = false;
				break;
			}
			if (!bit) {
				firstPart = false;
			} else {
				leadingOnes++;
			}
		}
		if (onPath) {
			return dimension + leadingOnes;
		}
		// If it is not on the path we check if it lies on the island.
		if (onIsland(bs)) {
			return 3 * dimension;
		}
		// If it is neither on the path nor on the island we return dimension-onemax.
		return dimension - bs.cardinality();
	}

	public String getName() {
		return "Path To Jump";
	}

	public String getDescription() {
		return "A function to demonstrate that the dynamic (1+1)EA can outperform each " + "static (1+1) EA drastically. The individuals have to climb a path and then jump " + "into an area of global optima. These two tasks require different mutation probabilities.";
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return 3 * ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

}
