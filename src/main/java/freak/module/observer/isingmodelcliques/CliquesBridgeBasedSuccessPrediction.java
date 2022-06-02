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
import freak.core.event.*;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.searchspace.GeneralString;

/**
 * Computes the quota of runs in which the success of the run was predicted  correctly using a bridge based potential function.
 * @author  Dirk
 */
public class CliquesBridgeBasedSuccessPrediction extends AbstractObserver implements BatchEventListener, RunEventListener, IndividualListEventListener, Configurable {

	private int totalRuns;
	private int successfullyPredictedRuns;
	
	private int generation = 0;
	
	private boolean prediction = false;
	private int currentRun = 0;
	
	private CliquesMonochromaticBridges potentialFunction;
	
	public CliquesBridgeBasedSuccessPrediction(Schedule schedule) {
		super(schedule);
		setMeasure(BATCHES);
		
		potentialFunction = new CliquesMonochromaticBridges(schedule);
		
		if (schedule.getPhenotypeSearchSpace() instanceof GeneralString) {
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
		return "Cliques Success Prediction (bridge based)";
	}

	public String getDescription() {
		return "Computes the quota of correctly predicted runs. The success " +
			"of a run is predicted by the rate of monochromatic bridges. " +
			"If after the specified number of generations, the rate of " +
			"monochromatic bridges is greater than 1/2, the run is predicted to be" +
			"successful.";
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

			double potential = potentialFunction.computePotentialFunction(elitist);

			prediction = potential > 0.5;
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
