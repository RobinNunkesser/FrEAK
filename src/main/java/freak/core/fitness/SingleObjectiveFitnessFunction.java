/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.fitness;

import freak.core.population.*;

/**
 * The interface for a single objective fitness function. A single objective
 * fitness function maps an individual to a real valued fitness value.
 *
 * @author Dirk, Heiko, Michael
 */
public interface SingleObjectiveFitnessFunction extends FitnessFunction {
	/**
	 * Evaluates an individual with respect to the specified
	 * <code>IndividualList</code>. The <code>IndividualList</code>
	 * is used when the fitness of one individual depends on some
	 * population, e.g., when niching strategies are used.
	 *
	 * @param individual the individual to be evaluated.
	 * @param list the individuals to be used by the fitness evaluation if the
	 * fitness depends on the surrounding individuals.
	 * @return the fitness of the specified individual.
	 */
	public abstract double evaluate(Individual individual, IndividualList list);
	
	/**
	 * Fitness Functions implementing this interface contain a method
	 * for comparing directly two Genotypes. This is needed for example
	 * in the context of the MST Fitness Functions, where fitness values
	 * calculated the normal way would return values too big to be represented
	 * with the normal data types.
	 * Returns a value of 1, if individual ind1 is better than individual ind2.
	 * It returns 0 if none of the two individuals is better.
	 * It returns -1 if ind2 better than ind1.
	 */
	public int compareIndividuals(Individual ind1, Individual ind2) throws UnsupportedOperationException;
	
	/**
	 * Returns the optimal fitness value if the operation is supported.
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown.
	 *
	 * @return the optimal fitness value.
	 * @throws UnsupportedOperationException if the optimal fitness value is unknown.
	 */
	public abstract double getOptimalFitnessValue() throws UnsupportedOperationException;
	
	/**
	 * Returns a lower bound for the fitness values of all individuals in the
	 * search space if known.
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown.
	 *
	 * @return a lower bound for the fitness values.
	 * @throws UnsupportedOperationException if no lower bound is known.
	 */
	public abstract double getLowerBound() throws UnsupportedOperationException;
	
	/**
	 * Returns an upper bound for the fitness values of all individuals in the
	 * search space if known.
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown.
	 *
	 * @return a upper bound for the fitness values.
	 * @throws UnsupportedOperationException if no upper bound is known.
	 */
	public abstract double getUpperBound() throws UnsupportedOperationException;
	
}
