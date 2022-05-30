/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.stoppingcriterion;

import freak.core.control.Schedule;
import freak.core.event.GenerationEvent;
import freak.core.fitness.MultiObjectiveFitnessFunction;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.core.stoppingcriterion.AbstractGenerationStoppingCriterion;

/**
 * This stopping criterion stopps the algorithm when a representation of the
 * pareto front is contained in the current population.
 * 
 * @author Heiko
 */
public class ParetoFrontFound extends AbstractGenerationStoppingCriterion {

	public ParetoFrontFound(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof MultiObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on multi objective fitness functions only.");
		}
		try {
			((MultiObjectiveFitnessFunction)getSchedule().getFitnessFunction()).containsParetoFront(new Population(schedule));
		} catch(UnsupportedOperationException e) {
			// catch all exceptions and throw UnsupportedEnvironmentException
			throw new UnsupportedEnvironmentException("The stopping criterion "+
			"can't be applied to the chosen fitness function\n since the pareto front is unknown.");
		}				

	}

	protected void checkCriterion(GenerationEvent evt) {
		IndividualList p = getSchedule().getPopulationManager().getPopulation();
		MultiObjectiveFitnessFunction ff = (MultiObjectiveFitnessFunction)getSchedule().getFitnessFunction();
		if (ff.containsParetoFront(p)) {
			stopRun();	
		}		
	}

	public String getName() {
		return "Pareto Front Found";
	}

	public String getDescription() {
		return "Stopps the algorithm when a representation of the pareto front is contained in the current population.";
	}

}
