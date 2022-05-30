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
 * This class represents the fitness function Path With Trap, which is defined
 * in "On the Analysis of a Dynamic Evolutionary Algorithm" by Thomas Jansen and
 * Ingo Wegener. <br>
 * The fitness of the bitstring x in {0,1}^n is defined as follows: <br>
 * 1. PWT(x) = 3n if x = 1^n <br>
 * 2. PWT(x) = n+i if x = 1^i0^{n-i} <br>
 * 3. PWT(x) = 2n if x lies in T <br>
 * 4. PWT(x) = n - ONEMAX(x) otherwise <br>
 * T is the trap which contains local optimal search points. x lies in T if
 * n/4 <= ONEMAX(x) <= 3n/4 and if a point on the path 1^i0^{n-i} exists which
 * has a distance to x which lies in the interval [n/12,n/6]. Furthermore the
 * distance of x to every path point must be at least n/24. <br>
 * This function demonstrates that the dynamic (1+1)EA can get trapped.
 * 
 * @author Heiko
 */
public class PathWithTrap extends AbstractStaticSingleObjectiveFitnessFunction {

	/**
	 * Constructs a new <code>PathWithTrap</code> object.
	 * 
	 * @param schedule
	 */
	public PathWithTrap(Schedule schedule) {
		super(schedule);
	}

	/**
	 * Checks whether the given search points lies in the trap.
	 * @param bs the search point to be checked.
	 * @return true iff <code>bs</code> lies in the trap. 
	 */
	private boolean trapped(BitSet bs) {
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int onemax = bs.cardinality();
		if ((onemax < n / 4) || (onemax > 3 * n / 4)) {
			return false;
		}
		int onesInFirstPart = 0;
		boolean foundPoint = false;
		for (int i = 0; i < n; i++) {
			int zeroesInFirstPart = i - onesInFirstPart;
			int differenceInSecondPart = onemax - onesInFirstPart;
			int difference = zeroesInFirstPart + differenceInSecondPart;
			if ((difference <= n / 6) && (difference >= n / 12)) {
				foundPoint = true;
			}
			if (difference < n / 24) {
				return false;
			}
			if (bs.get(i)) {
				onesInFirstPart++;
			}
		}
		return foundPoint;
	}

	protected double evaluate(Genotype genotype) {
		BitSet bs = ((BitStringGenotype)genotype).getBitSet();
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int cardinality = bs.cardinality();
		// Did we reach the optimum?
		if (cardinality == dimension) {
			return 3 * dimension;
		}
		boolean onPath = true;
		boolean firstPart = true;
		int leadingOnes = 0;
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
		// If it is not on the path we check if it lies in the trap.
		if (trapped(bs)) {
			return 2 * dimension;
		}
		// If it is neither on the path nor in the trap we return dimension-onemax.
		return dimension - bs.cardinality();
	}

	public String getName() {
		return "Path With Trap";
	}

	public String getDescription() {
		return "A function to demonstrate that the dynamic (1+1)EA can get trapped. " + "The individuals have to climb a path to the global optimum. Near the path is a line of traps, so " + "increasing the mutation probability causes the individuals to leave the path and jump into the traps.";
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

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension(); 
		BitSet bs = new BitSet(dimension);
		bs.set(0, dimension);
		return new BitStringGenotype(bs, dimension);
	}

}
