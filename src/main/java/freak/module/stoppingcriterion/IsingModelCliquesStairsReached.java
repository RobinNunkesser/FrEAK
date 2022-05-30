/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.stoppingcriterion;

import freak.core.control.Schedule;
import freak.core.event.GenerationEvent;
import freak.core.modulesupport.IncompatibleModulePropertyException;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.stoppingcriterion.AbstractGenerationStoppingCriterion;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * This stopping criterion can only be applied if the fitness function selected
 * is IsingModelCliques with bridge distribution set to "Stairs". 
 * It stopps the run as soon as the best individual reaches a point in the first 
 * half of one stair.
 * 
 * @author Heiko, Dirk
 */
public class IsingModelCliquesStairsReached extends AbstractGenerationStoppingCriterion {
	
	public IsingModelCliquesStairsReached(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getRealFitnessFunction() instanceof IsingModelCliques)) {
			throw new UnsupportedEnvironmentException();		
		}
		if (((IsingModelCliques)schedule.getRealFitnessFunction()).getPropertyBridgeSelectionStrategy().getIndex() != IsingModelCliques.STAIRS) {
			throw new IncompatibleModulePropertyException(schedule.getRealFitnessFunction(), "Bridge selection strategy", "This observer only works on the bridge distribution named stairs.");
		}
	}

	protected void checkCriterion(GenerationEvent evt) {
		IsingModelCliques ising = (IsingModelCliques)getSchedule().getRealFitnessFunction();
		int k = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getNumChars();
		if (ising.getPropertyBridgeSelectionStrategy().getIndex() == IsingModelCliques.STAIRS && ising.getCliques().length == 2 && k == 2) {
			IndividualList p = getSchedule().getPopulationManager().getPopulation();
			Individual ind = p.getIndividualWithRank(1);
			int[] gen = ((GeneralStringGenotype)ind.getGenotype()).getIntArray();
			int n = gen.length;
			
			// check if this is a point 0^i 1^n/2-i 0^n/2 or 1^i 0^n/2-i 1^n/2
			// for 0 <= i < n/8 

			int colorOfSecondClique = gen[gen.length - 1];
			for (int i = n/2; i < n; i++) {
				// if the second clique is not monochromatic, the stairs are 
				// not reached
				if (gen[i] != colorOfSecondClique) return;
			}
			
			// check the color of the first clique
			
			// save whether the color has been changed from 0 to 1 or vice versa
			boolean change = false;
			for (int i = 0; i < n/2; i++){
				if (change) {
					// all following nodes must have the opposite color
					if (gen[i] == colorOfSecondClique) {
						return;
					}
				} else {
					if (gen[i] != colorOfSecondClique) {
						// we're either not on stairs or have climbed too far
						if (i >= n/8) return;
						// mark that the second block of length n/2-i has been
						// reached
						change = true;
					}
				}
			}
			// all tests passed, we're on stairs
			stopRun();
		}
	}

	public String getName() {
		return "Cliques Stairs Reached";
	}

	public String getDescription() {
		return "Stopps the run if a best individual reached the first half of " +
			"one stair. The run is only stopped if the bridge distribution " +
			"STAIRS is used on exactly two cliques with two colors.";
	}

}
