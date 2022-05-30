/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.mapper;

import freak.core.modulesupport.*;
import freak.core.searchspace.*;
import freak.core.population.*;

/**
 * A mapper defines a transformation between the phenotype search space of the
 * fitness functions and the genotype search space. Only on the genotype
 * search space the genotypes of indivuals will modified and created by mutation
 * and recombination.
 *
 * @author Michael
 */
public interface Mapper extends Module {
	
	/**
	 * This method returns the search space where FrEAK works on. It will be
	 * parameterised by the phenotype search space of the fitness function.
	 *
	 * @return the genotype search space.
	 */
	SearchSpace getGenotypeSearchSpace();
	
	/**
	 * This method maps the genotype to a phenotype.
	 *
	 * @return the phenotype
	 */
	Genotype genotypeToPhenotype(Genotype genotype);
	
	/**
	 * This method maps the phenotype to a genotype or throws an 
	 * UnsupportedOperationException when no mapping from phenotype to genotype
	 * exists.
	 *
	 * @return the phenotype
	 */
	Genotype phenotypeToGenotype(Genotype phenotype) throws UnsupportedOperationException;
}
