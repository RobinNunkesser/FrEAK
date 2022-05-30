/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover.common;

import java.util.BitSet;
import java.util.Iterator;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Recombination;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.core.searchspace.HasDimension;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * Takes a whole population of size n as input and performs a genepool
 * crossover. That means numChildren Children are created. For each character
 * the number of occurrences k at position i is counted and the i-th position in
 * each child is set to the character with probability k/n.
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael
 */
public class GenepoolCrossover extends Recombination implements Configurable, CompatibleWithDifferentSearchSpaces {
	
	protected int numChildren = 1; //the number of children created by this operator
	
	public GenepoolCrossover(OperatorGraph graph) {
		super(graph);

		super.addOutPort();
		super.addInPort();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof GeneralString) || (searchspace instanceof BitString)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}
	
	/**
	 * Performs the genepool crossover and creates numChildren offsprings.
	 */
	public IndividualList[] process(IndividualList[] input) {
		SearchSpace searchspace = graph.getSchedule().getGenotypeSearchSpace();
		RandomElement re = graph.getSchedule().getRandomElement();
		int dimension = ((HasDimension)searchspace).getDimension();
		IndividualList[] output = new IndividualList[1];
		output[0] = new Population(graph.getSchedule(), numChildren);
		Individual ind;
		Iterator it;
		int sumInd = 0;
		
		if (searchspace instanceof BitString) {
			
			BitSet bs;
			int[] stats = new int[dimension];
			/* counts the occurrence of ones at each position of every genotypes
			 * in the population
			 */
			for (int i = 0; i < input.length; i++) {
				it = input[i].iterator();
				while (it.hasNext()) {
					// counts the number of individuals in the population
					sumInd++;
					ind = (Individual)it.next();
					bs = ((BitStringGenotype)ind.getGenotype()).getBitSet();
					for (int j = 0; j < dimension; j++) {
						if (bs.get(j))
							stats[j]++;
					}
				}
			}
		/* create numChildren offsprings
		 * the i-th bit in each offspring is set to one with probability
		 * (number of ones in the population at position i)/(number of
		 * individuals in the population)
		 */
			for (int j = 0; j < numChildren; j++) {
				bs = new BitSet(dimension);
				for (int i = 0; i < dimension; i++) {
					if (re.choose(sumInd) <= stats[i])
						bs.set(i);
				}
				output[0].addIndividual(new Individual(graph.getSchedule(), new BitStringGenotype(bs, dimension), new Individual[] {
				}));
			}
			
		} else {
			// now for generalstring
			int numChars = ((GeneralString)searchspace).getNumChars();
			int[] genotype;
			int[][] stats = new int[dimension][numChars];
			
			/* counts the occurrences of each char at each position of every
			 * genotypes in the population
			 */
			for (int i = 0; i < input.length; i++) {
				it = input[i].iterator();
				while (it.hasNext()) {
					// counts the number of individuals in the population
					sumInd++;
					ind = (Individual)it.next();
					genotype = ((GeneralStringGenotype)ind.getGenotype()).getIntArray();
					for (int j = 0; j < dimension; j++) {
						stats[j][genotype[j]]++;
					}
				}
			}
			
			// Sum to sorted lists
			for (int i = 0; i < dimension; i++) {
				for (int j = 0; j < numChars - 1; j++) {
					stats[i][j + 1] += stats[i][j];
				}
			}
		/* create numChildren offsprings
		 * the position i in each offspring is set to a character with
		 * probability
		 * (number of character in the population at position i)/(number of
		 * individuals in the population)
		 */
			int v;
			for (int j = 0; j < numChildren; j++) {
				genotype = new int[dimension];
				for (int i = 0; i < dimension; i++) {
					v = re.choose(sumInd);
					for (int b = 0; b < numChars; b++) {
						if (v <= stats[i][b]) {
							genotype[i] = b;
							//cancel for loop
							b = numChars;
						}
					}
					
				}
				output[0].addIndividual(new Individual(graph.getSchedule(), new GeneralStringGenotype(genotype, numChars), new Individual[] {
				}));
			}
		} //end generalstring
		return output;
	}
	
	/**
	 * Sets the number of offsprings to generate
	 */
	public void setPropertyNumChildren(Integer num) {
		if (num.intValue() >= 0)
			numChildren = num.intValue();
	}
	
	/**
	 * @return the number of offsprings to generate
	 */
	public Integer getPropertyNumChildren() {
		return new Integer(numChildren);
	}
	
	public String getShortDescriptionForNumChildren() {
		return "Created offsprings";
	}
	
	public String getLongDescriptionForNumChildren() {
		return "The number of offsprings to be created.";
	}
	
	public String getDescription() {
		if (graph.getSchedule().getGenotypeSearchSpace() instanceof BitString) {
			return "Takes a whole population as input and performs a genepool crossover to create the specified number of offsprings: " + "the number k of ones at position i is counted and the i-th bit in each child is set to one with probability k/n if n is the number of incoming individuals.";
		} else {
			return "Takes a whole population as input and performs a genepool crossover to create the specified number of offsprings: " + "the number k of every char at position i is counted and the i-th position in each child is set to a char with probability k/n if n is the number of incoming individuals.";
		}
	}
	
	public String getName() {
		return "Gene Pool Crossover";
	}
}
