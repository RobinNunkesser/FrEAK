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
 * Computes the fitness variance within the individuals.
 * 
 * @author Patrick, Dirk
 */
public class FitnessVariance extends AbstractObserver implements IndividualListEventListener, Configurable {

	public FitnessVariance(Schedule schedule) {
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
		return "Fitness Variance";
	}

	public String getDescription() {
		return "Computes the current population's fitness variance.";
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();
		Individual[] indList = individualList.toArray();

		double[] values = new double[indList.length];

		double sum = 0;
		for(int i = 0; i < indList.length; i++) {
			values[i] = ((SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction()).evaluate(indList[i], individualList);
			sum = sum + values[i];
		}
		double average = sum / values.length;

		sum = 0;
		for (int i = 0; i < indList.length; i++) {
			sum = sum + (values[i] - average) * (values[i] - average);
		}
		average = sum / values.length;

		updateViews(new Double(average));
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}