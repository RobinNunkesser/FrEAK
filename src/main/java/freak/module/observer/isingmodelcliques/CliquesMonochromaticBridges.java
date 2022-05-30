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
import freak.core.event.IndividualListEvent;
import freak.core.event.IndividualListEventListener;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * Computes the number of monochromatic bridges between the Ising Model cliques.
 * 
 * @author Dirk
 */
public class CliquesMonochromaticBridges extends AbstractObserver implements IndividualListEventListener, Configurable {

	public CliquesMonochromaticBridges(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {		
		if (!(schedule.getRealFitnessFunction() instanceof IsingModelCliques)) {
			throw new UnsupportedEnvironmentException("This module works on Ising Model Cliques only.");
		}
	}

	public String getName() {
		return "Cliques Monochromatic Bridge Rate";
	}

	public String getDescription() {
		return "Computes the rate of monochromatic bridges.";
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();
		Individual[] individuals = individualList.toArray();

		double[] result = new double[individuals.length];
		for (int i = 0; i < individuals.length; i++) {
			result[i] = computePotentialFunction(individuals[i]);
		}

		updateViews(result);
	}
	
	public double computePotentialFunction(Individual individual) {
		int result = 0;
		int[] genotype = ((GeneralStringGenotype)individual.getGenotype()).getIntArray(); 
		
		IsingModelCliques fitness = (IsingModelCliques)getSchedule().getRealFitnessFunction();
		
		int[][] bridges = fitness.getBridges();
		for (int i = 0; i < bridges.length; i++) {
			if (genotype[bridges[i][0]] == genotype[bridges[i][1]]) result++;
		}
		
		return result / (double)bridges.length;
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}