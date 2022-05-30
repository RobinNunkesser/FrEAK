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
 * Computes the progress rate, a local performance measure based on the 
 * individual's distance to the global optimum.
 * Here, the progress rate is defined as the difference between the average 
 * distance to the optimum in the current population and the average distance
 * to the optimum in the last population.<br> 
 * The observer can only be used if the search space provides a metric
 * by implementing the interface <code>freak.core.searchspace.HasMetric</code>,
 * the fitness function has an unique global optimum and the fitness function 
 * is able to return the optimum via <code>getGenotypeOptimum()</code>.
 * 
 * @see freak.core.searchspace.HasMetric
 * @see freak.core.fitness.FitnessFunction#getGenotypeOptimum()
 * 
 * @author Dirk, Stefan
 */
public class ProgressRate extends AbstractObserver implements IndividualListEventListener, RunEventListener, Configurable {

	private Double lastAverageDistance;

	/**
	 * Constructs a new <code>ProgressRate</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public ProgressRate(Schedule schedule) {
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
		return "Progress Rate";
	}

	public String getDescription() {
		return "Computes the progress rate, the difference between the average distance to the global optimum of the current population and last generation's population.";
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		Individual[] individuals = individualList.toArray();

		HasMetric searchSpace = (HasMetric)getSchedule().getGenotypeSearchSpace();
		Genotype optimum = getSchedule().getFitnessFunction().getGenotypeOptimum();

		double sum = 0;
		for (int i = 0; i < individuals.length; i++) {
			sum += searchSpace.getDistance(individuals[i].getGenotype(), optimum);
		}
		double average = sum / individuals.length;

		double result = 0;
		if (lastAverageDistance != null) {
			result = lastAverageDistance.doubleValue() - average;
		}
		lastAverageDistance = new Double(average);

		updateViews(new Double(result));
	}

	public void runStarted(RunEvent evt) {
		lastAverageDistance = null;
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, schedule.getPopulationManager());
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}
}
