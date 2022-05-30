/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.selection;

import java.util.Iterator;

import freak.core.control.Schedule;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.DifferentNumberOfIndividualsException;
import freak.core.graph.GraphException;
import freak.core.graph.HasFloatingNumberOfInPorts;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Selection;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;

/**
 * Competes individuals of the same position of all InPorts.
 *
 * @author Michael
 */
public class SeriationSelection extends Selection implements HasFloatingNumberOfInPorts, CompatibleWithDifferentSearchSpaces {

	/**
	 * The constructor of the class <code>SeriationSelection</code>.
	 *
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public SeriationSelection(OperatorGraph graph) {
		super(graph);
		addOutPort();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This operator works on single objective fitness functions only.");
		}
	}

	public IndividualList[] process(IndividualList[] input) throws GraphException {
		int inports = getNumberOfInPorts();
		Individual theWinner, tmpI;
		double winnerFitness, tmpF;
		IndividualList[] output = new IndividualList[1];
		int inputsize = input[0].size();
		output[0] = new Population(graph.getSchedule(), inputsize);
		SingleObjectiveFitnessFunction eval = (SingleObjectiveFitnessFunction)graph.getSchedule().getFitnessFunction();
		for (int i = 1; i < inports; i++) {
			if (inputsize != input[i].size())
				throw new DifferentNumberOfIndividualsException("All individual lists must have the same number of individuals.");
		}

		Iterator[] it = new Iterator[inports];
		for (int i = 0; i < inports; i++) {
			it[i] = input[i].iterator();
		}

		for (int i = 0; i < inputsize; i++) {
			theWinner = (Individual)it[0].next();
			winnerFitness = eval.evaluate(theWinner, input[0]);
			for (int j = 1; j < inports; j++) {
				tmpI = (Individual)it[j].next();
				tmpF = eval.evaluate(tmpI, input[0]);
				if (winnerFitness < tmpF) {
					theWinner = tmpI;
					winnerFitness = tmpF;
				}
			}
			output[0].addIndividual(theWinner);
		}
		return output;
	}

	public String getName() {
		return "Seriation Selection";
	}

	public String getDescription() {
		return "Competes individuals of the same position of all InPorts.";
	}

}
