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
 * Computes the average fitness value within the individuals.
 * 
 * @author Dirk
 */
public class AverageFitness extends AbstractObserver implements IndividualListEventListener, Configurable {

	public AverageFitness(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);		
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}
	}

	public String getName() {
		return "Average Fitness";
	}

	public String getDescription() {
		return "Computes the individuals' average fitness.";
	}

	public Class getOutputDataType() {
		return Double.class;
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();
		Individual[] individuals = individualList.toArray();
		SingleObjectiveFitnessFunction fitness = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();
		double sum = 0;
		for (int i = 0; i < individuals.length; i++) {
			sum = sum + fitness.evaluate(individuals[i], individualList);
		}
		double average = sum / individuals.length;

		updateViews(new Double(average));
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}