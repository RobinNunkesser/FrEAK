/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.observer;

import freak.core.control.Schedule;
import freak.core.event.IndividualListEvent;
import freak.core.event.IndividualListEventListener;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An abstract superclass for observers collecting the best individuals  throughout some time period (run/batch/etc.). To work properly, the fitness of an individual must be deterministic  and static: <code>evaluate</code>  must always return the same fitness value for a  specified individual. Subclasses are to call <code>reset()</code> at the end of the period. 
 * @author  Patrick, Dirk
 */
public abstract class BestIndividualsInSomeTimePeriod extends AbstractObserver implements IndividualListEventListener, Configurable {

	private double bestValue;

	// a map containing the currently best individuals mapped by their genotypes
	private HashMap mapOfBestIndividuals;
	// an alternative data structure containing the individuals inside the set
	private IndividualList bestIndividualList;

	public BestIndividualsInSomeTimePeriod(Schedule schedule) {
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
		return IndividualList.class;
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();

		IndividualList newElitists = individualList.getAllIndividualsWithRank(1);

		Individual ind = newElitists.getRandomIndividual();

		double newFitness = ((SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction()).evaluate(ind, individualList);

		if ((mapOfBestIndividuals == null) || (newFitness > bestValue)) {
			mapOfBestIndividuals = new HashMap();
			addIndividuals(newElitists);
			bestValue = newFitness;
		} else {
			if (newFitness == bestValue) {
				addIndividuals(newElitists);
			}
		}

		// since bestIndividualList is recreated if the map is modified, it
		// is read-only and thus can be passed to the views directly without
		// cloning it first.
		updateViews(bestIndividualList);
	}

	/**
	 * Adds the specified individuals to the map of best individuals.
	 * In addition, the list of best individuals is recreated if the map of 
	 * best individuals is modified.
	 */	
	private void addIndividuals(IndividualList individuals) {
		boolean modified = false;
		for (Iterator iter = individuals.iterator(); iter.hasNext();) {
			Individual individual = (Individual)iter.next();
			
			if (!mapOfBestIndividuals.containsKey(individual.getGenotype())) {
				modified = true;
				mapOfBestIndividuals.put(individual.getGenotype(), individual);
			}
		}

		// create new IndividualList from map if necessary
		if (modified || bestIndividualList == null) {
			bestIndividualList = new Population(getSchedule());
			for (Iterator iter = mapOfBestIndividuals.values().iterator(); iter.hasNext();) {
				Individual individual = (Individual)iter.next();
				bestIndividualList.addIndividual(individual);
			}
		}
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
	
	/**
	 * Resets the collection of best individuals. This method is to be called 
	 * at the end of the appropriate time period.
	 */
	public void reset() {
		mapOfBestIndividuals = null;
	}

}