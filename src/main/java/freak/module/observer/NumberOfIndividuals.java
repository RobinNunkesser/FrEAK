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
 * Computes the number of individuals observed.
 * 
 * @author Dirk
 */
public class NumberOfIndividuals extends AbstractObserver implements IndividualListEventListener, Configurable {

	/**
	 * Constructs a new <code>NumberOfIndividuals</code> observer. 
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public NumberOfIndividuals(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public String getName() {
		return "Number of Individuals";
	}

	public String getDescription() {
		return "Computes the number of observed individuals.";
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		updateViews(new Double(individualList.size()));
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}
