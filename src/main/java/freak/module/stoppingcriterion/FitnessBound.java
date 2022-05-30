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
 * number of generations have passed. If a search point is found whose fitness
 * value is within a specified range of the optimal fitness value the run is
 * stopped.  
 * 
 * @author Heiko
 */
public class FitnessBound extends AbstractGenerationStoppingCriterion implements GenerationEventListener {

	private double range;

	private boolean max;

	private boolean all;

	/**
	 * Creates a new <code>FitnessBound</code> object.
	 * 
	 * @param schedule a reference to the schedule.
	 */
	public FitnessBound(Schedule schedule) {
		super(schedule);
		max = true;
		try {
			range = ((SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction()).getUpperBound();
		} catch(Exception e) {}
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}
	}

	public String getName() {
		return "Fitness Bound";
	}

	public String getDescription() {
		return "Stops a run if the specified fitness bound is reached.";
	}

	public void setPropertyBound(Double range) {
		this.range = range.doubleValue();
	}

	public Double getPropertyBound() {
		return new Double(range);
	}

	public void setPropertyMaximize(Boolean max) {
		this.max = max.booleanValue();
	}

	public Boolean getPropertyMaximize() {
		return new Boolean(max);
	}

	public String getLongDescriptionForBound() {
		return "A fitness bound that has to be passed to stop the run.";
	}

	public String getShortDescriptionForMaximize() {
		return "Maximization problem";
	}

	public String getLongDescriptionForMaximize() {
		return "Specifies whether a maximization problem is processed " + "or a minimization problem.";
	}

	public void checkCriterion(GenerationEvent evt) {
		try {
			IndividualList population = getSchedule().getPopulationManager().getPopulation();
			SingleObjectiveFitnessFunction fitnessFunction = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();
			double fitness;
			if (!all) {
				Individual fittest = population.getIndividualWithRank(1);
				fitness = fitnessFunction.evaluate(fittest, population);
			} else {
				Individual worst = population.getIndividualWithRank(population.size());
				fitness = fitnessFunction.evaluate(worst, population);
			}
			if (max) {
				if (fitness >= range) {
					stopRun();
				}
			} else {
				if (fitness <= range) {
					stopRun();
				}
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
		return "If unchecked, the run is stopped as soon as one individual reaches " + "the specified fitness bound.\n" + "If checked, all individuals have to reached the specified fitness bound.";
	}

}
