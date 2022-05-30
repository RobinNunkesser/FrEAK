/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.observer;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;

/**
 * Computes the quota of runs that reach the optimum at the time the run is 
 * stopped by the stopping criterion.
 * 
 * @author Dirk, Stefan
 */
public class SuccessfulRuns extends AbstractObserver implements BatchEventListener, RunEventListener {

	private int totalRuns;
	private int successfulRuns;
	
	/**
	 * Constructs a new <code>SuccessfulRuns</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public SuccessfulRuns(Schedule schedule) {
		super(schedule);

		setMeasure(BATCHES);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}

		try {
			((SingleObjectiveFitnessFunction)schedule.getFitnessFunction()).getOptimalFitnessValue();
		} catch (UnsupportedOperationException e) {
			throw new UnsupportedEnvironmentException("Optimal fitness value is unknown.");
		}
	}

	public Class getOutputDataType() {
		return Double.class;
	}

	public String getName() {
		return "Success Rate";
	}

	public String getDescription() {
		return "Computes the quota of successful runs in the current batch. " +
			"Successful runs are runs that have " +
			"reached the optimal fitness value by the time the run is stopped by " +
			"the stopping criterion.";
	}

	public void runFinalize(RunEvent evt) {
		IndividualList population = getSchedule().getPopulationManager().getPopulation(); 
		Individual elitist = population.getIndividualWithRank(1);

		SingleObjectiveFitnessFunction fitnessFunction = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();
		double bestFitness = fitnessFunction.evaluate(elitist, population);

		if (bestFitness >= fitnessFunction.getOptimalFitnessValue()) {
			successfulRuns++; 
		}
		totalRuns++;
	}
	
	public void createEvents() {
		super.createEvents();
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

	public void batchFinished(BatchEvent evt) {
		updateViews(new Double(successfulRuns / (double)totalRuns));
		totalRuns = 0;
		successfulRuns = 0;
	}

}
