/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

import java.util.Iterator;

import freak.core.control.Schedule;
import freak.core.modulesupport.IncompatibleModuleException;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.core.searchspace.SearchSpace;

/**
 * An abstract superclass for all mutation operators.
 * Mutation is an abstract superclass for all mutation operators. It is
 * designed to generalize mutation operators which perform mutation on one
 * individual to operators which perform mutation on a list of individuals.
 * It iterates the given individual list and performs a mutation on every
 * individual in the list.
 *
 * @author Dirk, Michael
 */
abstract public class Mutation extends AbstractVariation {

	/**
	 * Creates a new <code>Mutation</code> operator.
	 *
	 * @param graph a link back to the operator graph.
	 */
	public Mutation(OperatorGraph graph) {
		super(graph);
		super.addInPort();
		super.addOutPort();
	}

	/**
	 * This method must be overridden by the subclassses. It defines how a
	 * mutation on a single individual is performed.
	 */
	protected abstract Individual doMutation(Individual ind);

	/**
	 * This method iterates the individual list and calls doMutation to mutate
	 * the individuals.
	 */
	public IndividualList[] process(IndividualList[] input) throws GraphException {
		IndividualList[] output = new IndividualList[1];
		output[0] = new Population(graph.getSchedule(), input[0].size());

		Iterator it = input[0].iterator();
		while (it.hasNext()) {
			output[0].addIndividual(doMutation((Individual)it.next()));
		}
		return output;
	}

	public void testSchedule(Schedule schedule)
		throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		// test if the operator's package is compatible with the current
		// search space
		SearchSpace searchSpace = schedule.getGenotypeSearchSpace();
		String searchSpaceName = searchSpace.getClass().getName().toLowerCase();
		searchSpaceName = searchSpaceName.substring(searchSpaceName.lastIndexOf('.') + 1, searchSpaceName.length());
		
		String packageName = this.getClass().getPackage().getName();
		
		if (!(packageName.endsWith(searchSpaceName) || packageName.endsWith("common"))) {
			throw new IncompatibleModuleException(schedule.getGenotypeSearchSpace(), "Wrong search space.");
		}
	}

}
