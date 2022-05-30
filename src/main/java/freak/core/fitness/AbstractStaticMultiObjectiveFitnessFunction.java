/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.fitness;

import freak.core.control.Schedule;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.population.IndividualList;

/**
 * This class can be used as abstract superclass of all multi objective fitness
 * functions which determine the fitness of an individual independently from the
 * other individuals in the population and independently from the age of the
 * individual. For these fitness functions the fitness value of an individual
 * can be cached since it doesn't depend on the current environment. 
 * 
 * @author Heiko
 */
public abstract class AbstractStaticMultiObjectiveFitnessFunction extends AbstractMultiObjectiveFitnessFunction {

	/**
	* Constructs a new <code>AbstractStaticMultiObjectiveFitnessFunction</code>
	* with a link back to the current schedule.
	* 
	* @param schedule a link back to the current schedule.
	*/
	public AbstractStaticMultiObjectiveFitnessFunction(Schedule schedule) {
		super(schedule);
	}

	/**
	 * This method implements the use of a cache for fitness values.
	 */
	public final double[] evaluate(Individual individual, IndividualList list) {
		// First, check whether the fitness value is cached.		
		Double[] d = individual.getLatestKnownFitnessValue();
		if (!(d == null)) {
			int bound = d.length;
			double[] result = new double[bound];			
			for (int i = 0; i < bound; i++) {
				result[i] = d[i].doubleValue();
			}
			return result;
		}
		double[] fitness = evaluate(individual.getPhenotype());
		int bound = fitness.length;
		Double[] cache = new Double[bound];
		for (int i = 0; i < bound; i++) {
			cache[i] = new Double(fitness[i]); 
		}
		individual.setLatestKnownFitnessValue(cache);		
		return fitness;
	}
	
	/**
	 * This method must be overriden by subclasses. It must determine the
	 * fitness value of the given individual independently from the its age.
	 * @param individual the individual whose fitness is to be evaluated.
	 * @return the fitness value of the given individual.
	 */
	abstract protected double[] evaluate(Genotype genotype);

}
