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
 * Simply collects all observed individuals.
 * 
 * @author Patrick, Dirk
 */
public class AllIndividuals extends AbstractObserver implements IndividualListEventListener, Configurable {

	public AllIndividuals(Schedule schedule) {
		super(schedule);
		setMeasure(Observer.GENERATIONS);
	}

	public String getName() {
		return "All Individuals";
	}

	public String getDescription() {
		return "Shows all observed individuals.";
	}

	public Class getOutputDataType() {
		return IndividualList.class;
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = (IndividualList)evt.getIndividualList();

		updateViews(individualList);
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}