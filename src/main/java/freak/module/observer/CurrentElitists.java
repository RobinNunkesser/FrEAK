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
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.observer.*;
import freak.core.population.*;

/**
 * Collects the best individuals from within the current set of individuals.
 * 
 * @author Dirk
 */
public class CurrentElitists extends AbstractObserver implements IndividualListEventListener, Configurable {

	public CurrentElitists(Schedule schedule) {
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
		return "Current Elitists";
	}

	public String getDescription() {
		return "Displays the fittest individuals among the observed individuals.";
	}

	public Class getOutputDataType() {
		return IndividualList.class;
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList().getAllIndividualsWithRank(1);

		updateViews(individualList);
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}