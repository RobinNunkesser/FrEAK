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
 * Computes the distance of all individuals to the global optimum. <br>
 * The observer can only be used if the search space provides a metric
 * by implementing the interface <code>freak.core.searchspace.HasMetric</code>,
 * the fitness function has an unique global optimum and the fitness function 
 * is able to return the optimum via <code>getGenotypeOptimum()</code>.
 * 
 * @author Dirk, Patrick
 * @see freak.core.searchspace.HasMetric
 * @see freak.core.fitness.FitnessFunction#getGenotypeOptimum()
 * 
 */
public class DistanceFromOptimum extends AbstractObserver implements IndividualListEventListener, Configurable {

	/**
	 * Constructs a new <code>DistanceFromOptimum</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public DistanceFromOptimum(Schedule schedule) {
		super(schedule);

		setMeasure(GENERATIONS);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		try {
			schedule.getFitnessFunction().getGenotypeOptimum();
		} catch (UnsupportedOperationException e) {
			throw new UnsupportedEnvironmentException("Global optimum is unknown.");
		}

		if (!(schedule.getGenotypeSearchSpace() instanceof HasMetric)) {
			throw new UnsupportedEnvironmentException("Search space has no metric.");
		}
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public String getName() {
		return "Distance from Optimum";
	}

	public String getDescription() {
		return "Computes the individuals' distance to the global optimum.";
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		Individual[] individuals = individualList.toArray();
		double[] distance = new double[individualList.size()];

		HasMetric searchSpace = (HasMetric)getSchedule().getGenotypeSearchSpace();
		Genotype optimum = getSchedule().getFitnessFunction().getGenotypeOptimum();

		for (int i = 0; i < individuals.length; i++) {
			distance[i] = searchSpace.getDistance(individuals[i].getGenotype(), optimum);
		}

		updateViews(distance);
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}
