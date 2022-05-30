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
import freak.core.modulesupport.*;
import freak.core.observer.*;
import freak.core.population.*;

/**
 * Computes the ages of the observed individuals.
 * 
 * @author Dirk
 */
public class CurrentAges extends AbstractObserver implements IndividualListEventListener, Configurable {

	/**
	 * Constructs a new <code>CurrentAges</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public CurrentAges(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public String getName() {
		return "Current Ages";
	}

	public String getDescription() {
		return "Computes the ages of the observed individuals.";
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		Individual[] individuals = individualList.toArray();
		double[] ages = new double[individuals.length];
		int generation = getSchedule().getCurrentGeneration();
		for (int i = 0; i < individuals.length; i++) {
			ages[i] = generation - individuals[i].getDateOfBirth();
		}

		updateViews(ages);
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}
