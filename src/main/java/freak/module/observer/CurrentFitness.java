/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.observer;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.*;
import freak.core.observer.*;
import freak.core.population.*;

/**
 * Computes the fitness values of the individuals observed.
 * 
 * @author Dirk
 */
public class CurrentFitness extends AbstractObserver implements IndividualListEventListener, Configurable {

	/**
	 * Constructs a new <code>CurrentFitness</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public CurrentFitness(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public String getName() {
		return "Current Fitness";
	}

	public String getDescription() {
		return "Computes the fitness values of the observed individuals.";
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		Individual[] individuals = individualList.toArray();
		double[] fitnessValues = new double[individuals.length];
		for (int i = 0; i < individuals.length; i++) {
			fitnessValues[i] = ((SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction()).evaluate(individuals[i], individualList);
		}

		updateViews(fitnessValues);
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}
