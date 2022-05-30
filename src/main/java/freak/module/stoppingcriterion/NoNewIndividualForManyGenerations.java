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
 * This stopping-condition is satisfied when the no new individual is found
 * for a fixed generations count
 *
 * @author Robin
 */
public class NoNewIndividualForManyGenerations extends AbstractStoppingCriterion implements Configurable, GenerationEventListener {
	
	private int generations = 10000;
	
	public NoNewIndividualForManyGenerations(Schedule schedule) {
		super(schedule);		
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
	}
	
	public String getName() {
		return "No new individual";
	}
	
	public String getDescription() {
		return "This stopping-criterium stops when no new individual is found for a specified amount of generations";
		
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
		return "When no new individual is accepted for this amount of generations a run stops.";
	}
	
	/**
	 * This method is called when a new generation is completely created. It
	 * checks whether already count many generation have been created. If so it
	 * stops the run.
	 */
	public void generation(GenerationEvent evt) {
		IndividualList population = getSchedule().getPopulationManager().getPopulation();
		int birth = population.getIndividual(0).getDateOfBirth();
		if (getSchedule().getCurrentGeneration()>birth+generations) {
			stopRun();
		}
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, GenerationEvent.class, schedule);
	}
}
