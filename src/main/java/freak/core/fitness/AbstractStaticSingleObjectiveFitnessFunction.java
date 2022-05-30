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
 * This class can be used as abstract superclass of all single objective fitness
 * functions which determine the fitness of an individual independently from the
 * other individuals in the population and independently from the age of the
 * individual. For these fitness functions the fitness value of an individual
 * can be cached since it doesn't depend on the current environment.
 *
 * @author Heiko, Michael
 */
public abstract class AbstractStaticSingleObjectiveFitnessFunction extends AbstractSingleObjectiveFitnessFunction implements SingleObjectiveFitnessFunction {
	
	/**
	 * Constructs a new <code>AbstractStaticSingleObjectiveFitnessFunction</code>
	 * with a link back to the current schedule.
	 *
	 * @param schedule a link back to the current schedule.
	 */
	public AbstractStaticSingleObjectiveFitnessFunction(Schedule schedule) {
		super(schedule);
	}
	
	/**
	 * This method implements the use of a cache for fitness values.
	 */
	public final double evaluate(Individual individual, IndividualList list) {
		// First, check whether the fitness value is cached.
		Double[] d = individual.getLatestKnownFitnessValue();
		if (!(d == null)) {
			return d[0].doubleValue();
		}
		double fitness = evaluate(individual.getPhenotype());
		Double[] result = {new Double(fitness)};
		individual.setLatestKnownFitnessValue(result);
		return fitness;
	}
	
	/**
	 * This method must be overriden by subclasses. It must determine the
	 * fitness value of the given individual independently from the its age.
	 * @param individual the individual whose fitness is to be evaluated.
	 * @return the fitness value of the given individual.
	 */
	abstract protected double evaluate(Genotype genotype);
	
	/**
	 * Returns a value of 1, if individual ind1 is better than individual ind2.
	 * It returns 0 if none of the two individuals is better.
	 * It returns -1 if ind2 better than ind1.
	 */
	public int compareIndividuals(Individual ind1, Individual ind2) throws UnsupportedOperationException {
		if (ind1 == ind2) return 0;
		double f1 = evaluate(ind1, null);
		double f2 = evaluate(ind2, null);
		if (f1 > f2) return 1;
		if (f1 < f2) return -1;
		return 0;
	}
}
