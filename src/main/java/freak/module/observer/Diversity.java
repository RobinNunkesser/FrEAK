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
import freak.core.searchspace.*;

/**
 * Computes the diversity within the individuals.
 * The diversity is defined as the average distance between pairs of 
 * individuals.
 *  
 * @see freak.core.searchspace.HasMetric
 * @author Dirk
 */
public class Diversity extends AbstractObserver implements IndividualListEventListener, Configurable {

	/**
	 * Constructs a new <code>Diversity</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public Diversity(Schedule schedule) {
		super(schedule);

		setMeasure(GENERATIONS);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getGenotypeSearchSpace() instanceof HasMetric)) {
			throw new UnsupportedEnvironmentException("Search space has no metric.");
		}
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public String getName() {
		return "Diversity";
	}

	public String getDescription() {
		return "Computes the individuals' diversity, the average distance between pairs of individuals.";
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		Individual[] individuals = individualList.toArray();

		HasMetric searchSpace = (HasMetric)getSchedule().getGenotypeSearchSpace();

		double sum = 0;
		for (int i = 0; i < individuals.length; i++) {
			for (int j = i + 1; j < individuals.length; j++) {
				sum += searchSpace.getDistance(individuals[i].getGenotype(), individuals[j].getGenotype());
			}
		}
		double average = sum * 2 / (individuals.length * (individuals.length - 1));

		updateViews(new Double(average));
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}
