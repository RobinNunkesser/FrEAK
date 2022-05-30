/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.fitness;

import freak.core.modulesupport.*;
import freak.core.population.*;

/**
 * The interface for a fitness function. A fitness function evaluates
 * individuals and can provide some optional operations that give further
 * information about the fitness function. In FrEAK, single objective and multi
 * objective fitness functions are distinguished. Therefore, the interfaces
 * <code>SingleObjectiveFitnessFunction</code> and <code>MultiObjectiveFitnessFunction</code>,
 * which extend this interface, contain more specialized methods.
 * 
 * @author Dirk, Heiko, Michael
 */
public interface FitnessFunction extends Module {

	/**
	 * Returns an optimal phenotype, if an optimum exists and is known.
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown.
	 * 
	 * @return an optimal phenotype.
	 * @throws UnsupportedOperationException if the optimum is unknown.
	 */
	public abstract Genotype getPhenotypeOptimum() throws UnsupportedOperationException;

	/**
	 * Returns a genotype of the optimal phenotype, if an optimum exists and
	 * is known and a mapper can translate the phenotype to genotype.
	 * Otherwise, an <code>UnsupportedOperationException</code> is thrown.
	 * 
	 * @return an optimal phenotype.
	 * @throws UnsupportedOperationException if the optimum is unknown.
	 */
	public abstract Genotype getGenotypeOptimum() throws UnsupportedOperationException;

}