/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.observer.isingmodelcliques;

import freak.core.control.Schedule;
import freak.core.event.IndividualListEvent;
import freak.core.event.IndividualListEventListener;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.IncompatibleModulePropertyException;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * Computes a potential function on the cliques.
 * 
 * @author Dirk
 */
public class CliquesPotentialFunction extends AbstractObserver implements IndividualListEventListener, Configurable {

	public CliquesPotentialFunction(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {		
		if (!(schedule.getRealFitnessFunction() instanceof IsingModelCliques)) {
			throw new UnsupportedEnvironmentException("This module works on Ising Model Cliques only.");
		}
		if (((IsingModelCliques)schedule.getRealFitnessFunction()).getPropertyNumberOfCliques().intValue() != 2) {
			throw new IncompatibleModulePropertyException(schedule.getRealFitnessFunction(), "Number of Cliques", "This observer only works on two cliques.");
		}
		if (((GeneralString)getSchedule().getGenotypeSearchSpace()).getNumChars() != 2) {
			throw new IncompatibleModulePropertyException(schedule.getGenotypeSearchSpace(), "Number of Chars", "This observer only works on cliques colored with two colors.");
		}
	}

	public String getName() {
		return "Cliques Potential Function";
	}

	public String getDescription() {
		return "Computes the following potential function on two cliques: " +
			"phi(x) := (number of 0-colored nodes in x - n/2)";
	}

	public Class getOutputDataType() {
		return int[].class;
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();
		Individual[] individuals = individualList.toArray();

		int[] result = new int[individuals.length];
		for (int i = 0; i < individuals.length; i++) {
			result[i] = computePotentialFunction(individuals[i]);
		}

		updateViews(result);
	}
	
	public int computePotentialFunction(Individual individual) {
		int result = 0;
		int[] genotype = ((GeneralStringGenotype)individual.getGenotype()).getIntArray(); 
		
		IsingModelCliques fitness = (IsingModelCliques)getSchedule().getRealFitnessFunction();
		IsingModelCliques.Clique[] cliques = fitness.getCliques();
		
		if (cliques.length == 2 && ((GeneralString)getSchedule().getGenotypeSearchSpace()).getNumChars() == 2) {
			for (int j = 0; j < cliques.length; j++) {
				for (int i = 0; i < cliques[j].getSize(); i++) {
					if (genotype[cliques[j].getStart() + i] == 0) result++;
				}
			}
			result -= genotype.length / 2;
		}
		
		return result;
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
}