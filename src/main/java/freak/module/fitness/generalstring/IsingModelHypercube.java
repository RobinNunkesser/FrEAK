/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.generalstring;

import freak.core.control.Schedule;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.population.Genotype;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * An implementation of the Ising fitness function on a hypercube.
 * 
 * @author Dirk
 */
public class IsingModelHypercube extends AbstractStaticSingleObjectiveFitnessFunction implements Configurable {

	private int k;
	private int cubeLength;
	
	// a cache for powers of cubeLength
	private int[] factors;

	public IsingModelHypercube(Schedule schedule) {
		super(schedule);
		setPropertyDimension(new Integer(1));
	}
	
	public void initialize() {
		super.initialize();
		
		if (factors != null) {
			// check if the hypercube's size is still equal to the search space 
			// dimension
			if (factors[factors.length-1] * cubeLength != ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension()) {
				// reset dimension to default value
				setPropertyDimension(new Integer(1));
			}
		}
	}
	
	protected double evaluate(Genotype genotype) {
		double fitness = 0;
		int[] gen = ((GeneralStringGenotype)genotype).getIntArray();
		
		// iterate all nodes
		for (int i = 0; i < gen.length; i++) {
			
			// check neighbors for all k dimensions
			for (int j = 0; j < k; j++) {
				int neighbor1 = getIndexOfNeighbor(i, j, +1);
				// edges are only counted once, namely if the first node has got 
				// the lower index 
				if ((neighbor1 > i) && (gen[i] == gen[neighbor1])) fitness++;
				
				// in a boolean hypercube, there is only one neighbor in each 
				// dimension.
				if (cubeLength > 2) {
					int neighbor2 = getIndexOfNeighbor(i, j, -1);
					if ((neighbor2 > i) && (gen[i] == gen[neighbor2])) fitness++;
				}
			}
		}
		
		return fitness;
	}
	
	/**
	 * Returns the index of the neighbor of node. The neighbor is specified by 
	 * the dimension d where the two nodes are neighbored and the offset 
	 * specifying the difference between the two nodes within dimension d.
	 */
	private int getIndexOfNeighbor(int node, int d, int offset) {
		// compute value of node in dimension d 
		int dValueOfNode = (node % (factors[d] * cubeLength)) / factors[d];
		// compute index of the node where the value in dimension d is set to zero
		int indexWithdZero = node - dValueOfNode * factors[d];
		// value of neighbor in dimension d
		int neighborsdValue = (dValueOfNode + offset + cubeLength) % cubeLength;
		// return index of node where the value in dimension d is set correctly
		return indexWithdZero + neighborsdValue * factors[d];
	}
	
	public String getName() {
		return "Ising Model (Hypercube)";
	}

	public String getDescription() {
		return "The Ising Model is a model derived from statistical mechanics " +
			   "describing the behavior of atoms with two spins where neighbored atoms tend to orient in the same direction as their neighbors.\n" +
			   "Another view is an inversion of the colorability problem: an edge contributes a value of 1 to the fitness if and only if its nodes have got the same color. So, all colorings with only one color are optimal.\n" +
			   "Here, the graph is described by a hypercube.";
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		int dim = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension();
		
		// every node has got 2k neighbors except the case cubeLength = 2
		// every edge has two nodes, so we divide the sum of all neighbors by 2
		return (cubeLength == 2 ? dim * k / 2 : dim * k);
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public void setPropertyDimension(Integer i) {
		if (i.intValue() > 0) {

			int dim = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension();
			// compute the i-th root of the genotype length
			int newCubeLength = (int)Math.round(Math.pow(dim, 1/i.doubleValue()));

			int[] newFactors = new int[i.intValue()];
			newFactors[0] = 1;
			for (int j = 1; j < i.intValue(); j++) {
				newFactors[j] = newFactors[j-1] * newCubeLength;
			}

			// check if the hypercube has got size dim
			if (newFactors[newFactors.length-1] * newCubeLength == dim) {
				k = i.intValue();
				factors = newFactors;
				cubeLength = newCubeLength;
			}
		}
	}

	public Integer getPropertyDimension() {
		return new Integer(k);
	}
	
	public String getShortDescriptionForDimension() {
		return "Hypercube dimension";
	}
	
	public String getLongDescriptionForDimension() {
		return "The dimension of the hypercube. The length of all sides of the " +
			"hypercube is the dimension-th root of the genotype's length. " +
			"Since the length must be an integer, the genotype's length must be " +
			"of value a^dimension for some a.";     
	}

}
