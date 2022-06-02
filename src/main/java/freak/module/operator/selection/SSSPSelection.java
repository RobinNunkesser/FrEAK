/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.selection;

import freak.core.control.Schedule;
import freak.core.fitness.FitnessFunction;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.GraphException;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Selection;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.module.fitness.generalstring.SSSPMultiObjective;
import freak.module.fitness.generalstring.SSSPSingleObjective;

import java.util.Iterator;

/**
 * This selection operator assumes that its input consists of exactly 2
 * individuals. If these individuals are comparable it returns the better one
 * (the newer one if their fitness values are identical). If the individuals
 * are uncomparable the older one is returned.
 * 
 * @author Heiko
 */
public class SSSPSelection extends Selection implements CompatibleWithDifferentSearchSpaces {

	public SSSPSelection(OperatorGraph graph) {
		super(graph);

		super.addInPort();
		super.addOutPort();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		FitnessFunction f = schedule.getRealFitnessFunction();
		if (!(f instanceof SSSPMultiObjective) && !(f instanceof SSSPSingleObjective)) {
			throw new UnsupportedEnvironmentException("Only works in connection with a SSSP fitness function.");
		}
	}

	public IndividualList[] process(IndividualList[] input) throws GraphException {
		if (input[0].size() <= 1) {
			return input;
		}
		
		// create the resulting lists ...
		IndividualList output[] = new IndividualList[1];
		
		// create a new IndividualList
		output[0] = new Population(graph.getSchedule(), 1);
		
		Iterator it = input[0].iterator();
		SSSPMultiObjective fitness = (SSSPMultiObjective)graph.getSchedule().getFitnessFunction();
		
		Individual first = (Individual)it.next();
		Individual second = (Individual)it.next();
		
		if (first.getDateOfBirth() > second.getDateOfBirth()) {
			Individual tmp = second;
			second = first;
			first = tmp;
		}
		
		int cmp = SSSPMultiObjective.compare(fitness.evaluate(first,null),fitness.evaluate(second,null));
		if (cmp == 0 || cmp == -1) {
			first = second;
		}
		output[0].addIndividual(first);
		
		return output;
	}

	public String getName() {
		return "SSSP Selection";
	}

	public String getDescription() {
		return "This selection operator assumes that its input consists of exactly 2 "+
 			"individuals. If these individuals are comparable it returns the better one "+
 			"(the newer one if their fitness values are identical). If the individuals "+
 			"are uncomparable the older one is returned.";
	}

}
