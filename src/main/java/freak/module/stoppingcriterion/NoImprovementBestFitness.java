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
import freak.core.modulesupport.*;
import freak.core.stoppingcriterion.*;
import freak.core.population.*;
import freak.core.searchspace.*;
import freak.core.fitness.SingleObjectiveFitnessFunction;

/**
 * This stopping-condition is satisfied when the best fitness remains unchanged
 * for a fixed generations count
 *
 * @author Michael
 */
public class NoImprovementBestFitness extends AbstractStoppingCriterion implements Configurable, GenerationEventListener {
	
	private int generations = 1000;
	private int noImprovement;
	private double lastFitness;
	
	public NoImprovementBestFitness(Schedule schedule) {
		super(schedule);
		
		noImprovement = -1;
		if (schedule.getGenotypeSearchSpace() instanceof HasDimension)
			generations = 2*((HasDimension)schedule.getGenotypeSearchSpace()).getDimension();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}
	}
	
	public String getName() {
		return "No Improvement of Best Fitness";
	}
	
	public String getDescription() {
		return "This stopping-criterium stops when the best fitness remains unchanged for a specified amount of generations";
		
	}
	
	public void setPropertyGenerations(Integer generations) {
		if (generations.intValue() > 0) {
			this.generations = generations.intValue();
		}
	}
	
	public Integer getPropertyGenerations() {
		return new Integer(generations);
	}
	
	public String getShortDescriptionForGenerations() {
		return "Amount of generations";
	}
	
	public String getLongDescriptionForGenerations() {
		return "When the best fitness remains unchanged for this amount of generations a run stops.";
	}
	
	/**
	 * This method is called when a new generation is completely created. It
	 * checks whether already count many generation have been created. If so it
	 * stops the run.
	 */
	public void generation(GenerationEvent evt) {
		IndividualList population = getSchedule().getPopulationManager().getPopulation();
		Individual ind = population.getIndividualWithRank(1);
		double fitness = ((SingleObjectiveFitnessFunction)getSchedule().getRealFitnessFunction()).evaluate(ind,population);
		
		if (fitness != lastFitness) {
			lastFitness = fitness;
			noImprovement = 0;
		} else {
			noImprovement++;
			if (noImprovement >= generations) {
				stopRun();
			}
		}
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, GenerationEvent.class, schedule);
	}
}
