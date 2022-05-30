/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.observer;

import java.util.*;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.modulesupport.*;
import freak.core.observer.*;
import freak.core.population.*;

/**
 * Computes the selection intensity. 
 * The selection intensity here is defined as the quote of individuals born in
 * the current generation.
 * 
 * @author Dirk
 */
public class SelectionIntensity extends AbstractObserver implements IndividualListEventListener, Configurable {

	/**
	 * Constructs a new <code>SelectionIntensity</code> observer. 
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public SelectionIntensity(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public String getName() {
		return "Selection Intensity";
	}

	public String getDescription() {
		return "Computes the quota of individuals born in the current generation.";
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		int count = 0;
		Iterator iter = individualList.iterator();
		while (iter.hasNext()) {
			Individual individual = (Individual)iter.next();
			if (individual.getDateOfBirth() == getSchedule().getCurrentGeneration())
				count++;
		}

		updateViews(new Double(count));
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}
