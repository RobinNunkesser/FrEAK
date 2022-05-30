/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.fitness;

import freak.core.population.Individual;
import freak.core.population.IndividualList;

/**
 * The interface for a multi objective fitness function. A multi objective
 * fitness function maps an individual to a vector consisting of real numbers.
 * 
 * @author Heiko
 */
public interface MultiObjectiveFitnessFunction extends FitnessFunction {
	/**
	* Evaluates an individual with respect to the specified
	* <code>IndividualList</code>. The <code>IndividualList</code>
	* is used when the fitness of one individual depends on some
	* population, e.g., when niching strategies are used.
	* 
	* @param individual the individual to be evaluated.
	* @param list the individuals to be used by the fitness evaluation if the 
	* fitness depends on the surrounding individuals.
	* @return the fitness vector of the specified individual.
	*/
	public abstract double[] evaluate(Individual individual, IndividualList list);

	/**
	 * Returns the optimal fitness value if the operation is supported.
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown. In
	 * most cases an optimal fitness vector doesn't exist and the pareto front
	 * consists of more than one element.  
	 * 
	 * @return the optimal fitness value.
	 * @throws UnsupportedOperationException if the optimal fitness value is unknown.
	 */
	public abstract double[] getOptimalFitnessValue() throws UnsupportedOperationException;
	
	/**
	 * Returns a lower bound for each component of the fitness vector if known.
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown.
	 * 
	 * @return a lower bound for each component of the fitness vector.
	 * @throws UnsupportedOperationException if no lower bound is known.
	 */
	public abstract double[] getLowerBounds() throws UnsupportedOperationException;
	
	/**
	 * Returns an upper bound for each component of the fitness vector if known.
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown.
	 * 
	 * @return a upper bound for each component of the fitness vector.
	 * @throws UnsupportedOperationException if no upper bound is known.
	 */
	public abstract double[] getUpperBounds() throws UnsupportedOperationException;
	
	/**
	 * Returns the dimension of the objective space, i.e. the number of
	 * components in a fitness vector returned by evaluate.
	 * 
	 * @return the dimension of the objective space.
	 */
	public abstract int getDimensionOfObjectiveSpace();
	
	/**
	 * Checks whether the given <code>IndividualList</code> cotains a
	 * representation of the pareto front, if the pareto front is known. 
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown.
	 * @param list the list of individuals to be checked.
	 * @return true iff list contains a representation of the pareto front.
	 * @throws UnsupportedOperationException if the pareto front is unknown.
	 */
	public abstract boolean containsParetoFront(IndividualList list) throws UnsupportedOperationException;
	
}
