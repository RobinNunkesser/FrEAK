/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.stoppingcriterion;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.core.stoppingcriterion.*;

/**
 * This stopping criterion is checked periodically each time after a specified
 * number of generations have passed. If the population contains an optimal
 * individual at that time the algorithm is stopped.  
 * 
 * @author Heiko
 */
public class OptimumReached extends AbstractGenerationStoppingCriterion {

	private boolean all;

	/**
	 * Creates a new stopping criterion which terminates the run if an optimal
	 * search point is reached.
	 * @param schedule a reference to the schedule.
	 */
	public OptimumReached(Schedule schedule) {
		super(schedule);
	}
	
	/**
	 * @throws UnsupportedEnvironmentException the fitness function must be able to calculate its optimum.
	 */
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}
		try {
			((SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction()).getOptimalFitnessValue();
		} catch(UnsupportedOperationException e) {
			// catch exception and throw UnsupportedEnvironmentException
			throw new UnsupportedEnvironmentException("The stopping criterion " + "can't be applied to the chosen fitness function\n since the optimal fitness value is unknown.");
		}
	}

	public String getName() {
		return "Optimum Reached";
	}

	public String getDescription() {
		return "Stops a run if the optimum is reached.";
	}

	/**
	 * This method is called when a new generation is completely created. It
	 * checks whether an optimal search point was found or not. It stops the run
	 * if an optimal search point was found. 
	 */
	public void checkCriterion(GenerationEvent evt) {
		try {
			IndividualList p = getSchedule().getPopulationManager().getPopulation();
			SingleObjectiveFitnessFunction fitnessFunction = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();
			double fitness = 0;
			Individual compare = null;
			if (!all)
				compare = p.size() == 1 ? p.getIndividual(0) : p.getIndividualWithRank(1);
			else
				compare = p.size() == 1 ? p.getIndividual(0) : p.getIndividualWithRank(p.size());
			fitness = fitnessFunction.evaluate(compare, p);
			if (fitness == fitnessFunction.getOptimalFitnessValue()) {
				stopRun();
			}
		} catch (UnsupportedOperationException e) {
			// should never happen
			throw new RuntimeException("The stopping criterion can't be applied since the fitness function doesn't know its optimum.");
		}
	}

	public void setPropertyAll(Boolean all) {
		this.all = all.booleanValue();
	}

	public Boolean getPropertyAll() {
		return new Boolean(all);
	}

	public String getShortDescriptionForAll() {
		return "Whole population";
	}

	public String getLongDescriptionForAll() {
		return "If unchecked, the run is stopped as soon as one individual has reached " + "the optimum.\n" + "If checked, all individuals have to reach the optimum.";
	}
}
