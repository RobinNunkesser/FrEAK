/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.generalstring;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.core.searchspace.SearchSpace;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.operator.mutation.common.StandardMutation;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * A highly optimized (1+1)EA for the fitness function IsingModelCliques.
 * 
 * @author Dirk
 */
public class OptimizedCliqueEA extends StandardMutation {
	
	// a cache for bit-flips to be performed; each field contains two values for
	// the index of the bit's position and the new value 
	private int[][] flippedBits;
	// the array of flipped Bits is reused throughout all runs. Therefore, ii 
	// has got maximal size and a seperate variable is used to save the current 
	// amount of data contained from the current run.
	private int numberOfFlippedBits;  

	
	public OptimizedCliqueEA(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!(searchspace instanceof GeneralString)) {
			throw new UnsupportedEnvironmentException("Wrong searchspace");
		}
		
		if (!(graph.getSchedule().getRealFitnessFunction() instanceof IsingModelCliques)) {
			throw new UnsupportedEnvironmentException("Does only work with the fitness IsingModelCliques");
		}
	}
	
	public String getDescription() {
		return "A highly optimized (1+1)EA for the Ising model on cliques.";
	}
	
	public String getName() {
		return "Optimized Ising Cliques (1+1)EA";
	}
	
	/**
	 * Performs an adapted standard mutation. The effects of bit-flips are 
	 * analysed locally and the fitness difference between the offspring and its
	 * parent is computed. The offspring is actually created only in case the 
	 * fitness difference is at least 0. 
	 */
	protected Individual doMutation(Individual ind) {
		int dimension = ((HasDimension)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		int numChars = ((GeneralString)graph.getSchedule().getGenotypeSearchSpace()).getNumChars();
		int startindex = 0;
		int nextPos = getNextPosition(dimension - 1);
		
		double fitnessDifference = 0;
		
		GeneralStringGenotype gt = (GeneralStringGenotype)ind.getGenotype();
		int[] genotype = gt.getIntArray();
		
		// Save the bit flips that are to be made to the offspring.
		// The offspring is not yet created, we only check if the changes would 
		// yield a fitness improvement or not.
		if (flippedBits == null || flippedBits.length != dimension) flippedBits = new int[dimension][2];
		numberOfFlippedBits = 0;
		
		RandomElement re = graph.getSchedule().getRandomElement();
		while (nextPos != -1) {
			
			// simulate bit-flip
			int indexToFlip = startindex + nextPos;
			int oldValue = genotype[indexToFlip];
			int newValue = re.choose(0, numChars - 2);
			if (newValue == oldValue)
				newValue = (numChars - 1);

			flippedBits[numberOfFlippedBits][0] = indexToFlip;
			flippedBits[numberOfFlippedBits][1] = newValue;
			numberOfFlippedBits++;

			int[] adjacentNodes = ((IsingModelCliques)graph.getSchedule().getRealFitnessFunction()).getAdjacencyList(indexToFlip);
			int numberOfNodesWithOldValue = 0;
			int numberOfNodesWithNewValue = 0;
			int colorOfNeighbor;
			for (int i = 0; i < adjacentNodes.length; i++) {
				colorOfNeighbor = genotype[adjacentNodes[i]];

				// check if the neighbor has already been flipped
				for (int j = 0; j < numberOfFlippedBits - 1; j++) {
					// if so, take the new color instead
					if (flippedBits[j][0] == adjacentNodes[i]) {
						colorOfNeighbor = flippedBits[j][1];
					}  
				}

				if (colorOfNeighbor == oldValue) numberOfNodesWithOldValue++;
				if (colorOfNeighbor == newValue) numberOfNodesWithNewValue++;
			}
			fitnessDifference += numberOfNodesWithNewValue - numberOfNodesWithOldValue;

			startindex += nextPos + 1;
			nextPos = getNextPosition(dimension - startindex - 1);
		}
		
		if (fitnessDifference >= 0) {
			// the offspring replaces the parent
			GeneralStringGenotype newGenotype = (GeneralStringGenotype)gt.clone();
			
			// apply bit-flips saved before
			for (int i = 0; i < numberOfFlippedBits; i++) {
				newGenotype.set(flippedBits[i][0], flippedBits[i][1]);
			}

			double oldFitness = ((IsingModelCliques)graph.getSchedule().getRealFitnessFunction()).evaluate(ind, null);
			
			Individual result = new Individual(graph.getSchedule(), newGenotype, new Individual[] { ind });
			// fill cache with new fitness value
			result.setLatestKnownFitnessValue(new Double[] {new Double(oldFitness + fitnessDifference)});
			
			return result;
		} else {
			// no fitness improvement; thus, the parent is returned
			return ind; 
		}
	}
	
}
