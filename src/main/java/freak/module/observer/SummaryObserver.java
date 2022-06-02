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

import freak.core.control.*;
import freak.core.event.*;
import freak.core.fitness.MultiObjectiveFitnessFunction;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.observer.*;
import freak.core.population.*;

import java.util.Iterator;

/**
 * Draws a summary of all runs.
 * 
 * @author Dirk
 */
public class SummaryObserver extends AbstractObserver implements RunEventListener {

	/**
	 * Constructs a new <code>SummaryObserver</code>.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public SummaryObserver(Schedule schedule) {
		super(schedule);
		setMeasure(RUNS);
	}

	public Class getOutputDataType() {
		return Summary.class;
	}

	public String getName() {
		return "Summary";
	}

	public String getDescription() {
		return "Draws a summary of all runs.";
	}

	public void runFinalize(RunEvent evt) {
		Summary summary = new Summary();

		IndividualList lastPopulation = getSchedule().getPopulationManager().getPopulation();

		try {
			summary.setBestIndividuals(lastPopulation.getAllIndividualsWithRank(1));
		} catch (Exception e) { 
			if (schedule.getFitnessFunction() instanceof MultiObjectiveFitnessFunction) {
				summary.setBestIndividuals(lastPopulation);
			} 				
		}

		summary.setLastPopulationSize(lastPopulation.size());
		summary.setNumberOfGenerations(getSchedule().getCurrentGeneration());
		summary.setRunNumber(evt.getRunIndex().run);

		updateViews(summary);
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

	/**
	 * A simple entity class containing the data needed for a summary. Summaries generate the textual output displayed via  <code>toString()</code> 
	 * @author  Dirk
	 */
	class Summary {

		private int runNumber;

		private int numberOfGenerations;
		private int lastPopulationSize;

		private IndividualList bestIndividuals;

		/**
		 * @return  the bestIndividuals
		 * @uml.property  name="bestIndividuals"
		 */
		public IndividualList getBestIndividuals() {
			return bestIndividuals;
		}

		/**
		 * @return  the lastPopulationSize
		 * @uml.property  name="lastPopulationSize"
		 */
		public int getLastPopulationSize() {
			return lastPopulationSize;
		}

		/**
		 * @return  the numberOfGenerations
		 * @uml.property  name="numberOfGenerations"
		 */
		public int getNumberOfGenerations() {
			return numberOfGenerations;
		}

		/**
		 * @param bestIndividuals  the bestIndividuals to set
		 * @uml.property  name="bestIndividuals"
		 */
		public void setBestIndividuals(IndividualList individuals) {
			bestIndividuals = individuals;
		}

		/**
		 * @param lastPopulationSize  the lastPopulationSize to set
		 * @uml.property  name="lastPopulationSize"
		 */
		public void setLastPopulationSize(int i) {
			lastPopulationSize = i;
		}

		/**
		 * @param numberOfGenerations  the numberOfGenerations to set
		 * @uml.property  name="numberOfGenerations"
		 */
		public void setNumberOfGenerations(int i) {
			numberOfGenerations = i;
		}

		public String toString() {
			StringBuffer s = new StringBuffer("Summary of run " + getRunNumber() + "\n\n");

			s.append("Optimization time in generations: " + getNumberOfGenerations() + "\n");
			s.append("Size of final population: " + getLastPopulationSize() + "\n");

			if ((getSchedule().getRealFitnessFunction() instanceof SingleObjectiveFitnessFunction) || (getSchedule().getRealFitnessFunction() instanceof MultiObjectiveFitnessFunction)){
				if (getSchedule().getRealFitnessFunction() instanceof SingleObjectiveFitnessFunction){ 
					double fitness = ((SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction()).evaluate(getBestIndividuals().getRandomIndividual(), getSchedule().getPopulationManager().getPopulation());
					s.append("Best individuals of final population with fitness: " + fitness + "\n");								
				} else {
					s.append("Individuals of final population: \n");					
				}

				Iterator iter = getBestIndividuals().iterator();
				while (iter.hasNext()) {
					Individual individual = (Individual)iter.next();
					s.append(individual.toString());
					s.append("\n");
				}				
			}
			return s.toString();
		}

		/**
		 * @return  the runNumber
		 * @uml.property  name="runNumber"
		 */
		public int getRunNumber() {
			return runNumber;
		}

		/**
		 * @param runNumber  the runNumber to set
		 * @uml.property  name="runNumber"
		 */
		public void setRunNumber(int i) {
			runNumber = i;
		}

	}

}
