/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover;

import freak.core.graph.*;
import freak.core.population.*;
import freak.module.searchspace.BooleanFunctionGenotype;

import java.util.Iterator;

/**
 * MultiPairwiseCrossover is an abstract class. It is designed to generalize
 * crossover operators which perform crossover on exactly two individuals to
 * operators which perform crossover on more individuals . It has two inports.
 * It iterates both individual lists simultaneously and performs pairwise
 * crossovers on two individuals (one from each list). Therefore the number of
 * individuals must be equal in both lists.
 *
 * @author Michael
 */
abstract public class MultiPairwiseCrossover extends Recombination {
	
	/**
	 * Creates a new MultiPairwiseCrossover object.
	 */
	public MultiPairwiseCrossover(OperatorGraph graph) {
		super(graph);
		super.addInPort();
		super.addInPort();
		super.addOutPort();
	}

	/**
	 * This method must be overridden by the subclassses. It defines how a 
	 * pairwise crossover is performed.
	 */
	protected abstract Individual doCrossover(Individual ind1, Individual ind2);

	/**
	 * This method iterates the individual lists and calls doCrossover to do 
	 * pairwise crossovers.
	 */
	public IndividualList[] process(IndividualList[] input) throws GraphException {
		IndividualList[] output = new IndividualList[1];

		if (input[0].size() != input[1].size()){
			BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)(input[0].getIndividual(0)).getGenotype();
			BooleanFunctionGenotype bfg2 = (BooleanFunctionGenotype)(input[1].getIndividual(0)).getGenotype();
			throw new DifferentNumberOfIndividualsException("Both individual lists must have the same number of individuals, " +
					" but the sizes are "+input[0].size()+" and "+input[1].size()+"." +
							bfg.getPopulation()+" "+bfg2.getPopulation());
		}

		output[0] = new Population(graph.getSchedule(), input[0].size());

		Iterator[] it = new Iterator[2];
		it[0] = input[0].iterator();
		it[1] = input[1].iterator();

		while (it[0].hasNext()) {
			output[0].addIndividual(doCrossover((Individual)it[0].next(), (Individual)it[1].next()));
		}
		return output;
	}

}
