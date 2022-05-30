/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.observer.isingmodelcliques;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.event.IndividualListEvent;
import freak.core.event.IndividualListEventListener;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.searchspace.GeneralString;

/**
 * Computes the quota of runs in which the success of the run was predicted  correctly using a node based potential function.
 * @author  Dirk
 */
public class CliquesNodeBasedSuccessPrediction extends AbstractObserver implements BatchEventListener, RunEventListener, IndividualListEventListener, Configurable {

	private int totalRuns;
	private int successfullyPredictedRuns;
	
	private int bound = 0;
	private int generation = 0;
	
	private boolean prediction = false;
	private int currentRun = 0;
	
	private CliquesPotentialFunction potentialFunction;
	
	public CliquesNodeBasedSuccessPrediction(Schedule schedule) {
		super(schedule);
		setMeasure(BATCHES);
		
		potentialFunction = new CliquesPotentialFunction(schedule);
		
		if (schedule.getPhenotypeSearchSpace() instanceof GeneralString) {
			bound = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension() / 8;
			generation = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		}
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		if (!(schedule.getRealFitnessFunction() instanceof IsingModelCliques)) {
			throw new UnsupportedEnvironmentException("This module works on Ising Model Cliques only.");
		}
		potentialFunction.testSchedule(schedule);
	}
	
	public void initialize() {
		super.initialize();
		potentialFunction.initialize();
	}

	public Class getOutputDataType() {
		return Double.class;
	}

	public String getName() {
		return "Cliques Success Prediction (node based)";
	}

	public String getDescription() {
		return "Computes the quota of correctly predicted runs. The success " +
			"of a run is predicted by a potential function on nodes, namely " +
			"phi(x) := (number of 0-colored nodes in x - n/2). The test " +
			"predicts a successful run if |phi(x)| >= bound after the specified " +
			"number of generations.";
	}

	public void createEvents() {
		super.createEvents();
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
		schedule.getEventController().addEvent(this, IndividualListEvent.class, schedule.getPopulationManager());
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

	public void batchFinished(BatchEvent evt) {
		updateViews(new Double(successfullyPredictedRuns / (double)totalRuns));
		totalRuns = 0;
		successfullyPredictedRuns = 0;
	}

	public void setPropertyBound(Integer i) {
		if (i.intValue() >= 0) {
			bound = i.intValue(); 
		}
	}
	
	public Integer getPropertyBound() {
		return new Integer(bound);
	}

	public void setPropertyGeneration(Integer i) {
		if (i.intValue() >= 0) {
			generation = i.intValue(); 
		}
	}
	
	public Integer getPropertyGeneration() {
		return new Integer(generation);
	}

	public void individualList(IndividualListEvent evt) {
		super.individualList(evt);
		
		if (getSchedule().getCurrentGeneration() == generation) {
			IndividualList population = getSchedule().getPopulationManager().getPopulation(); 
			Individual elitist = population.getIndividualWithRank(1);

			int potential = potentialFunction.computePotentialFunction(elitist);

			prediction = (Math.abs(potential) >= bound);
			currentRun = getSchedule().getCurrentRun();
		}
	}

	public void runFinalize(RunEvent evt) {
		super.runFinalize(evt);
		
		if (getSchedule().getCurrentRun() == currentRun) {

			IndividualList population = getSchedule().getPopulationManager().getPopulation(); 
			Individual elitist = population.getIndividualWithRank(1);

			SingleObjectiveFitnessFunction fitnessFunction = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();
			double bestFitness = fitnessFunction.evaluate(elitist, population);

			boolean success = (bestFitness >= fitnessFunction.getOptimalFitnessValue());

			if (prediction == success) successfullyPredictedRuns++; 
			totalRuns++;
		}
	}

}
