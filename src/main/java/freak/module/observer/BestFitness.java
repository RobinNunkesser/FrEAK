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
 * Computes the best fitness within the current set of individuals.
 * 
 * @author Dirk
 */
public class BestFitness extends AbstractObserver implements IndividualListEventListener, Configurable {

	public BestFitness(Schedule schedule) {
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
		return "Best Fitness";
	}

	public String getDescription() {
		return "Computes the individuals' best fitness value.";
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public void individualList(IndividualListEvent evt) {
		Individual ind = evt.getIndividualList().getIndividualWithRank(1);
		double fitness = ((SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction()).evaluate(ind,evt.getIndividualList());

		updateViews(new Double(fitness));
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}