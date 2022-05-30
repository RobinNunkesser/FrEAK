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
 * Computes the quality gain, the difference between the current average 
 * fitness and last generation's average fitness.
 * In the first generation, the quality gain is defined as <code>0</code>.
 * 
 * @author Dirk, Stefan
 */
public class QualityGain extends AbstractObserver implements IndividualListEventListener, RunEventListener, Configurable {

	private Double lastAverageFitness;

	/**
	 * Constructs a new <code>QualityGain</code> observer.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public QualityGain(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public String getName() {
		return "Quality Gain";
	}

	public String getDescription() {
		return "Computes the difference between the current average fitness and last generation's average fitness";
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		Individual[] individuals = individualList.toArray();
		double sum = 0;
		for(int i = 0; i < individuals.length; i++) {
			sum += ((SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction()).evaluate(individuals[i], individualList);
		}
		double average = sum / individuals.length;

		double result = 0;
		if (lastAverageFitness != null) {
			result = average - lastAverageFitness.doubleValue();
		}
		lastAverageFitness = new Double(average);

		updateViews(new Double(result));

	}

	public void runStarted(RunEvent evt) {
		lastAverageFitness = null;
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, schedule.getPopulationManager());
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}
}
